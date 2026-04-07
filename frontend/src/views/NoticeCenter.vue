<template>
  <div class="notice-page">
    <AppShellBar />
    <div class="notice-container alumni-page">
      <div class="notice-header">
        <div class="tabs">
          <router-link to="/message" class="tab">
            <span>私信</span>
          </router-link>
          <router-link to="/message/notice" class="tab active">
            <el-badge
              :value="noticeUnread"
              :hidden="!noticeUnread"
              :max="99"
            >
              <span>消息中心</span>
            </el-badge>
          </router-link>
        </div>
      </div>

      <div v-if="loading && noticeList.length === 0" class="loading-container">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <div v-else-if="noticeList.length === 0" class="empty-container">
        <el-empty description="暂时还没有新消息 (ง •_•)ง" />
      </div>

      <div
        v-else
        ref="listRef"
        class="notice-list"
        @scroll.passive="handleScroll"
      >
        <div
          v-for="item in noticeList"
          :key="`${item.noticeType}-${item.sourceId}`"
          class="notice-item"
          :class="{ unread: item.isRead === false }"
          @click="handleItemClick(item)"
        >
          <div class="left">
            <ResolvedAvatar :size="44" :src="item.fromAvatar || ''">
              {{ (item.fromRealName || item.fromUsername || '用')[0] }}
            </ResolvedAvatar>
            <span v-if="item.isRead === false" class="unread-dot" />
          </div>
          <div class="content">
            <div class="title-row">
              <div class="title">
                <span class="name">{{ item.fromRealName || item.fromUsername || '用户' }}</span>
                <span class="action">{{ getActionText(item) }}</span>
              </div>
              <div class="time">{{ formatTime(item.createdAt) }}</div>
            </div>
            <div v-if="item.noticeType === 'COMMENT' && item.commentContent" class="comment">
              “{{ item.commentContent }}”
            </div>
            <div v-if="item.postId && item.postContent" class="post-snippet">
              {{ item.postContent }}
            </div>
          </div>
        </div>

        <div v-if="loading && noticeList.length > 0" class="loading-more">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载更多...</span>
        </div>

        <div v-if="!hasMore && noticeList.length > 0" class="no-more">
          <span>没有更多内容啦 (ฅ'ω'ฅ) ~</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import AppShellBar from '../components/AppShellBar.vue'
import ResolvedAvatar from '../components/ResolvedAvatar.vue'
import { noticeApi } from '../api/notice'
import { useNoticeStore } from '../stores/notice'

const router = useRouter()
const noticeStore = useNoticeStore()
const listRef = ref()
const loading = ref(false)
const hasMore = ref(true)
const pageNum = ref(1)
const pageSize = ref(20)
const noticeList = ref([])
const noticeUnread = computed(() => noticeStore.unreadCount || 0)

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  if (diff < minute) return '刚刚'
  if (diff < hour) return `${Math.floor(diff / minute)}分钟前`
  if (diff < day) return `${Math.floor(diff / hour)}小时前`
  return `${date.getMonth() + 1}-${date.getDate()}`
}

const getActionText = (item) => {
  if (item.noticeType === 'LIKE') return '点赞了你的动态'
  if (item.noticeType === 'COMMENT') return '评论了你的动态'
  if (item.noticeType === 'FOLLOW') return '关注了你'
  return '有新动态'
}

const handleItemClick = (item) => {
  if (item.noticeType === 'FOLLOW') {
    router.push(`/profile/${item.fromUserId}`)
    return
  }
  if (item.postId) {
    router.push(`/post/${item.postId}`)
  }
}

const fetchList = async (refresh = false) => {
  if (loading.value) return
  if (refresh) {
    pageNum.value = 1
    hasMore.value = true
    noticeList.value = []
  }
  if (!hasMore.value) return

  loading.value = true
  try {
    const res = await noticeApi.getNoticeList(pageNum.value, pageSize.value)
    if (res.code === 200) {
      const data = res.data || {}
      const list = data.list || []
      if (refresh) noticeList.value = list
      else noticeList.value = [...noticeList.value, ...list]
      const totalPages = Number(data.totalPages || 0)
      const cur = Number(data.pageNum || pageNum.value)
      hasMore.value = totalPages ? cur < totalPages : list.length === pageSize.value
      pageNum.value = cur + 1
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('加载失败，请重试')
  } finally {
    loading.value = false
  }
}

const handleScroll = () => {
  const el = listRef.value
  if (!el) return
  if (loading.value || !hasMore.value) return
  if (el.scrollTop + el.clientHeight >= el.scrollHeight - 120) {
    fetchList(false)
  }
}

onMounted(() => {
  fetchList(true)
  noticeStore.markAllAsRead()
})
</script>

<style scoped>
.notice-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.notice-container {
  flex: 1;
}

.notice-header {
  margin-bottom: 18px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--border-subtle);
}

.tabs {
  display: inline-flex;
  gap: 8px;
  padding: 6px;
  border: 1px solid var(--border-subtle);
  border-radius: 999px;
  background: var(--surface-solid);
  box-shadow: var(--shadow-sm);
}

.tab {
  text-decoration: none;
  color: var(--ink-muted);
  font-size: 14px;
  font-weight: 700;
  padding: 8px 14px;
  border-radius: 999px;
  transition: background 0.2s, color 0.2s;
}

.tab:hover {
  color: var(--accent);
  background: var(--accent-soft);
}

.tab.active {
  color: var(--accent);
  background: var(--accent-soft);
}

.loading-container,
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 320px;
  color: #909399;
}

.notice-list {
  max-height: calc(100vh - var(--shell-h) - 164px);
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 12px 2px 18px;
}

.notice-item {
  display: flex;
  gap: 12px;
  padding: 14px 14px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-subtle);
  background: var(--surface-solid);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.notice-item.unread {
  background: linear-gradient(135deg, rgba(79, 70, 229, 0.06) 0%, rgba(14, 165, 233, 0.05) 100%);
  border-color: rgba(79, 70, 229, 0.15);
}

.left {
  position: relative;
}

.unread-dot {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: #ef4444;
  box-shadow: 0 0 0 2px var(--surface-solid);
}

.notice-item:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-card);
}

.left {
  flex: 0 0 auto;
}

.content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.title-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
}

.title {
  min-width: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: baseline;
}

.name {
  font-weight: 800;
  color: var(--ink);
}

.action {
  color: var(--ink-muted);
  font-weight: 600;
}

.time {
  flex: 0 0 auto;
  font-size: 12px;
  color: var(--ink-faint);
  font-weight: 600;
}

.comment {
  color: var(--ink);
  font-size: 14px;
  line-height: 1.55;
}

.post-snippet {
  color: var(--ink-faint);
  font-size: 13px;
  line-height: 1.45;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.loading-more,
.no-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 18px 0 8px;
  color: #909399;
  font-size: 14px;
}
</style>

