class WebSocketManager {
  constructor() {
    if (WebSocketManager.instance) {
      return WebSocketManager.instance
    }

    this.ws = null
    this.url = 'ws://localhost:8120/api/ws/message'
    this.token = null
    this.status = 'disconnected'
    this.listeners = new Map()
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 10
    this.reconnectInterval = 3000
    this.heartbeatInterval = null
    this.heartbeatTime = 30000
    this.messageQueue = []
    this.pendingMessages = new Map()
    this.messageId = 0
    this.messageTimeout = 5000

    WebSocketManager.instance = this
  }

  setToken(token) {
    this.token = token
  }

  getStatus() {
    return this.status
  }

  setStatus(status) {
    this.status = status
    this.emit('status_change', status)
  }

  connect(token) {
    if (this.status === 'connected') {
      return
    }
    
    if (this.status === 'connecting') {
      return
    }

    this.token = token
    this.setStatus('connecting')

    const wsUrl = `${this.url}?token=${encodeURIComponent(token)}`

    try {
      this.ws = new WebSocket(wsUrl)

      this.ws.onopen = () => {
        this.reconnectAttempts = 0
        this.setStatus('connected')
        this.startHeartbeat()
        this.authenticate()
        this.flushMessageQueue()
      }

      this.ws.onmessage = (event) => {
        this.handleMessage(event.data)
      }

      this.ws.onerror = (error) => {
        this.emit('error', error)
      }

      this.ws.onclose = (event) => {
        this.stopHeartbeat()
        this.handleDisconnect()
      }
    } catch (error) {
      this.setStatus('disconnected')
    }
  }

  authenticate() {
  }

  handleMessage(data) {
    try {
      const message = JSON.parse(data)

      switch (message.type) {
        case 'connected':
          this.emit('connected', message.data)
          break

        case 'pong':
          break

        case 'message':
        case 'new_message':
          this.emit('message', message.data)
          break

        case 'read':
        case 'message_read':
          this.emit('read', message.data)
          break

        case 'message_ack':
          this.handleMessageAck(message.data)
          this.emit('message_ack', message.data)
          break

        case 'typing':
          this.emit('typing', message.data)
          break

        case 'error':
          this.emit('error', message.data)
          break

        case 'auth_success':
          break

        case 'auth_failed':
          this.emit('auth_failed', message.data)
          this.disconnect()
          break
      }
    } catch (error) {
    }
  }

  handleMessageAck(data) {
    const { clientMessageId, messageId, success, error: errorMsg } = data

    let pending = null

    if (clientMessageId) {
      pending = this.pendingMessages.get(clientMessageId)
    }

    if (!pending && messageId) {
      for (const [key, value] of this.pendingMessages.entries()) {
        if (value.messageId === messageId) {
          pending = value
          break
        }
      }
    }

    if (pending) {
      if (pending.timeoutId) {
        clearTimeout(pending.timeoutId)
      }

      if (success) {
        pending.resolve(data)
      } else {
        pending.reject(new Error(errorMsg || 'Message send failed'))
      }

      if (clientMessageId) {
        this.pendingMessages.delete(clientMessageId)
      }
    }
  }

  handleDisconnect() {
    if (this.status === 'disconnected') {
      return
    }

    this.setStatus('disconnected')

    this.pendingMessages.forEach((pending, clientMessageId) => {
      if (pending.timeoutId) {
        clearTimeout(pending.timeoutId)
      }
      pending.reject(new Error('Connection lost'))
    })
    this.pendingMessages.clear()

    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.setStatus('reconnecting')
      this.reconnectAttempts++

      setTimeout(() => {
        if (this.token) {
          this.connect(this.token)
        }
      }, this.reconnectInterval)
    } else {
      this.setStatus('disconnected')
      this.emit('reconnect_failed')
    }
  }

  startHeartbeat() {
    this.stopHeartbeat()
    this.heartbeatInterval = setInterval(() => {
      if (this.status === 'connected') {
        this.send({ 
          type: 'ping',
          timestamp: Date.now()
        })
      }
    }, this.heartbeatTime)
  }

  stopHeartbeat() {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval)
      this.heartbeatInterval = null
    }
  }

  send(data) {
    if (this.status !== 'connected' || !this.ws) {
      if (data.type === 'message') {
        this.messageQueue.push(data)
      }
      return false
    }

    try {
      this.ws.send(JSON.stringify(data))
      return true
    } catch (error) {
      if (data.type === 'message') {
        this.messageQueue.push(data)
      }
      return false
    }
  }

  sendMessage(receiverId, content, messageType = 1, clientMessageId = null) {
    if (!clientMessageId) {
      clientMessageId = `client_${Date.now()}_${++this.messageId}`
    }
    
    const wsMessage = {
      type: 'message',
      data: {
        clientMessageId,
        receiverId,
        content,
        messageType,
        timestamp: Date.now()
      },
      timestamp: Date.now()
    }

    return new Promise((resolve, reject) => {
      const timeoutId = setTimeout(() => {
        this.pendingMessages.delete(clientMessageId)
        reject(new Error('Message send timeout'))
      }, this.messageTimeout)

      this.pendingMessages.set(clientMessageId, {
        resolve,
        reject,
        timeoutId,
        messageId: null,
        content,
        receiverId
      })

      if (!this.send(wsMessage)) {
        clearTimeout(timeoutId)
        this.pendingMessages.delete(clientMessageId)

        if (this.status === 'disconnected') {
          this.messageQueue.push(wsMessage)
          reject(new Error('WebSocket not connected, message queued'))
        } else {
          reject(new Error('Failed to send message'))
        }
      }
    })
  }

  sendReadReceipt(userId) {
    const message = {
      type: 'read',
      data: {
        userId,
        timestamp: Date.now()
      }
    }
    this.send(message)
  }

  flushMessageQueue() {
    if (this.messageQueue.length > 0 && this.status === 'connected') {
      const queue = [...this.messageQueue]
      this.messageQueue = []

      const messageMessages = queue.filter(msg => msg.type === 'message')
      messageMessages.forEach(msg => {
        const clientMessageId = msg.data.clientMessageId

        const timeoutId = setTimeout(() => {
          this.pendingMessages.delete(clientMessageId)
          const pending = this.pendingMessages.get(clientMessageId)
          if (pending) {
            pending.reject(new Error('Message send timeout'))
          }
        }, this.messageTimeout)

        this.pendingMessages.set(clientMessageId, {
          resolve: () => {},
          reject: () => {},
          timeoutId,
          messageId: null
        })

        this.send(msg)
      })

      const otherMessages = queue.filter(msg => msg.type !== 'message')
      otherMessages.forEach(msg => this.send(msg))
    }
  }

  disconnect() {
    this.stopHeartbeat()
    this.setStatus('disconnected')
    this.reconnectAttempts = this.maxReconnectAttempts

    this.pendingMessages.forEach((pending) => {
      if (pending.timeoutId) {
        clearTimeout(pending.timeoutId)
      }
    })
    this.pendingMessages.clear()

    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
  }

  on(event, callback) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, [])
    }
    this.listeners.get(event).push(callback)
  }

  off(event, callback) {
    if (!this.listeners.has(event)) {
      return
    }

    const callbacks = this.listeners.get(event)
    const index = callbacks.indexOf(callback)
    if (index > -1) {
      callbacks.splice(index, 1)
    }
  }

  emit(event, data) {
    if (!this.listeners.has(event)) {
      return
    }

    this.listeners.get(event).forEach(callback => {
      try {
        callback(data)
      } catch (error) {
      }
    })
  }

  clearListeners() {
    this.listeners.clear()
  }
}

export default new WebSocketManager()
