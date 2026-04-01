<template>
  <div class="feed-container">
    <div class="feed-header">
      <h1 class="feed-title">活动广场</h1>
      <div class="header-actions">
        <el-button type="primary" @click="handlePublishClick" class="publish-button">
          <el-icon><Plus /></el-icon>
          发布动态
        </el-button>
        <div class="message-icon" @click="handleMessageClick">
          <el-badge :value="totalUnreadCount" :hidden="!hasUnreadMessages" type="danger" class="message-badge">
            <el-icon class="icon"><ChatDotRound /></el-icon>
          </el-badge>
        </div>
        <div class="user-avatar" @click="handleAvatarClick">
          <el-avatar :size="40" :src="userStore.userInfo?.avatar || ''">{{ userStore.username?.[0] || '用' }}</el-avatar>
        </div>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="feed-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="推荐" name="recommended">
        <PostList type="recommended" />
      </el-tab-pane>
      <el-tab-pane label="关注" name="following">
        <PostList type="following" />
      </el-tab-pane>
      <el-tab-pane label="热门" name="hot">
        <PostList type="hot" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, ChatDotRound } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { useMessageStore } from '../stores/message'
import { alumniApi } from '../api/alumni'
import websocketManager from '../utils/websocket-manager'
import PostList from '../components/PostList.vue'

const router = useRouter()
const userStore = useUserStore()
const messageStore = useMessageStore()
const activeTab = ref('recommended')

const totalUnreadCount = computed(() => messageStore.totalUnreadCount)
const hasUnreadMessages = computed(() => messageStore.hasUnreadMessages)

const handleMessageClick = () => {
  router.push('/message')
}

const handleNewMessage = (data) => {
  const { senderId } = data
  messageStore.updateUnreadCount(senderId, 1)
}

const handleReadReceipt = (data) => {
  const { userId, lastReadTime } = data
  const previousCount = messageStore.unreadCounts.get(userId) || 0
  if (previousCount > 0) {
    messageStore.decreaseUnreadCountByUserId(userId, previousCount)
  }
}

onMounted(async () => {
  const token = localStorage.getItem('token')
  if (token) {
      messageStore.connectWebSocket(token)
    messageStore.fetchUnreadCounts()

    websocketManager.on('message', handleNewMessage)
    websocketManager.on('read', handleReadReceipt)
  }
})

onUnmounted(() => {
  websocketManager.off('message', handleNewMessage)
  websocketManager.off('read', handleReadReceipt)
})

const handleTabChange = (tabName) => {
  console.log('切换到:', tabName)
}

const handlePublishClick = () => {
  router.push('/post/publish')
}

const handleAvatarClick = async () => {
  try {
    const response = await alumniApi.getCurrentProfile()
    console.log('获取校友信息响应:', response)

    if (response.code === 200 && response.data) {
      if (response.data.id) {
        const alumniId = response.data.id
        console.log('使用校友ID:', alumniId)
        router.push(`/profile/${alumniId}`)
      } else {
        console.log('校友信息中没有id字段，使用store中的用户ID:', userStore.userId)
        router.push(`/profile/${userStore.userId}`)
      }
    } else {
      console.log('获取校友信息失败，使用store中的用户ID:', userStore.userId)
      router.push(`/profile/${userStore.userId}`)
    }
  } catch (error) {
    console.error('获取校友信息失败:', error)
    console.log('发生错误，使用store中的用户ID:', userStore.userId)
    router.push(`/profile/${userStore.userId}`)
  }
}
</script>

<style scoped>
.feed-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.feed-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.feed-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.publish-button {
  display: flex;
  align-items: center;
  gap: 6px;
}

.user-avatar {
  cursor: pointer;
  transition: transform 0.3s;
}

.user-avatar:hover {
  transform: scale(1.05);
}

.message-icon {
  cursor: pointer;
  transition: transform 0.3s;
}

.message-icon:hover {
  transform: scale(1.05);
}

.message-badge {
  font-size: 20px;
  color: #909399;
  transition: color 0.3s;
}

.message-badge:hover {
  color: #409eff;
}

.message-badge .icon {
  font-size: 20px;
}

.feed-tabs {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.feed-tabs :deep(.el-tabs__header) {
  margin: 0;
  padding: 0 20px;
  border-bottom: 1px solid #e4e7ed;
}

.feed-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.feed-tabs :deep(.el-tabs__item) {
  height: 50px;
  line-height: 50px;
  font-size: 16px;
  color: #606266;
}

.feed-tabs :deep(.el-tabs__item.is-active) {
  color: #409eff;
  font-weight: bold;
}

.feed-tabs :deep(.el-tabs__active-bar) {
  background-color: #409eff;
}

.feed-tabs :deep(.el-tabs__content) {
  padding: 0;
}
</style>