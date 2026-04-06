<template>
  <div class="conversations-page">
    <AppShellBar />
    <div class="conversations-container alumni-page">
    <div class="conversations-header">
      <h2 class="page-title">消息</h2>
      <el-badge :value="totalUnreadCount" :hidden="!hasUnreadMessages" type="danger" class="unread-badge">
        <el-icon class="message-icon"><ChatDotRound /></el-icon>
      </el-badge>
    </div>

    <AlumniOnlyBlur :locked="isUserNonAlumni">
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载中...</span>
    </div>

    <div v-else-if="conversations.length === 0" class="empty-container">
      <el-empty description="暂无会话" />
    </div>

    <div
      v-else
      class="conversations-list"
      v-infinite-scroll="loadMore"
      :infinite-scroll-disabled="loading || !hasMore"
      :infinite-scroll-distance="10"
    >
      <div
        v-for="conversation in conversations"
        :key="conversation.userId"
        class="conversation-item"
        :class="{ 'has-unread': conversation.unreadCount > 0 }"
        @click="handleConversationClick(conversation.userId)"
      >
        <div class="conversation-avatar">
          <ResolvedAvatar :size="50" :src="conversation.avatar || ''" />
          <span v-if="conversation.online" class="online-indicator"></span>
        </div>

        <div class="conversation-content">
          <div class="conversation-header">
            <div class="conversation-name">{{ conversation.realName || conversation.username }}</div>
            <div class="conversation-time">{{ formatTime(conversation.lastMessageTime) }}</div>
          </div>

          <div class="conversation-message">
            <span v-if="conversation.lastMessageType === 1">{{ conversation.lastMessage }}</span>
            <span v-else-if="conversation.lastMessageType === 2">[图片]</span>
            <span v-else-if="conversation.lastMessageType === 3">[文件]</span>
            <span v-else>{{ conversation.lastMessage }}</span>
          </div>
        </div>

        <div class="conversation-unread">
          <el-badge
            :value="conversation.unreadCount"
            :hidden="conversation.unreadCount === 0"
            type="danger"
            class="unread-count"
          />
        </div>
      </div>

      <div v-if="loading" class="loading-more">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载更多...</span>
      </div>

      <div v-if="!hasMore && conversations.length > 0" class="no-more">
        <span>没有更多了</span>
      </div>
    </div>
    </AlumniOnlyBlur>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Loading, ChatDotRound } from '@element-plus/icons-vue'
import { useMessageStore } from '../stores/message'
import { useUserStore } from '../stores/user'
import websocketManager from '../utils/websocket-manager'
import ResolvedAvatar from '../components/ResolvedAvatar.vue'
import AppShellBar from '../components/AppShellBar.vue'
import AlumniOnlyBlur from '../components/AlumniOnlyBlur.vue'

const router = useRouter()
const messageStore = useMessageStore()
const userStore = useUserStore()
const isUserNonAlumni = computed(() => userStore.role === 'USER')

const loading = ref(false)
const hasMore = ref(true)
const currentUserId = computed(() => userStore.userId)

const conversations = computed(() => messageStore.conversations)
const totalUnreadCount = computed(() => messageStore.totalUnreadCount)
const hasUnreadMessages = computed(() => messageStore.hasUnreadMessages)

const formatTime = (time) => {
  if (!time) return ''

  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour

  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  } else {
    return `${date.getMonth() + 1}-${date.getDate()}`
  }
}

const handleConversationClick = async (userId) => {
  router.push(`/message/detail?userId=${userId}`)
}

const loadMore = async () => {
  if (loading.value || !hasMore.value) return

  loading.value = true
  await messageStore.fetchConversations(false)
  hasMore.value = messageStore.hasMoreConversations
  loading.value = false
}

const refreshConversations = async () => {
  loading.value = true
  await messageStore.fetchConversations(true)
  await messageStore.fetchUnreadCounts()
  hasMore.value = messageStore.hasMoreConversations
  loading.value = false
}

const handleNewMessage = (data) => {
  const { senderId, content, timestamp, messageType } = data

  const existingIndex = conversations.value.findIndex(c => c.userId === senderId)

  if (existingIndex !== -1) {
    const updatedConversations = [...conversations.value]
    const conversation = { ...updatedConversations[existingIndex] }

    conversation.lastMessage = content
    conversation.lastMessageTime = timestamp
    conversation.lastMessageType = messageType
    conversation.unreadCount = (conversation.unreadCount || 0) + 1

    updatedConversations.splice(existingIndex, 1)
    updatedConversations.unshift(conversation)

    messageStore.conversations = updatedConversations
    messageStore.totalUnreadCount++
    messageStore.unreadCounts.set(senderId, conversation.unreadCount)
  } else {
    const newConversation = {
      userId: senderId,
      username: data.senderUsername || '',
      realName: data.senderRealName || '',
      avatar: data.senderAvatar || '',
      lastMessage: content,
      lastMessageTime: timestamp,
      lastMessageType: messageType,
      unreadCount: 1,
      online: false
    }

    messageStore.conversations = [newConversation, ...messageStore.conversations]
    messageStore.totalUnreadCount++
    messageStore.unreadCounts.set(senderId, 1)
  }
}

const handleReadReceipt = (data) => {
  const { userId, lastReadTime } = data

  const existingIndex = conversations.value.findIndex(c => c.userId === userId)
  if (existingIndex !== -1) {
    const updatedConversations = [...conversations.value]
    updatedConversations[existingIndex] = {
      ...updatedConversations[existingIndex],
      isRead: true
    }
    messageStore.conversations = updatedConversations
  }
}

const handleMessageAck = (data) => {
  const { receiverId, messageId, success } = data
  if (success) {
    const existingIndex = conversations.value.findIndex(c => c.userId === receiverId)
    if (existingIndex !== -1) {
      const updatedConversations = [...conversations.value]
      const conversation = { ...updatedConversations[existingIndex] }
      conversation.lastMessageTime = Date.now()
      conversation.lastMessageType = 1
      updatedConversations.splice(existingIndex, 1)
      updatedConversations.unshift(conversation)
      messageStore.conversations = updatedConversations
    }
  }
}

onMounted(async () => {
  loading.value = true
  await messageStore.fetchUnreadCounts()
  await messageStore.fetchConversations(true)
  hasMore.value = messageStore.hasMoreConversations
  loading.value = false

  websocketManager.on('message', handleNewMessage)
  websocketManager.on('read', handleReadReceipt)
  websocketManager.on('message_ack', handleMessageAck)
})

onUnmounted(() => {
  websocketManager.off('message', handleNewMessage)
  websocketManager.off('read', handleReadReceipt)
  websocketManager.off('message_ack', handleMessageAck)
})
</script>

<style scoped>
.conversations-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.conversations-container {
  flex: 1;
}

.conversations-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 22px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-subtle);
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--ink);
}

.unread-badge {
  cursor: pointer;
  font-size: 20px;
  color: #909399;
  transition: color 0.3s;
}

.unread-badge:hover {
  color: #409eff;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  color: #909399;
}

.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  color: #909399;
}

.conversations-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 18px;
  background: var(--surface-solid);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-subtle);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.conversation-item:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-card);
}

.conversation-item.has-unread {
  background: linear-gradient(135deg, rgba(79, 70, 229, 0.06) 0%, rgba(14, 165, 233, 0.05) 100%);
  border-color: rgba(79, 70, 229, 0.15);
}

.conversation-avatar {
  position: relative;
  flex-shrink: 0;
}

.online-indicator {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 12px;
  height: 12px;
  background-color: #67c23a;
  border: 2px solid #fff;
  border-radius: 50%;
}

.conversation-content {
  flex: 1;
  min-width: 0;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.conversation-name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
}

.conversation-time {
  font-size: 12px;
  color: #909399;
}

.conversation-message {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.conversation-unread {
  flex-shrink: 0;
}

.unread-count {
  font-size: 12px;
}

.loading-more {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: #909399;
}

.no-more {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: #909399;
  font-size: 14px;
}

@media (max-width: 768px) {
  .conversations-container {
    padding: 10px;
  }

  .conversation-item {
    padding: 12px;
  }

  .conversation-name {
    max-width: 150px;
  }
}
</style>