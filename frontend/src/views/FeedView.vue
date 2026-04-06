<template>
  <div class="feed-page">
    <AppShellBar />

    <main class="feed-main alumni-page">
      <header class="feed-hero">
        <div class="feed-hero-row">
          <h1 class="feed-title">动态圈</h1>
          <p class="feed-desc">校友动态广场</p>
        </div>
      </header>

      <div class="tabs-panel">
        <el-tabs v-model="activeTab" class="premium-tabs" @tab-change="handleTabChange">
          <el-tab-pane label="全部" name="recommended">
            <div class="feed-with-sidebar">
              <div class="feed-left">
                <PostList type="recommended" />
              </div>
              <div class="feed-right">
                <HotPostsPanel />
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane v-if="showFollowingTab" label="关注" name="following">
            <PostList type="following" />
          </el-tab-pane>
        </el-tabs>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useMessageStore } from '../stores/message'
import { useUserStore } from '../stores/user'
import websocketManager from '../utils/websocket-manager'
import PostList from '../components/PostList.vue'
import AppShellBar from '../components/AppShellBar.vue'
import HotPostsPanel from '../components/HotPostsPanel.vue'

const messageStore = useMessageStore()
const userStore = useUserStore()
const activeTab = ref('recommended')
const showFollowingTab = computed(() => userStore.role === 'ALUMNI')

const handleNewMessage = (data) => {
  const { senderId } = data
  messageStore.updateUnreadCount(senderId, 1)
}

const handleReadReceipt = (data) => {
  const { userId } = data
  const previousCount = messageStore.unreadCounts.get(userId) || 0
  if (previousCount > 0) {
    messageStore.decreaseUnreadCountByUserId(userId, previousCount)
  }
}

onMounted(() => {
  const token = localStorage.getItem('token')
  if (token && userStore.role === 'ALUMNI') {
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

const handleTabChange = () => {}
</script>

<style scoped>
.feed-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.feed-main {
  flex: 1;
}

.feed-hero {
  margin-bottom: 14px;
  padding: 4px 2px 0;
}

.feed-hero-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px 16px;
}

.feed-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--ink);
  line-height: 1.3;
}

.feed-desc {
  margin: 0;
  padding-left: 16px;
  border-left: 1px solid var(--border-subtle);
  font-size: 13px;
  color: var(--ink-muted);
  font-weight: 500;
  line-height: 1.4;
}

.tabs-panel {
  border-radius: var(--radius-lg);
  background: var(--surface);
  border: 1px solid var(--border-subtle);
  box-shadow: var(--shadow-card);
  overflow: hidden;
  backdrop-filter: blur(12px);
}

.premium-tabs {
  --tab-accent: var(--accent);
}

.premium-tabs :deep(.el-tabs__header) {
  margin: 0;
  padding: 0 8px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.65) 0%, rgba(248, 249, 252, 0.9) 100%);
  border-bottom: 1px solid var(--border-subtle);
}

.premium-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.premium-tabs :deep(.el-tabs__nav-scroll) {
  padding: 0 8px;
}

.premium-tabs :deep(.el-tabs__item) {
  height: 46px;
  line-height: 46px;
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-muted);
  padding: 0 20px;
}

.premium-tabs :deep(.el-tabs__item.is-active) {
  color: var(--accent);
  font-weight: 700;
}

.premium-tabs :deep(.el-tabs__active-bar) {
  height: 3px;
  border-radius: 3px 3px 0 0;
  background: linear-gradient(90deg, #4f46e5, #0ea5e9);
}

.premium-tabs :deep(.el-tabs__content) {
  padding: 0;
}

.premium-tabs :deep(.el-tab-pane) {
  min-height: min(640px, calc(100vh - var(--shell-h) - 132px));
}

@media (max-width: 640px) {
  .feed-hero-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  .feed-desc {
    padding-left: 0;
    border-left: none;
  }
}

.feed-with-sidebar {
  display: flex;
  gap: 14px;
  align-items: flex-start;
}

.feed-left {
  flex: 1;
  min-width: 0;
}

.feed-right {
  width: 320px;
  flex-shrink: 0;
}

@media (max-width: 1020px) {
  .feed-with-sidebar {
    flex-direction: column;
  }
  .feed-right {
    width: 100%;
  }
}
</style>
