<template>
  <header class="shell-bar">
    <div class="shell-inner">
      <router-link to="/" class="brand" aria-label="首页">
        <span class="brand-mark">social</span>
        <span class="brand-text">
          <span class="brand-name">遇见校友</span>
          <span class="brand-tagline">连结 · 分享 · 同行</span>
        </span>
      </router-link>

      <nav class="nav" aria-label="主导航">
        <router-link
          to="/"
          class="nav-link"
          :class="{ active: isFeedActive }"
        >
          动态圈
        </router-link>
        <router-link
          to="/activity"
          class="nav-link"
          :class="{ active: isActivityActive }"
        >
          活动
        </router-link>
        <router-link
          to="/message"
          class="nav-link"
          :class="{ active: route.path.startsWith('/message') }"
        >
          消息
        </router-link>
      </nav>

      <div class="actions">
        <el-button type="primary" class="btn-publish" round aria-label="发布动态" @click="goPublish">
          <el-icon><Plus /></el-icon>
          <span class="btn-publish-text">发布动态</span>
        </el-button>

        <router-link to="/message" class="icon-btn" aria-label="消息">
          <el-badge
            :value="totalUnreadCount"
            :hidden="!hasUnreadMessages"
            :max="99"
            class="msg-badge"
          >
            <el-icon class="msg-icon"><ChatDotRound /></el-icon>
          </el-badge>
        </router-link>

        <button type="button" class="avatar-btn" aria-label="我的主页" @click="goProfile">
          <ResolvedAvatar :size="38" :src="userStore.userInfo?.avatar || ''">
            {{ userStore.username?.[0] || '用' }}
          </ResolvedAvatar>
        </button>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Plus, ChatDotRound } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { useMessageStore } from '../stores/message'
import { alumniApi } from '../api/alumni'
import { authApi } from '../api/auth'
import ResolvedAvatar from './ResolvedAvatar.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const messageStore = useMessageStore()

const isFeedActive = computed(
  () => route.path === '/' || route.path === '/feed'
)

const isActivityActive = computed(
  () => route.path === '/activity' || route.path.startsWith('/activity/')
)

const totalUnreadCount = computed(() => messageStore.totalUnreadCount)
const hasUnreadMessages = computed(() => messageStore.hasUnreadMessages)

const goPublish = () => router.push('/post/publish')

const goProfile = async () => {
  try {
    const response = await alumniApi.getCurrentProfile()
    if (response.code === 200 && response.data?.userId) {
      router.push(`/profile/${response.data.userId}`)
      return
    }
  } catch {
    /* fallback */
  }
  router.push(`/profile/${userStore.userId}`)
}

onMounted(() => {
  const token = localStorage.getItem('token')
  if (token) {
    authApi.getAlumniVerifyStatus().then((res) => {
      const role = res?.data?.role
      if (role && role !== userStore.role) {
        userStore.role = role
        localStorage.setItem('role', role)
      }
      if (role === 'ALUMNI') {
        messageStore.fetchUnreadCounts()
      }
    }).catch(() => {})
  }
})
</script>

<style scoped>
.shell-bar {
  position: sticky;
  top: 0;
  z-index: 100;
  height: var(--shell-h);
  display: flex;
  align-items: center;
  border-bottom: 1px solid var(--border-subtle);
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  box-shadow: 0 1px 0 rgba(255, 255, 255, 0.6) inset;
}

.shell-inner {
  max-width: calc(var(--content-max) + 48px);
  margin: 0 auto;
  padding: 0 28px;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  color: inherit;
  flex-shrink: 0;
}

.brand-mark {
  min-width: 76px;
  height: 38px;
  padding: 0 12px;
  border-radius: 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.05em;
  text-transform: lowercase;
  color: #fff;
  background: linear-gradient(135deg, #4f46e5, #6366f1 45%, #0ea5e9);
  box-shadow: 0 4px 14px rgba(79, 70, 229, 0.35);
}

.brand-text {
  display: flex;
  flex-direction: column;
  gap: 1px;
  line-height: 1.15;
}

.brand-name {
  font-size: 16px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--ink);
}

.brand-tagline {
  font-size: 11px;
  font-weight: 500;
  color: var(--ink-faint);
  letter-spacing: 0.04em;
}

.nav {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
  justify-content: center;
}

.nav-link {
  padding: 8px 18px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-muted);
  text-decoration: none;
  transition: color 0.2s, background 0.2s;
}

.nav-link:hover {
  color: var(--accent);
  background: var(--accent-soft);
}

.nav-link.active {
  color: var(--accent);
  background: var(--accent-soft);
}

.actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.btn-publish {
  font-weight: 600;
  padding: 10px 18px;
  box-shadow: 0 4px 14px rgba(79, 70, 229, 0.28);
}

.icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 12px;
  color: var(--ink-muted);
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid var(--border-subtle);
  transition: color 0.2s, border-color 0.2s, box-shadow 0.2s;
  text-decoration: none;
}

.icon-btn:hover {
  color: var(--accent);
  border-color: rgba(79, 70, 229, 0.25);
  box-shadow: var(--shadow-sm);
}

.msg-icon {
  font-size: 22px;
}

.avatar-btn {
  padding: 0;
  border: none;
  background: none;
  cursor: pointer;
  border-radius: 50%;
  line-height: 0;
  box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.95), 0 4px 12px rgba(12, 18, 34, 0.12);
  transition: transform 0.2s, box-shadow 0.2s;
}

.avatar-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 0 0 2px #fff, 0 6px 20px rgba(79, 70, 229, 0.2);
}

@media (max-width: 640px) {
  .brand-tagline {
    display: none;
  }

  .nav {
    justify-content: flex-start;
  }

  .nav-link {
    padding: 8px 12px;
    font-size: 13px;
  }

  .btn-publish-text {
    display: none;
  }

  .btn-publish {
    padding: 10px 12px;
    min-width: auto;
  }
}
</style>
