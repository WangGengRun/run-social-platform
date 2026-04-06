<template>
  <div class="alumni-blur-root" :class="{ 'has-lock': locked }">
    <div v-if="locked" class="alumni-blur-banner">
      <span class="alumni-blur-icon" aria-hidden="true">🔒</span>
      <span>校友可见内容，完成校友认证后可查看</span>
    </div>
    <div class="alumni-blur-body" :class="{ 'is-blurred': locked }">
      <slot />
    </div>
    <div
      v-if="locked"
      class="alumni-blur-mask"
      role="button"
      tabindex="0"
      aria-label="需校友认证后查看"
      @click="onTip"
      @keydown.enter.prevent="onTip"
    />
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'

defineProps({
  locked: {
    type: Boolean,
    default: false
  }
})

const onTip = () => {
  ElMessage.warning('请先完成校友认证，认证后可查看完整内容')
}
</script>

<style scoped>
.alumni-blur-root {
  position: relative;
  border-radius: inherit;
}

.alumni-blur-root.has-lock {
  border-radius: var(--radius-md, 8px);
}

.alumni-blur-banner {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(245, 158, 11, 0.12);
  color: #b45309;
  font-size: 13px;
  font-weight: 600;
  width: fit-content;
  max-width: 100%;
}

.alumni-blur-icon {
  flex-shrink: 0;
}

.alumni-blur-body.is-blurred {
  filter: blur(8px);
  user-select: none;
  pointer-events: none;
}

.alumni-blur-mask {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  z-index: 3;
  cursor: not-allowed;
  border-radius: inherit;
  background: transparent;
}
</style>
