<template>
  <div class="comment-item">
    <div class="comment-header">
      <div class="user-info" @click="handleUserClick(comment.userId)">
        <el-avatar :size="32" :src="comment.avatar" />
        <div class="user-details">
          <div class="username">{{ comment.realName || comment.username }}</div>
          <div class="time">{{ formatTime(comment.createdAt) }}</div>
        </div>
      </div>
      <div class="comment-actions">
        <el-button type="text" size="small" @click="handleReplyClick">回复</el-button>
        <el-button v-if="comment.isSelf" type="text" size="small" @click="handleDeleteClick">删除</el-button>
      </div>
    </div>
    
    <div class="comment-content">
      {{ comment.content }}
    </div>
    
    <!-- 回复列表 -->
    <div v-if="comment.replies && comment.replies.length > 0" class="replies-list">
      <CommentItem
        v-for="reply in comment.replies"
        :key="reply.id"
        :comment="reply"
        :post-id="postId"
        @reply="handleReplyClick"
        @user-click="handleUserClick"
        @delete="handleReplyDelete"
      />
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { postApi } from '../api/post'
import CommentItem from './CommentItem.vue'

const props = defineProps({
  comment: {
    type: Object,
    required: true
  },
  postId: {
    type: Number,
    required: true
  }
})

const comment = props.comment
const postId = props.postId

const emit = defineEmits(['reply', 'user-click', 'delete'])

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

const handleReplyClick = () => {
  emit('reply', {
    userId: comment.userId,
    username: comment.realName || comment.username,
    commentId: comment.id
  })
}

const handleUserClick = (userId) => {
  emit('user-click', userId)
}

const handleDeleteClick = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const commentId = comment.id
    console.log('准备删除评论，ID:', commentId, 'postId:', postId)
    
    const response = await postApi.deleteComment(commentId)
    console.log('删除响应:', response)
    
    if (response.code === 200 || response.code === 0) {
      ElMessage.success('删除成功')
      emit('delete', commentId)
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    console.error('删除评论失败:', error)
    ElMessage.error('删除失败: ' + (error.message || error))
  }
}

const handleReplyDelete = (commentId) => {
  emit('delete', commentId)
}
</script>

<style scoped>
.comment-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.username {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
}

.time {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.comment-actions {
  display: flex;
  gap: 12px;
}

.comment-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  margin-bottom: 8px;
}

.replies-list {
  margin-left: 42px;
  margin-top: 8px;
  border-left: 2px solid #f0f0f0;
  padding-left: 16px;
}

.replies-list :deep(.comment-item) {
  border-bottom: none;
  padding: 8px 0;
}

.replies-list :deep(.comment-item:last-child) {
  border-bottom: 1px solid #f0f0f0;
}
</style>
