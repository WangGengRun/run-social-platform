<template>
  <el-avatar :size="size" :src="displayUrl">
    <slot />
  </el-avatar>
</template>

<script setup>
import { ref, watch } from 'vue'
import { resolveAvatarUrl } from '../utils/avatarUrl'

const props = defineProps({
  src: {
    type: String,
    default: ''
  },
  size: {
    type: [Number, String],
    default: 40
  }
})

const displayUrl = ref('')

watch(
  () => props.src,
  async (v) => {
    displayUrl.value = await resolveAvatarUrl(v || '')
  },
  { immediate: true }
)
</script>
