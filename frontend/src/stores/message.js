import { defineStore } from 'pinia'
import { messageApi } from '../api/message'
import websocketManager from '../utils/websocket-manager'

export const useMessageStore = defineStore('message', {
  state: () => ({
    wsStatus: 'disconnected',
    unreadCounts: new Map(),
    totalUnreadCount: 0,
    conversations: [],
    conversationsLoading: false,
    hasMoreConversations: true,
    conversationsPageNum: 1,
    conversationsPageSize: 20,
    messages: new Map(),
    messagesPageNum: new Map(),
    messagesPageSize: 20,
    hasMoreMessages: new Map()
  }),

  getters: {
    hasUnreadMessages: (state) => {
      return state.totalUnreadCount > 0
    },

    getUnreadCount: (state) => {
      return (userId) => state.unreadCounts.get(userId) || 0
    },

    getMessages: (state) => {
      return (userId) => state.messages.get(userId) || []
    }
  },

  actions: {
    connectWebSocket(token) {
      this.wsStatus = 'connecting'

      websocketManager.setToken(token)

      websocketManager.on('status_change', (status) => {
        this.wsStatus = status
      })

      websocketManager.on('message', (data) => {
        this.handleNewMessage(data)
      })

      websocketManager.on('read', (data) => {
        this.handleReadReceipt(data)
      })

      websocketManager.on('error', (error) => {
        console.error('WebSocket error:', error)
      })

      websocketManager.on('auth_failed', (data) => {
        console.error('WebSocket authentication failed:', data)
        this.disconnectWebSocket()
      })

      websocketManager.on('reconnect_failed', () => {
        console.error('WebSocket reconnection failed')
      })

      websocketManager.connect(token)
    },

    disconnectWebSocket() {
      websocketManager.disconnect()
      websocketManager.clearListeners()
      this.wsStatus = 'disconnected'
    },

    handleNewMessage(data) {
      const senderId = data.senderId
      const content = data.content
      
      const timestamp = data.createdAt ? new Date(data.createdAt).getTime() : (data.timestamp || Date.now())
      
      const message = {
        id: data.id || Date.now(),
        senderId,
        content,
        timestamp,
        messageType: data.messageType || 1,
        isRead: data.isRead || false,
        isMine: false,
        avatar: data.senderAvatar,
        username: data.senderUsername
      }

      if (!this.messages.has(senderId)) {
        this.messages.set(senderId, [])
      }
      this.messages.get(senderId).push(message)

      this.updateUnreadCount(senderId, 1)
    },

    handleReadReceipt(data) {
      const { userId, lastReadTime } = data
      const userMessages = this.messages.get(userId)
      if (userMessages) {
        userMessages.forEach(msg => {
          if (!msg.isMine && msg.timestamp <= lastReadTime) {
            msg.isRead = true
          }
        })
      }

      const previousCount = this.unreadCounts.get(userId) || 0
      if (previousCount > 0) {
        this.decreaseUnreadCountByUserId(userId, previousCount)
      }
    },

    updateUnreadCount(userId, increment) {
      const currentCount = this.unreadCounts.get(userId) || 0
      const newCount = currentCount + increment
      this.unreadCounts.set(userId, newCount)
      this.totalUnreadCount = Math.max(0, this.totalUnreadCount + increment)
    },

    decreaseUnreadCountByUserId(userId, decrement) {
      const currentCount = this.unreadCounts.get(userId) || 0
      const newCount = Math.max(0, currentCount - decrement)
      this.unreadCounts.set(userId, newCount)
      this.totalUnreadCount = Math.max(0, this.totalUnreadCount - decrement)
    },

    recalcTotalUnreadCount() {
      let total = 0
      this.unreadCounts.forEach((count) => {
        total += count
      })
      this.totalUnreadCount = total
    },

    async sendMessage(receiverId, content, messageType = 1) {
      try {
        const result = await websocketManager.sendMessage(receiverId, content, messageType)

        const message = {
          id: result.messageId || Date.now(),
          senderId: 'me',
          receiverId,
          content,
          timestamp: Date.now(),
          messageType,
          isRead: false,
          isMine: true,
          sendStatus: 'sent'
        }

        if (!this.messages.has(receiverId)) {
          this.messages.set(receiverId, [])
        }
        this.messages.get(receiverId).push(message)

        return message
      } catch (error) {
        console.error('Failed to send message:', error)
        throw error
      }
    },

    async markAsRead(userId) {
      try {
        await messageApi.markAsRead(userId)

        const userMessages = this.messages.get(userId)
        if (userMessages) {
          let count = 0
          userMessages.forEach(msg => {
            if (!msg.isMine && !msg.isRead) {
              msg.isRead = true
              count++
            }
          })

          if (count > 0) {
            this.decreaseUnreadCountByUserId(userId, count)
          }
        }

        websocketManager.sendReadReceipt(userId)
      } catch (error) {
        console.error('Failed to mark as read:', error)
      }
    },

    async fetchUnreadCounts() {
      try {
        const response = await messageApi.getUnreadCount()
        if (response.code === 200) {
          const counts = response.data.counts || []
          this.totalUnreadCount = response.data.totalUnreadCount || 0

          counts.forEach(item => {
            this.unreadCounts.set(item.userId, item.count)
          })
        }
      } catch (error) {
        console.error('Failed to fetch unread counts:', error)
      }
    },

    async fetchConversations(refresh = false) {
      if (this.conversationsLoading) return

      if (refresh) {
        this.conversationsPageNum = 1
        this.conversations = []
        this.hasMoreConversations = true
      }

      if (!this.hasMoreConversations) return

      this.conversationsLoading = true

      try {
        const response = await messageApi.getConversations(
          this.conversationsPageNum,
          this.conversationsPageSize
        )

        if (response.code === 200) {
          const { list, total, pageNum, pageSize } = response.data

          if (refresh) {
            this.conversations = list
          } else {
            this.conversations = [...this.conversations, ...list]
          }

          list.forEach(conv => {
            this.unreadCounts.set(conv.userId, conv.unreadCount || 0)
          })

          this.conversationsPageNum = pageNum + 1
          this.hasMoreConversations = list.length === pageSize
        }
      } catch (error) {
        console.error('Failed to fetch conversations:', error)
      } finally {
        this.conversationsLoading = false
      }
    },

    async fetchMessages(userId, refresh = false) {
      if (refresh) {
        this.messagesPageNum.set(userId, 1)
        this.messages.set(userId, [])
        this.hasMoreMessages.set(userId, true)
      }

      const pageNum = this.messagesPageNum.get(userId) || 1
      const hasMore = this.hasMoreMessages.get(userId) !== false

      if (!hasMore) return

      try {
        const response = await messageApi.getMessages(
          userId,
          pageNum,
          this.messagesPageSize
        )

        if (response.code === 200) {
          const { list, pageNum: newPageNum, pageSize } = response.data

          const formattedMessages = list.map(msg => ({
            id: msg.id,
            senderId: msg.senderId,
            receiverId: msg.receiverId,
            content: msg.content,
            timestamp: msg.createdAt,
            messageType: msg.messageType,
            isRead: msg.isRead,
            isMine: msg.senderId === 'me'
          }))

          if (refresh) {
            this.messages.set(userId, formattedMessages)
          } else {
            const existing = this.messages.get(userId) || []
            this.messages.set(userId, [...existing, ...formattedMessages])
          }

          this.messagesPageNum.set(userId, newPageNum + 1)
          this.hasMoreMessages.set(userId, list.length === pageSize)
        }
      } catch (error) {
        console.error('Failed to fetch messages:', error)
      }
    },

    resetConversations() {
      this.conversations = []
      this.conversationsPageNum = 1
      this.hasMoreConversations = true
    },

    resetMessages(userId) {
      this.messages.delete(userId)
      this.messagesPageNum.delete(userId)
      this.hasMoreMessages.delete(userId)
    },

    updateUnreadCountTotal(count) {
      this.totalUnreadCount = count
    },

    decreaseUnreadCount(count) {
      this.totalUnreadCount = Math.max(0, this.totalUnreadCount - count)
    },

    resetUnreadCounts() {
      this.unreadCounts = new Map()
      this.totalUnreadCount = 0
    }
  }
})
