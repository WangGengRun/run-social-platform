<template>
  <div
    class="ai-fab"
    :style="{ left: `${pos.x}px`, top: `${pos.y}px` }"
    @pointerdown="onPointerDown"
    @click="onFabClick"
    role="button"
    aria-label="AI校友助手"
  >
    <div class="ai-fab-inner" :class="{ dragging }">
      <span class="ai-fab-icon">🤖</span>
    </div>
  </div>

  <el-dialog
    v-model="visible"
    title="AI校友助手"
    :modal="false"
    :lock-scroll="false"
    :close-on-click-modal="false"
    :append-to-body="true"
    class="ai-float-dialog"
  >
    <div class="ai-chat">
      <div ref="listRef" class="ai-msg-list">
        <div v-for="(m, idx) in messages" :key="idx" class="ai-msg" :class="m.role">
          <div class="ai-bubble">
            <div class="ai-role">{{ m.role === 'user' ? '我' : '助手' }}</div>
            <div class="ai-text">{{ m.content }}</div>
          </div>
        </div>
        <div v-if="loading" class="ai-typing">助手思考中...</div>
      </div>

      <div class="ai-input-row">
        <el-input
          v-model="input"
          type="textarea"
          :rows="1"
          resize="none"
          placeholder="问我：平台规则、活动报名、动态发布等..."
          @keyup.enter.exact="send"
          @keydown.shift.enter.prevent
        />
        <el-button type="primary" :loading="loading" :disabled="!input.trim()" @click="send">发送</el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { aiApi } from '../api/ai'

const visible = ref(false)
const dragging = ref(false)
const justDragged = ref(false)

const pos = reactive({ x: 0, y: 0 })
const pointerStart = reactive({ x: 0, y: 0, px: 0, py: 0 })

const clamp = (v, min, max) => Math.min(max, Math.max(min, v))

const initPos = () => {
  const vw = window.innerWidth
  const vh = window.innerHeight
  const size = 56
  pos.x = clamp(vw - size - 20, 8, vw - size - 8)
  pos.y = clamp(vh - size - 160, 80, vh - size - 8)
}

const onPointerDown = (e) => {
  // 只支持主键拖拽
  if (e.button !== 0) return
  dragging.value = true
  justDragged.value = false
  pointerStart.px = e.clientX
  pointerStart.py = e.clientY
  pointerStart.x = pos.x
  pointerStart.y = pos.y
  e.currentTarget?.setPointerCapture?.(e.pointerId)
  window.addEventListener('pointermove', onPointerMove)
  window.addEventListener('pointerup', onPointerUp, { once: true })
}

const onPointerMove = (e) => {
  if (!dragging.value) return
  const dx = e.clientX - pointerStart.px
  const dy = e.clientY - pointerStart.py
  if (Math.abs(dx) + Math.abs(dy) > 4) justDragged.value = true

  const size = 56
  const vw = window.innerWidth
  const vh = window.innerHeight
  pos.x = clamp(pointerStart.x + dx, 8, vw - size - 8)
  pos.y = clamp(pointerStart.y + dy, 80, vh - size - 8)
}

const onPointerUp = () => {
  dragging.value = false
  window.removeEventListener('pointermove', onPointerMove)
  // 防止拖拽结束触发 click
  setTimeout(() => {
    justDragged.value = false
  }, 0)
}

const onFabClick = () => {
  if (justDragged.value) return
  visible.value = true
  nextTick(() => scrollToBottom())
}

const input = ref('')
const loading = ref(false)
const listRef = ref(null)
const chatId = ref('')

const messages = ref([
  { role: 'assistant', content: '你好，我是AI校友助手。你可以问我平台规则、活动报名、动态发布等问题。' }
])

const scrollToBottom = async () => {
  await nextTick()
  const el = listRef.value
  if (!el) return
  el.scrollTop = el.scrollHeight
}

const ensureChatId = () => {
  if (chatId.value) return chatId.value
  const key = 'ai_chat_id'
  const stored = localStorage.getItem(key)
  if (stored) {
    chatId.value = stored
    return stored
  }
  const v = (globalThis.crypto && crypto.randomUUID) ? crypto.randomUUID() : `chat_${Date.now()}_${Math.random().toString(16).slice(2)}`
  chatId.value = v
  localStorage.setItem(key, v)
  return v
}

const send = async () => {
  const text = input.value.trim()
  if (!text || loading.value) return

  messages.value.push({ role: 'user', content: text })
  input.value = ''
  loading.value = true
  await scrollToBottom()

  const pushAssistant = (content) => {
    messages.value.push({ role: 'assistant', content })
  }

  try {
    const cid = ensureChatId()
    // 先插入一个“流式气泡”，后续分片不断追加
    const streamingIndex = messages.value.length
    pushAssistant('')

    const runStream = async (useRag) => {
      await aiApi.chatStream(text, cid, useRag, (evt) => {
        if (evt.event === 'meta') {
          try {
            const meta = JSON.parse(evt.data)
            if (meta?.chatId && meta.chatId !== chatId.value) {
              chatId.value = meta.chatId
              localStorage.setItem('ai_chat_id', meta.chatId)
            }
          } catch {}
          return
        }
        if (evt.event === 'delta') {
          const current = messages.value[streamingIndex]
          if (current && current.role === 'assistant') {
            current.content = String(current.content || '') + evt.data
          }
          return
        }
        if (evt.event === 'error') {
          throw new Error(evt.data || 'AI助手暂时不可用')
        }
      })
    }

    try {
      await runStream(true)
    } catch (ragError) {
      console.warn('RAG stream failed, fallback to normal stream:', ragError)
      // 清空上一次失败的内容，避免混杂
      if (messages.value[streamingIndex]) messages.value[streamingIndex].content = ''
      await runStream(false)
    }

    // 兜底：如果流式结束但没内容，给一个默认提示
    if (!String(messages.value[streamingIndex]?.content || '').trim()) {
      messages.value[streamingIndex].content = '我刚刚没想好，可以换个问法再试试～'
    }
  } catch (e) {
    console.error('AI chat failed:', e)
    // axios 拦截器会把后端 message 包装为 Error(message)
    ElMessage.error(e?.message || 'AI助手暂时不可用，请稍后重试')
    pushAssistant(`（失败）${e?.message || 'AI助手暂时不可用，请稍后重试'}`)
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

const onResize = () => {
  const size = 56
  const vw = window.innerWidth
  const vh = window.innerHeight
  pos.x = clamp(pos.x, 8, vw - size - 8)
  pos.y = clamp(pos.y, 80, vh - size - 8)
}

onMounted(() => {
  initPos()
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  window.removeEventListener('pointermove', onPointerMove)
})
</script>

<style scoped>
.ai-fab {
  position: fixed;
  width: 56px;
  height: 56px;
  z-index: 1200;
  touch-action: none;
}

.ai-fab-inner {
  width: 56px;
  height: 56px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #4f46e5, #0ea5e9);
  box-shadow: 0 14px 34px rgba(79, 70, 229, 0.28);
  border: 1px solid rgba(255, 255, 255, 0.55);
  cursor: grab;
  user-select: none;
  transition: transform 0.12s ease;
}

.ai-fab-inner.dragging {
  cursor: grabbing;
  transform: scale(1.03);
}

.ai-fab-icon {
  font-size: 22px;
  line-height: 1;
}

/* 悬浮对话框：不遮罩，不占满屏 */
:deep(.ai-float-dialog .el-dialog) {
  position: fixed;
  right: 18px;
  bottom: 88px;
  margin: 0 !important;
  width: min(92vw, 380px);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 18px 60px rgba(12, 18, 34, 0.22);
  border: 1px solid rgba(12, 18, 34, 0.08);
}

:deep(.ai-float-dialog .el-dialog__header) {
  margin-right: 0;
  padding: 14px 16px 10px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(12, 18, 34, 0.06);
}

:deep(.ai-float-dialog .el-dialog__body) {
  padding: 0;
}

.ai-chat {
  display: flex;
  flex-direction: column;
  height: 520px;
  background: rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(10px);
}

.ai-msg-list {
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
  padding: 14px 14px 10px;
}

.ai-msg {
  display: flex;
  margin-bottom: 10px;
}

.ai-msg.user {
  justify-content: flex-end;
}

.ai-msg.assistant {
  justify-content: flex-start;
}

.ai-bubble {
  max-width: 86%;
  padding: 10px 12px;
  border-radius: 14px;
  border: 1px solid rgba(12, 18, 34, 0.08);
  background: #fff;
}

.ai-msg.user .ai-bubble {
  background: rgba(79, 70, 229, 0.08);
  border-color: rgba(79, 70, 229, 0.16);
}

.ai-role {
  font-size: 11px;
  font-weight: 700;
  color: rgba(12, 18, 34, 0.55);
  margin-bottom: 4px;
}

.ai-text {
  white-space: pre-wrap;
  word-break: break-word;
  color: rgba(12, 18, 34, 0.88);
  line-height: 1.55;
  font-size: 13.5px;
  font-weight: 600;
}

.ai-typing {
  padding: 10px 12px;
  color: rgba(12, 18, 34, 0.55);
  font-size: 12px;
  font-weight: 700;
}

.ai-input-row {
  display: flex;
  gap: 10px;
  padding: 12px;
  border-top: 1px solid rgba(12, 18, 34, 0.06);
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
}

.ai-input-row :deep(.el-textarea__inner) {
  border-radius: 12px;
}
</style>

