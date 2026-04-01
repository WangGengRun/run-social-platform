<template>
  <div class="message-bubble-container" :class="{ 'self-message': isSelf, 'other-message': !isSelf }">
    <div v-if="!isSelf" class="message-avatar">
      <el-avatar :size="36" :src="avatar" />
    </div>

    <div class="message-content-wrapper" :class="{ 'self-content': isSelf, 'other-content': !isSelf }">
      <div class="message-bubble" :class="{ 'self-bubble': isSelf, 'other-bubble': !isSelf }">
        <div class="message-text">{{ content }}</div>

        <div class="message-meta">
          <span class="message-time">{{ formatTime(createdAt) }}</span>

          <span v-if="isSelf && showStatus" class="message-status">
            <template v-if="status === 'sending'">
              <el-icon class="is-loading status-icon"><Loading /></el-icon>
              <span>发送中</span>
            </template>
            <template v-else-if="status === 'failed'">
              <span class="failed-status" @click="$emit('retry')">
                <el-icon><WarningFilled /></el-icon>
                <span>发送失败，点击重试</span>
              </span>
            </template>
            <template v-else-if="isRead">
              <span class="read-status">已读</span>
            </template>
            <template v-else>
              <span class="sent-status">已发送</span>
            </template>
          </span>
        </div>
      </div>
    </div>

    <div v-if="isSelf" class="message-avatar">
      <el-avatar :size="36" :src="avatar" />
    </div>
  </div>
</template>

<script setup>
import { Loading, WarningFilled } from '@element-plus/icons-vue'

const props = defineProps({
  isSelf: {
    type: Boolean,
    required: true
  },
  content: {
    type: String,
    required: true
  },
  createdAt: {
    type: String,
    default: ''
  },
  isRead: {
    type: Boolean,
    default: false
  },
  avatar: {
    type: String,
    default: ''
  },
  status: {
    type: String,
    default: 'sent'
  },
  showStatus: {
    type: Boolean,
    default: true
  }
})

defineEmits(['retry'])

const formatTime = (time) => {
  if (!time) return ''

  const date = new Date(time)
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')

  return `${hours}:${minutes}`
}
</script>

<style scoped>
.message-bubble-container {
  display: flex;
  align-items: flex-end;
  margin-bottom: 16px;
  gap: 8px;
}

.self-message {
  justify-content: flex-end;
}

.other-message {
  justify-content: flex-start;
}

.message-avatar {
  flex-shrink: 0;
  margin-bottom: 4px;
}

.message-content-wrapper {
  max-width: 70%;
}

.self-content {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.other-content {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.message-bubble {
  padding: 10px 14px;
  border-radius: 18px;
  position: relative;
  word-wrap: break-word;
  word-break: break-all;
}

.self-bubble {
  background-color: #ecf5ff;
  border: 1px solid #b3d8ff;
  border-bottom-right-radius: 4px;
}

.other-bubble {
  background-color: #ffffff;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.message-text {
  font-size: 14px;
  line-height: 1.4;
  color: #303133;
}

.message-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.message-time {
  flex-shrink: 0;
}

.message-status {
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-icon {
  font-size: 12px;
}

.failed-status {
  color: #f56c6c;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}

.failed-status:hover {
  color: #f78989;
}

.read-status {
  color: #409eff;
}

.sent-status {
  color: #909399;
}

@media (max-width: 768px) {
  .message-content-wrapper {
    max-width: 80%;
  }

  .message-bubble {
    padding: 8px 12px;
  }

  .message-text {
    font-size: 13px;
  }
}
</style>