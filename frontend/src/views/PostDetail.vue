<template>
  <div class="post-detail-container">
    <!-- 顶部动态卡片 -->
    <div class="post-card">
      <div class="post-header">
        <div class="user-info" @click="handleUserClick(post.userId)">
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
            @click="handleImageClick(imageUrl, index)"
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
            @click="handleLikeClick"
          >
            <el-icon>
              <component :is="post.isLiked ? 'StarFilled' : 'Star'" />
            </el-icon>
            <span>{{ post.likeCount || 0 }}</span>
          </div>
          
          <div class="action-button">
            <el-icon><ChatDotRound /></el-icon>
            <span>{{ post.commentCount || 0 }}</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 评论区 -->
    <div class="comments-section">
      <h3 class="comments-title">评论 ({{ comments.length }})</h3>
      
      <!-- 评论列表 -->
      <div class="comments-list">
        <el-skeleton v-if="loading" :rows="3" animated />
        <div v-else-if="comments.length === 0" class="no-comments">
          暂无评论，快来发表第一条评论吧！
        </div>
        <CommentItem
          v-else
          v-for="comment in comments"
          :key="comment.id"
          :comment="comment"
          :post-id="postId"
          @reply="handleReplyClick"
          @user-click="handleUserClick"
          @delete="handleCommentDelete"
        />
      </div>
    </div>
    
    <!-- 评论输入框 -->
    <div class="comment-input-section">
      <CommentInput
        :post-id="postId"
        :reply-to="replyTo"
        @submit="handleCommentSubmit"
        @cancel="handleCancelReply"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, Star, StarFilled, ChatDotRound } from '@element-plus/icons-vue'
import CommentItem from '../components/CommentItem.vue'
import CommentInput from '../components/CommentInput.vue'
import { postApi } from '../api/post'
import { usePostStore } from '../stores/post'

const route = useRoute()
const router = useRouter()
const postStore = usePostStore()

const postId = ref(Number(route.params.id))
const post = ref({})
const comments = ref([])
const loading = ref(true)
const replyTo = ref(null)

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

const getPostDetail = async () => {
  try {
    const response = await postApi.getPostDetail(postId.value)
    if (response.code === 200) {
      post.value = response.data
    }
  } catch (error) {
    console.error('获取动态详情失败:', error)
    ElMessage.error('获取动态详情失败，请重试')
  }
}

const getComments = async () => {
  try {
    const response = await postApi.getComments(postId.value)
    if (response.code === 200) {
      comments.value = response.data || []
    }
  } catch (error) {
    console.error('获取评论列表失败:', error)
    ElMessage.error('获取评论列表失败，请重试')
  } finally {
    loading.value = false
  }
}

const handleLikeClick = async () => {
  try {
    if (post.value.isLiked) {
      await postStore.unlikePost(postId.value, 'recommended')
      post.value.isLiked = false
      post.value.likeCount--
    } else {
      await postStore.likePost(postId.value, 'recommended')
      post.value.isLiked = true
      post.value.likeCount++
    }
  } catch (error) {
    ElMessage.error('操作失败，请重试')
  }
}

const handleReplyClick = (comment) => {
  replyTo.value = comment
}

const handleCommentDelete = (commentId) => {
  const deleteCommentFromList = (list) => {
    const index = list.findIndex(c => c.id === commentId)
    if (index !== -1) {
      list.splice(index, 1)
      return true
    }
    for (const comment of list) {
      if (comment.replies && deleteCommentFromList(comment.replies)) {
        return true
      }
    }
    return false
  }
  
  deleteCommentFromList(comments.value)
}

const handleCancelReply = () => {
  replyTo.value = null
}

const handleCommentSubmit = async (commentData) => {
  try {
    const response = await postApi.addComment(
      commentData.postId,
      commentData.content,
      commentData.parentId
    )
    
    if (response.code === 200) {
      ElMessage.success('发表成功')
      
      // 模拟评论数据，实际应该从接口获取最新评论
      const newComment = {
        id: response.data,
        userId: 1, // 假设当前用户id
        username: '当前用户', // 假设当前用户名
        realName: '当前用户', // 假设当前用户真实姓名
        avatar: '', // 假设当前用户头像
        content: commentData.content,
        parentId: commentData.parentId,
        isSelf: true,
        createdAt: new Date().toISOString(),
        replies: []
      }
      
      if (commentData.parentId === 0) {
        // 一级评论，添加到列表顶部
        comments.value.unshift(newComment)
      } else {
        // 回复，添加到对应评论的回复列表
        const parentComment = findCommentById(comments.value, commentData.parentId)
        if (parentComment) {
          if (!parentComment.replies) {
            parentComment.replies = []
          }
          parentComment.replies.unshift(newComment)
        }
      }
      
      // 清空回复状态
      replyTo.value = null
    }
  } catch (error) {
    console.error('发表评论失败:', error)
    ElMessage.error('发表评论失败，请重试')
  }
}

const findCommentById = (commentsList, commentId) => {
  for (const comment of commentsList) {
    if (comment.id === commentId) {
      return comment
    }
    if (comment.replies && comment.replies.length > 0) {
      const found = findCommentById(comment.replies, commentId)
      if (found) {
        return found
      }
    }
  }
  return null
}

const handleUserClick = (userId) => {
  console.log('跳转到个人主页:', userId)
}

const handleImageClick = (imageUrl, index) => {
  console.log('查看图片:', imageUrl, index)
}

onMounted(() => {
  if (!postId.value) {
    ElMessage.error('动态ID不存在')
    router.push('/feed')
    return
  }
  
  getPostDetail()
  getComments()
})
</script>

<style scoped>
.post-detail-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
  padding-bottom: 100px;
}

.post-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
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
  font-size: 16px;
  color: #303133;
  line-height: 1.6;
  margin-bottom: 16px;
  white-space: pre-wrap;
  word-break: break-word;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.image-grid.single {
  grid-template-columns: 1fr;
}

.image-item {
  aspect-ratio: 1;
  overflow: hidden;
  border-radius: 8px;
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

.comments-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
}

.comments-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 20px;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.no-comments {
  text-align: center;
  color: #909399;
  padding: 40px 0;
}

.comment-input-section {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #fff;
  border-top: 1px solid #f0f0f0;
  padding: 16px;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
  max-width: 800px;
  margin: 0 auto;
}

@media (max-width: 840px) {
  .post-detail-container {
    padding: 10px;
    padding-bottom: 120px;
  }
  
  .comment-input-section {
    padding: 12px;
  }
}
</style>
