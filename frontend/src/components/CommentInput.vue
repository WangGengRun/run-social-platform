<template>
  <div class="comment-input-container">
    <el-input
      v-model="commentContent"
      type="textarea"
      :rows="3"
      :placeholder="placeholder"
      resize="none"
      class="comment-textarea"
      @keyup.enter.exact="handleSubmit"
    />
    <div class="comment-actions">
      <el-button
        type="primary"
        @click="handleSubmit"
        :loading="loading"
        :disabled="!commentContent.trim()"
      >
        发表
      </el-button>
      <el-button
        v-if="isReplyMode"
        type="text"
        @click="handleCancelReply"
      >
        取消
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  postId: {
    type: Number,
    required: true
  },
  replyTo: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['submit', 'cancel'])

const commentContent = ref('')
const loading = ref(false)

const isReplyMode = computed(() => !!props.replyTo)

const placeholder = computed(() => {
  if (isReplyMode.value) {
    return `回复 @${props.replyTo.username || props.replyTo.realName}`
  }
  return '写下你的评论...'
})

const handleSubmit = async () => {
  const content = commentContent.value.trim()
  if (!content) {
    ElMessage.warning('评论内容不能为空')
    return
  }
  
  loading.value = true
  
  try {
    const parentId = isReplyMode.value ? props.replyTo.commentId : 0
    emit('submit', {
      postId: props.postId,
      content,
      parentId
    })
    commentContent.value = ''
  } catch (error) {
    console.error('发表评论失败:', error)
    ElMessage.error('发表评论失败，请重试')
  } finally {
    loading.value = false
  }
}

const handleCancelReply = () => {
  commentContent.value = ''
  emit('cancel')
}
</script>

<style scoped>
.comment-input-container {
  background-color: #fff;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.05);
}

.comment-textarea {
  margin-bottom: 12px;
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
