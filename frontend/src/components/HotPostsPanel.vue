<template>
  <aside class="hot-panel">
    <header class="hot-header">
      <div class="hot-dot" aria-hidden="true" />
      <div class="hot-title-wrap">
        <div class="hot-title">热门动态</div>
        <div class="hot-sub">Top 10</div>
      </div>

      <div class="hot-decor" aria-hidden="true">
        <span v-for="n in 6" :key="n" class="hot-decor-dot" />
      </div>
    </header>

    <div v-if="loading" class="hot-loading">
      <el-skeleton :rows="6" animated />
    </div>

    <div v-else class="hot-list">
      <div
        v-for="post in hotTop10"
        :key="post.id"
        class="hot-item"
        @click="goDetail(post.id)"
      >
        <ResolvedAvatar :size="34" :src="post.avatar || ''" />
        <div class="hot-main">
          <div class="hot-author">
            {{ post.realName || post.username }}
          </div>
          <div class="hot-content">
            {{ post.content }}
          </div>

          <div class="hot-stats">
            <span class="hot-like">
              <svg class="like-thumb" :class="{ liked: post.isLiked }" width="16" height="16" viewBox="0 0 24 24">
                <path fill="currentColor" d="M2 21h4V10H2v11zm20-11c0-1.1-.9-2-2-2h-3l1-6c.05-.25-.02-.51-.2-.69-.18-.18-.44-.25-.69-.2l-1 .2c-.36.07-.65.35-.75.7L13 9H9c-.55 0-1 .45-1 1v10c0 .55.45 1 1 1h10c.55 0 1-.45 1-1v-11h2z" />
              </svg>
              {{ post.likeCount || 0 }}
            </span>
            <span class="hot-comment">
              {{ post.commentCount || 0 }} 评论
            </span>
          </div>
        </div>
      </div>

      <el-empty v-if="hotTop10.length === 0" description="暂无热门动态" />
    </div>
  </aside>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { usePostStore } from '../stores/post'
import ResolvedAvatar from './ResolvedAvatar.vue'

const router = useRouter()
const postStore = usePostStore()
const loading = ref(false)

const hotTop10 = computed(() => {
  const list = postStore.hotPosts?.list || []
  return list.slice(0, 10)
})

const goDetail = (id) => {
  router.push(`/post/${id}`)
}

onMounted(async () => {
  try {
    loading.value = true
    await postStore.fetchHotPosts(true)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.hot-panel {
  width: 320px;
  flex-shrink: 0;
  border-radius: var(--radius-xl);
  border: 1px solid var(--border-subtle);
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(12px);
  box-shadow: var(--shadow-card);
  overflow: hidden;
  position: relative;
}

.hot-header {
  padding: 16px 16px 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.9) 0%, rgba(248, 249, 252, 0.96) 100%);
  border-bottom: 1px solid var(--border-subtle);
}

.hot-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--accent);
  box-shadow: 0 0 0 6px rgba(79, 70, 229, 0.12);
  flex-shrink: 0;
}

.hot-title-wrap {
  flex: 1;
  min-width: 0;
}

.hot-title {
  font-weight: 900;
  letter-spacing: -0.02em;
  color: var(--ink);
  font-size: 16px;
}

.hot-sub {
  margin-top: 2px;
  font-weight: 650;
  color: var(--ink-faint);
  font-size: 12px;
}

.hot-decor {
  display: flex;
  gap: 4px;
  align-items: center;
}

.hot-decor-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: rgba(79, 70, 229, 0.25);
}

.hot-loading {
  padding: 12px 12px 14px;
}

.hot-list {
  padding: 10px 12px 12px;
  max-height: calc(100vh - var(--shell-h) - 160px);
  overflow-y: auto;
}

.hot-item {
  display: flex;
  gap: 10px;
  padding: 12px 10px;
  border-radius: var(--radius-md);
  border: 1px solid rgba(12, 18, 34, 0.05);
  background: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
  margin-bottom: 10px;
}

.hot-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-card-hover);
  border-color: rgba(79, 70, 229, 0.18);
}

.hot-main {
  min-width: 0;
  flex: 1;
}

.hot-author {
  font-weight: 900;
  color: var(--ink);
  font-size: 13px;
  margin-bottom: 4px;
}

.hot-content {
  color: var(--ink-muted);
  font-weight: 600;
  font-size: 12px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.hot-stats {
  margin-top: 8px;
  display: flex;
  justify-content: space-between;
  gap: 10px;
  font-size: 12px;
  font-weight: 700;
  color: var(--ink-faint);
}

.hot-like {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.like-thumb {
  color: #909399;
  transition: color 0.2s ease, transform 0.2s ease;
}

.like-thumb.liked {
  color: #e11d48;
}

.hot-comment {
  white-space: nowrap;
}
</style>

