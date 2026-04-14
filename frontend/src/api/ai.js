import service from './request'

export const aiApi = {
  chat: (message, chatId, useRag = true) => {
    return service.post('/ai/chat', { message, chatId, useRag }, { timeout: 60000 })
  },
  chatStream: async (message, chatId, useRag = true, onEvent) => {
    const token = localStorage.getItem('token') || ''
    const res = await fetch('/api/ai/chat/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {})
      },
      body: JSON.stringify({ message, chatId, useRag })
    })

    if (!res.ok) {
      let text = ''
      try {
        text = await res.text()
      } catch {}
      throw new Error(text || `请求失败 (${res.status})`)
    }

    const reader = res.body?.getReader()
    if (!reader) throw new Error('浏览器不支持流式读取')

    const decoder = new TextDecoder('utf-8')
    let buffer = ''

    const emit = (evt) => {
      if (typeof onEvent === 'function') onEvent(evt)
    }

    while (true) {
      const { value, done } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })

      // SSE frame split by blank line
      let idx
      while ((idx = buffer.indexOf('\n\n')) !== -1) {
        const raw = buffer.slice(0, idx)
        buffer = buffer.slice(idx + 2)
        const lines = raw.split('\n').map((l) => l.trimEnd())
        let event = 'message'
        const dataLines = []
        for (const line of lines) {
          if (line.startsWith('event:')) event = line.slice(6).trim()
          else if (line.startsWith('data:')) dataLines.push(line.slice(5).trimStart())
        }
        const data = dataLines.join('\n')
        if (data) emit({ event, data })
      }
    }
  }
}

export default aiApi

