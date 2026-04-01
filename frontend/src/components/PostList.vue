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
        @click="handlePostClick(post.id)"
      >
        <div class="post-header">
          <div class="user-info" @click.stop="handleUserClick(post.userId)">
            <el-avatar :size="40" :src="post.avatar" />
            <div class="user-details">
              <div class="username">{{ post.realName || post.username }}</div>
              <div class="time">{{ formatTime(post.createdAt) }}</div>
            </div>
          </div>
        </div>
        
        <div class="post-content">
          <div class="text-content">{{ post.content }}</div>
          
          <div v-if="post.imageUrlList && post.imageUrlList.length > 0" class="image-grid">
            <div
              v-for="(imageUrl, index) in post.imageUrlList"
              :key="index"
              class="image-item"
              :class="{ 'single': post.imageUrlList.length === 1 }"
              @click.stop="handleImageClick(imageUrl)"
            >
              <el-image
                :src="imageUrl"
                fit="cover"
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
              <el-icon v-if="post.isLiked"><GoodsFilled /></el-icon>
              <el-icon v-else><Goods /></el-icon>
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
      
      <div v-if="posts.loading && posts.list.length > 0" class="loading-more">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载更多...</span>
      </div>
      
      <div v-if="!posts.hasMore && posts.list.length > 0" class="no-more">
        <span>没有更多了</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, Goods, GoodsFilled, ChatDotRound } from '@element-plus/icons-vue'
import { usePostStore } from '../stores/post'

const props = defineProps({
  type: {
    type: String,
    required: true,
    validator: (value) => ['recommended', 'following', 'hot'].includes(value)
  }
})

const router = useRouter()
const postStore = usePostStore()
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
    } else if (props.type === 'hot') {
      await postStore.fetchHotPosts(refresh)
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

const handlePostClick = (postId) => {
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

onMounted(() => {
  loadPosts(true)
})
</script>

<style scoped>
.post-list {
  height: calc(100vh - 50px);
  overflow-y: auto;
  padding: 20px;
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
  gap: 16px;
}

.post-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: box-shadow 0.3s;
}

.post-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
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

.username {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.time {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.post-content {
  margin-bottom: 12px;
}

.text-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 12px;
  white-space: pre-wrap;
  word-break: break-word;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 4px;
}

.image-grid.single {
  grid-template-columns: 1fr;
}

.image-item {
  aspect-ratio: 1;
  overflow: hidden;
  border-radius: 4px;
  cursor: pointer;
}

.image-item.single {
  max-width: 400px;
  aspect-ratio: auto;
}

.image-item :deep(.el-image) {
  width: 100%;
  height: 100%;
  display: block;
}

.post-footer {
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
}

.action-buttons {
  display: flex;
  gap: 24px;
}

.action-button {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #909399;
  font-size: 14px;
  cursor: pointer;
  transition: color 0.3s;
}

.action-button:hover {
  color: #409eff;
}

.action-button.liked {
  color: #f56c6c;
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
