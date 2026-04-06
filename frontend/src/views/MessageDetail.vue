<template>
  <div class="message-page">
    <AppShellBar />
    <div class="message-detail-container">
    <div class="message-header">
      <el-button type="text" @click="goBack" class="back-button">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <div class="user-info">
        <el-avatar :size="40" :src="avatarUrl" class="header-avatar">
          <img v-if="avatarUrl" :src="avatarUrl" alt="头像" />
          <span v-else>{{ userInfo.realName?.charAt(0) || userInfo.username?.charAt(0) || '用' }}</span>
        </el-avatar>
        <span class="user-name">{{ userInfo.realName || userInfo.username }}</span>
        <span v-if="userInfo.online" class="online-status">在线</span>
      </div>
      <el-button type="text" class="more-button">
        <el-icon><More /></el-icon>
      </el-button>
    </div>

    <AlumniOnlyBlur :locked="isUserNonAlumni">
    <div
      class="messages-list"
      ref="messagesListRef"
      v-infinite-scroll="loadMore"
      :infinite-scroll-disabled="loading || !hasMore || isUserNonAlumni"
      :infinite-scroll-distance="10"
    >
      <div v-if="loading && messages.length === 0" class="loading-container">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <div v-else-if="messages.length === 0" class="empty-container">
        <el-empty description="暂无消息" />
      </div>

      <div v-else class="messages-content">
        <MessageBubble
          v-for="message in messages"
          :key="message.clientMessageId || message.id"
          :is-self="message.isSelf"
          :content="message.content"
          :created-at="message.createdAt"
          :is-read="message.isRead"
          :avatar="message.isSelf ? userStore.userInfo?.avatar : message.senderAvatar"
          :status="message.status"
          :show-status="message.isSelf"
          @retry="retryMessage(message)"
        />
      </div>

      <div v-if="loading && messages.length > 0" class="loading-more">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载更多...</span>
      </div>
    </div>

    <div class="new-message-tip" v-if="showNewMessageTip" @click="scrollToBottom">
      有新消息，点击查看
    </div>

    <div class="message-input-container">
      <el-input
        v-model="messageContent"
        placeholder="输入消息..."
        type="textarea"
        :rows="1"
        resize="none"
        class="message-input"
        :disabled="isUserNonAlumni"
        @keyup.enter.exact="sendMessage"
        @keydown.shift.enter.prevent="handleShiftEnter"
      />
      <el-button
        type="primary"
        @click="sendMessage"
        class="send-button"
        :disabled="!messageContent.trim() || sending || isUserNonAlumni"
      >
        发送
      </el-button>
    </div>
    </AlumniOnlyBlur>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, More, Loading } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { useMessageStore } from '../stores/message'
import { messageApi } from '../api/message'
import { resolveAvatarUrl } from '../utils/avatarUrl'
import websocketManager from '../utils/websocket-manager'
import MessageBubble from '../components/MessageBubble.vue'
import AppShellBar from '../components/AppShellBar.vue'
import AlumniOnlyBlur from '../components/AlumniOnlyBlur.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const messageStore = useMessageStore()
const isUserNonAlumni = computed(() => userStore.role === 'USER')

const messageContent = ref('')
const messages = ref([])
const userInfo = ref({
  userId: route.query.userId,
  username: '',
  realName: '',
  avatar: '',
  online: false
})
const avatarUrl = ref('')
const loading = ref(false)
const hasMore = ref(true)
const pageNum = ref(1)
const pageSize = ref(20)
const messagesListRef = ref(null)
const sending = ref(false)
const showNewMessageTip = ref(false)
const currentUserId = ref(route.query.userId)
let clientMessageIdCounter = 0

const getClientMessageId = () => {
  return `client_${Date.now()}_${++clientMessageIdCounter}`
}

const goBack = () => {
  router.push('/message')
}

const handleShiftEnter = (e) => {
  e.preventDefault()
  const start = e.target.selectionStart
  const end = e.target.selectionEnd
  const value = e.target.value
  messageContent.value = value.substring(0, start) + '\n' + value.substring(end)
  nextTick(() => {
    e.target.selectionStart = e.target.selectionEnd = start + 1
  })
}

const loadMore = async () => {
  if (loading.value || !hasMore.value) return
  await fetchMessages(false)
}

const fetchMessages = async (refresh = false) => {
  if (loading.value) return

  if (refresh) {
    pageNum.value = 1
    messages.value = []
    hasMore.value = true
  }

  loading.value = true

  try {
    const userId = currentUserId.value
    const response = await messageApi.getMessages(userId, pageNum.value, pageSize.value)

    if (response.code === 200) {
      const { list, total, pageNum: currentPage, pageSize: currentSize } = response.data

      const formattedMessages = list.map(msg => ({
        ...msg,
        isSelf: msg.isSelf,
        createdAt: msg.createdAt,
        status: 'sent'
      }))

      if (refresh) {
        messages.value = formattedMessages
      } else {
        messages.value = [...messages.value, ...formattedMessages]
      }

      pageNum.value = currentPage + 1
      hasMore.value = messages.value.length < total

      if (refresh) {
        await nextTick()
        scrollToBottom()
      }

      if (formattedMessages.length > 0 && (!userInfo.value.realName && !userInfo.value.username)) {
        let targetMessage = formattedMessages.find(msg => !msg.isSelf)

        if (!targetMessage) {
          targetMessage = formattedMessages[0]
          if (targetMessage) {
            userInfo.value = {
              userId: targetMessage.receiverId,
              username: targetMessage.receiverUsername,
              realName: targetMessage.receiverRealName,
              avatar: targetMessage.receiverAvatar
            }
          }
        } else {
          userInfo.value = {
            userId: targetMessage.senderId,
            username: targetMessage.senderUsername,
            realName: targetMessage.senderRealName,
            avatar: targetMessage.senderAvatar
          }
        }
        
        if (userInfo.value.avatar) {
          avatarUrl.value = await resolveAvatarUrl(userInfo.value.avatar)
        }
      }
    }
  } catch (error) {
    console.error('获取消息列表失败:', error)
    ElMessage.error('获取消息列表失败，请重试')
  } finally {
    loading.value = false
  }
}

const sendMessage = async () => {
  if (userStore.role === 'USER') {
    ElMessage.warning('请先完成校友认证，认证后可发私信')
    return
  }
  if (!messageContent.value.trim() || sending.value) {
    return
  }

  const content = messageContent.value.trim()
  const receiverId = currentUserId.value

  const clientMessageId = getClientMessageId()
  const tempMessage = {
    clientMessageId,
    senderId: userStore.userId,
    senderUsername: userStore.username,
    senderRealName: userStore.userInfo?.realName,
    senderAvatar: userStore.userInfo?.avatar,
    receiverId: receiverId,
    receiverUsername: userInfo.value.username,
    receiverRealName: userInfo.value.realName,
    receiverAvatar: userInfo.value.avatar,
    content: content,
    messageType: 1,
    isRead: false,
    isSelf: true,
    createdAt: new Date().toISOString(),
    status: 'sending'
  }

  messages.value.push(tempMessage)
  messageContent.value = ''
  sending.value = true

  await nextTick()
  scrollToBottom()

  try {
    let result
    try {
      result = await websocketManager.sendMessage(receiverId, content, 1, clientMessageId)
    } catch (wsError) {
      console.log('WebSocket failed, using HTTP API')
      const httpResponse = await messageApi.sendMessage(receiverId, content, 1)
      if (httpResponse.code === 200) {
        result = { messageId: httpResponse.data, success: true }
      } else {
        throw new Error(httpResponse.message || 'HTTP 发送失败')
      }
    }

    const messageIndex = messages.value.findIndex(m => m.clientMessageId === clientMessageId)
    if (messageIndex !== -1) {
      messages.value[messageIndex] = {
        ...messages.value[messageIndex],
        id: result.messageId || result.id,
        status: 'sent'
      }
    }
  } catch (error) {
    console.error('发送消息失败:', error)

    const messageIndex = messages.value.findIndex(m => m.clientMessageId === clientMessageId)
    if (messageIndex !== -1) {
      messages.value[messageIndex].status = 'failed'
    }

    ElMessage.error('发送消息失败')
  } finally {
    sending.value = false
  }
}

const retryMessage = async (message) => {
  if (userStore.role === 'USER') {
    ElMessage.warning('请先完成校友认证')
    return
  }
  if (message.status !== 'failed') return

  const index = messages.value.findIndex(m => m.clientMessageId === message.clientMessageId)
  if (index === -1) return

  messages.value[index].status = 'sending'

  try {
    let result
    try {
      result = await websocketManager.sendMessage(message.receiverId, message.content, message.messageType, message.clientMessageId)
    } catch (wsError) {
      console.log('WebSocket failed, using HTTP API')
      const httpResponse = await messageApi.sendMessage(message.receiverId, message.content, message.messageType)
      if (httpResponse.code === 200) {
        result = { messageId: httpResponse.data, success: true }
      } else {
        throw new Error(httpResponse.message || 'HTTP 发送失败')
      }
    }

    messages.value[index] = {
      ...messages.value[index],
      id: result.messageId || result.id,
      status: 'sent'
    }
  } catch (error) {
    console.error('重试发送消息失败:', error)
    messages.value[index].status = 'failed'
    ElMessage.error('发送消息失败')
  }
}

const handleNewMessage = (data) => {
  try {
    const senderId = data.senderId
    const myUserId = Number(userStore.userId)
    const isSelfMessage = Number(senderId) === myUserId

  if (isSelfMessage) {
    const existingIndex = messages.value.findIndex(m => m.clientMessageId === data.clientMessageId)
    if (existingIndex !== -1) {
      messages.value[existingIndex] = {
        ...data,
        isSelf: true,
        status: 'sent'
      }
    }
  } else {
    messages.value.push({
      ...data,
      isSelf: false,
      status: 'received'
    })

    nextTick(() => {
      scrollToBottom()
    })

    markAsRead()
  }
  } catch (error) {
    console.error('handleNewMessage error:', error)
  }
}

const handleReadReceipt = (data) => {
  const { userId, lastReadTime } = data

  if (userId === currentUserId.value) {
    messages.value.forEach(msg => {
      if (msg.isSelf && msg.createdAt <= lastReadTime) {
        msg.isRead = true
      }
    })
  }
}

const handleMessageAck = (data) => {
  const { messageId, success, error: errorMsg } = data

  const messageIndex = messages.value.findIndex(m => m.id === messageId || m.clientMessageId === messageId)
  if (messageIndex !== -1) {
    if (success) {
      messages.value[messageIndex].status = 'sent'
    } else {
      messages.value[messageIndex].status = 'failed'
      ElMessage.error(errorMsg || '发送失败')
    }
  }
}

const scrollToBottom = () => {
  if (messagesListRef.value) {
    messagesListRef.value.scrollTop = messagesListRef.value.scrollHeight
    showNewMessageTip.value = false
  }
}

const checkIfAtBottom = () => {
  if (!messagesListRef.value) return true
  const { scrollTop, scrollHeight, clientHeight } = messagesListRef.value
  return scrollHeight - scrollTop - clientHeight < 100
}

const markAsRead = async () => {
  try {
    const userId = currentUserId.value
    await messageApi.markAsRead(userId)

    messages.value = messages.value.map(msg => {
      if (!msg.isSelf) {
        return { ...msg, isRead: true }
      }
      return msg
    })

    websocketManager.sendReadReceipt(userId)

    await messageStore.fetchUnreadCounts()
  } catch (error) {
    console.error('标记消息为已读失败:', error)
  }
}

const handleRouteChange = async (newUserId) => {
  if (!newUserId) return

  currentUserId.value = newUserId

  userInfo.value = {
    userId: newUserId,
    username: '',
    realName: '',
    avatar: '',
    online: false
  }

  await fetchMessages(true)
  await markAsRead()
}

watch(() => route.query.userId, (newUserId) => {
  if (newUserId && newUserId !== currentUserId.value) {
    handleRouteChange(newUserId)
  }
})

onMounted(async () => {
  await fetchMessages(true)
  await markAsRead()
  
  nextTick(() => {
    scrollToBottom()
  })

  websocketManager.on('message', handleNewMessage)
  websocketManager.on('read', handleReadReceipt)
  websocketManager.on('message_ack', handleMessageAck)

  messagesListRef.value?.addEventListener('scroll', () => {
    if (checkIfAtBottom()) {
      showNewMessageTip.value = false
    }
  })
})

onUnmounted(() => {
  websocketManager.off('message', handleNewMessage)
  websocketManager.off('read', handleReadReceipt)
  websocketManager.off('message_ack', handleMessageAck)
})
</script>

<style scoped>
.message-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.message-detail-container {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  height: calc(100vh - var(--shell-h));
  max-width: var(--content-max);
  margin: 0 auto;
  width: 100%;
  position: relative;
  background: transparent;
}

.message-header {
  display: flex;
  align-items: center;
  padding: 14px 20px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--border-subtle);
  box-shadow: var(--shadow-sm);
}

.back-button {
  margin-right: 16px;
}

.user-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-avatar {
  flex-shrink: 0;
}

.user-name {
  font-size: 16px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--ink);
}

.online-status {
  font-size: 12px;
  color: #67c23a;
  padding: 2px 8px;
  background-color: #f0f9eb;
  border-radius: 10px;
}

.more-button {
  margin-left: 16px;
}

.messages-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #909399;
}

.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  color: #909399;
}

.messages-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.loading-more {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px 0;
  color: #909399;
}

.new-message-tip {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: rgba(0, 0, 0, 0.6);
  color: #fff;
  padding: 8px 16px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  z-index: 100;
}

.new-message-tip:hover {
  background-color: rgba(0, 0, 0, 0.8);
}

.message-input-container {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding: 16px 20px 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  border-top: 1px solid var(--border-subtle);
  box-shadow: 0 -8px 24px rgba(12, 18, 34, 0.06);
}

.message-input {
  flex: 1;
  min-height: 40px;
  max-height: 120px;
  border-radius: 20px;
  padding: 10px 16px;
  resize: none;
}

.send-button {
  flex-shrink: 0;
  border-radius: 20px;
  padding: 0 20px;
  height: 40px;
}

@media (max-width: 768px) {
  .message-header {
    padding: 10px 16px;
  }

  .messages-list {
    padding: 16px;
  }

  .message-input-container {
    padding: 12px;
  }
}
</style>