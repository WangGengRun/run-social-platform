<template>
  <div class="post-list" ref="postListRef" @scroll="handleScroll">
    <div v-if="posts.loading && posts.list.length === 0" class="loading-container">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载中...</span>
    </div>
    
    <div v-else-if="posts.list.length === 0" class="empty-container">
      <el-empty description="暂无动态" />
    </div>
    
    <div v-else class="posts">
      <div
        v-for="post in posts.list"
        :key="post.id"
        class="post-card"
        :class="{ locked: isLockedPost(post) }"
        @click="handlePostClick(post)"
      >
        <div class="post-header">
          <div class="user-info" @click.stop="handleUserClick(post.userId)">
            <ResolvedAvatar :size="40" :src="post.avatar || ''" class="avatar">
              <span>{{ post.username?.charAt(0) || post.realName?.charAt(0) || '用' }}</span>
            </ResolvedAvatar>
            <div class="user-details">
              <div class="username-row">
                <div class="username">{{ post.username || post.realName }}</div>
                <el-tag v-if="post.userRole === 'ALUMNI'" size="small" type="warning" effect="plain">校友</el-tag>
                <el-tag v-else size="small" effect="plain">普通用户</el-tag>
              </div>
              <div class="time">{{ formatTime(post.createdAt) }}</div>
            </div>
          </div>
        </div>
        
        <div class="post-content">
          <div v-if="isLockedPost(post)" class="lock-banner">🔒 校友可见内容，完成校友认证后可查看</div>
          <div class="text-content">{{ post.content }}</div>
          <div
            v-if="!isLockedPost(post) && shouldShowMore(post)"
            class="read-more"
            @click.stop="handlePostClick(post)"
          >
            查看更多
          </div>
          
          <div
            v-if="post.imageUrlList && post.imageUrlList.length > 0"
            class="image-grid"
            :class="`image-count-${Math.min(post.imageUrlList.length, 5)}`"
          >
            <div
              v-for="(imageUrl, index) in post.imageUrlList"
              :key="index"
              class="image-item"
              @click.stop="handleImageClick(imageUrl)"
            >
              <el-image
                :src="imageUrl"
                fit="contain"
                :preview-src-list="post.imageUrlList"
                :initial-index="index"
                :preview-teleported="true"
              />
            </div>
          </div>
        </div>
        
        <div class="post-footer">
          <div class="action-buttons">
            <div
              class="action-button"
              :class="{ 'liked': post.isLiked }"
              @click.stop="handleLikeClick(post.id)"
            >
              <svg
                class="like-thumb"
                :class="{ liked: post.isLiked }"
                width="20"
                height="20"
                viewBox="0 0 24 24"
                aria-hidden="true"
              >
                <!-- 简化拇指上扬图标（避免引入额外依赖） -->
                <path
                  fill="currentColor"
                  d="M2 21h4V10H2v11zm20-11c0-1.1-.9-2-2-2h-3l1-6c.05-.25-.02-.51-.2-.69-.18-.18-.44-.25-.69-.2l-1 .2c-.36.07-.65.35-.75.7L13 9H9c-.55 0-1 .45-1 1v10c0 .55.45 1 1 1h10c.55 0 1-.45 1-1v-11h2z"
                />
              </svg>
              <span>{{ post.likeCount || 0 }}</span>
            </div>
            
            <div
              class="action-button"
              @click.stop="handleCommentClick(post.id)"
            >
              <el-icon><ChatDotRound /></el-icon>
              <span>{{ post.commentCount || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <div v-if="posts.loading && posts.list.length > 0 && posts.hasMore" class="loading-more">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载更多...</span>
      </div>
      
      <div v-if="!posts.hasMore && posts.list.length > 0" class="no-more">
        <span>没有更多内容啦 (ฅ'ω'ฅ) ~</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, ChatDotRound } from '@element-plus/icons-vue'
import { usePostStore } from '../stores/post'
import { useUserStore } from '../stores/user'
import ResolvedAvatar from './ResolvedAvatar.vue'

const props = defineProps({
  type: {
    type: String,
    required: true,
    validator: (value) => ['recommended', 'following'].includes(value)
  }
})

const router = useRouter()
const postStore = usePostStore()
const userStore = useUserStore()
const postListRef = ref()

const posts = computed(() => postStore.getPostsByType(props.type))

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  const month = 30 * day
  const year = 365 * day
  
  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  } else if (diff < month) {
    return `${Math.floor(diff / day)}天前`
  } else if (diff < year) {
    return `${Math.floor(diff / month)}个月前`
  } else {
    return `${Math.floor(diff / year)}年前`
  }
}

const handleScroll = () => {
  const element = postListRef.value
  if (!element) return
  if (posts.value.loading || !posts.value.hasMore) return
  
  const { scrollTop, scrollHeight, clientHeight } = element
  
  if (scrollTop + clientHeight >= scrollHeight - 100) {
    loadMorePosts()
  }
}

const loadPosts = async (refresh = false) => {
  try {
    if (props.type === 'recommended') {
      await postStore.fetchRecommendedPosts(refresh)
    } else if (props.type === 'following') {
      await postStore.fetchFollowingPosts(refresh)
    }
  } catch (error) {
    console.error('加载动态失败:', error)
  }
}

const loadMorePosts = () => {
  loadPosts(false)
}

const handleLikeClick = async (postId) => {
  try {
    await postStore.toggleLike(postId, props.type)
  } catch (error) {
    ElMessage.error('操作失败，请重试')
  }
}

const isLockedPost = (post) => {
  if (!post) return false
  if (String(post.userId) === String(userStore.userId)) return false
  if (userStore.role !== 'USER') return false
  return post.locked === true || post.visibility === 1
}

const handlePostClick = (post) => {
  if (isLockedPost(post)) {
    ElMessage.warning('该动态仅校友可见，请先完成校友认证')
    return
  }
  const postId = post.id
  router.push(`/post/${postId}`)
}

const handleUserClick = (userId) => {
  console.log('跳转到个人主页:', userId)
  router.push(`/profile/${userId}`)
}

const handleCommentClick = (postId) => {
  router.push(`/post/${postId}`)
}

const handleImageClick = (imageUrl) => {
  console.log('查看图片:', imageUrl)
}

const shouldShowMore = (post) => {
  const content = String(post?.content || '')
  const normalized = content.replace(/\r/g, '')
  const lineCount = normalized.split('\n').length
  return normalized.length > 120 || lineCount > 4
}

onMounted(() => {
  loadPosts(true)
})
</script>

<style scoped>
.post-list {
  max-height: calc(100vh - var(--shell-h) - 128px);
  min-height: min(560px, calc(100vh - var(--shell-h) - 128px));
  overflow-y: auto;
  padding: 20px 22px 28px;
  scroll-behavior: smooth;
}

.loading-container,
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  color: #909399;
}

.loading-container .el-icon {
  font-size: 32px;
  margin-bottom: 10px;
}

.posts {
  display: flex;
  flex-direction: column;
  gap: 14px;
  align-items: center;
}

.post-card {
  width: min(100%, 760px);
  background: var(--surface-solid);
  border-radius: var(--radius-md);
  padding: 18px 18px 14px;
  border: 1px solid var(--border-subtle);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
  display: flex;
  flex-direction: column;
  height: 420px;
  overflow: hidden;
}

.post-card:has(.image-grid) {
  height: 468px;
}

.post-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-card-hover);
  border-color: rgba(79, 70, 229, 0.12);
}

.post-card.locked {
  border-color: rgba(245, 158, 11, 0.28);
}

.post-card.locked .text-content {
  filter: blur(6px);
  user-select: none;
}

.post-card.locked .image-item :deep(.el-image__inner) {
  filter: blur(8px);
}

.post-header {
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.username-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.username {
  font-size: 15px;
  font-weight: 700;
  color: var(--ink);
  letter-spacing: -0.02em;
}

.time {
  font-size: 12px;
  color: var(--ink-faint);
  margin-top: 3px;
  font-weight: 500;
}

.post-content {
  flex: 1 1 auto;
  min-height: 0;
  display: flex;
  flex-direction: column;
  margin-bottom: 10px;
}

.text-content {
  font-size: 15px;
  color: var(--ink-muted);
  line-height: 1.65;
  margin-bottom: 6px;
  white-space: pre-wrap;
  word-break: break-word;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: calc(1.65em * 4);
}

.read-more {
  display: inline-block;
  font-size: 13px;
  font-weight: 600;
  color: var(--accent);
  margin-bottom: 10px;
  cursor: pointer;
}

.read-more:hover {
  opacity: 0.85;
  text-decoration: underline;
}

.lock-banner {
  display: inline-flex;
  align-items: center;
  margin-bottom: 10px;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(245, 158, 11, 0.12);
  color: #b45309;
  font-size: 12px;
  font-weight: 600;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 4px;
  margin-top: 6px;
  flex: 1 1 auto;
  min-height: 0;
  height: 240px;
  align-content: stretch;
}

.image-grid.image-count-1 {
  grid-template-columns: 1fr;
  height: 292px;
}

.image-grid.image-count-2,
.image-grid.image-count-3,
.image-grid.image-count-4 {
  grid-template-columns: repeat(2, 1fr);
  height: 268px;
}

.image-grid.image-count-5 {
  grid-template-columns: repeat(3, 1fr);
  height: 248px;
}

.image-item {
  border-radius: var(--radius-sm);
  cursor: pointer;
  border: 1px solid var(--border-subtle);
  overflow: hidden;
  display: flex;
  align-items: flex-start;
  justify-content: flex-start;
  min-height: 0;
}

.image-item :deep(.el-image) {
  width: 100%;
  height: 100%;
  display: block;
}

.image-item :deep(.el-image__inner) {
  object-fit: contain !important;
  object-position: left center;
  background: transparent;
  width: 100%;
  height: 100%;
}

.post-footer {
  border-top: 1px solid var(--border-subtle);
  padding-top: 14px;
  margin-top: 2px;
  flex: 0 0 auto;
}

.action-buttons {
  display: flex;
  gap: 24px;
}

.action-button {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--ink-faint);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: color 0.2s, transform 0.2s;
}

.action-button:hover {
  color: var(--accent);
}

.action-button.liked {
  color: #e11d48;
}

.like-thumb {
  color: currentColor;
  transition: transform 0.18s ease, color 0.2s ease;
}

.action-button.liked .like-thumb {
  transform: translateY(-1px) scale(1.04);
}

.action-button .el-icon {
  font-size: 18px;
}

.loading-more,
.no-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px;
  color: #909399;
  font-size: 14px;
}

.loading-more .el-icon {
  font-size: 20px;
}
</style>
