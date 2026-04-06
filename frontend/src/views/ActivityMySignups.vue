<template>
  <div class="activity-my-page">
    <AppShellBar />

    <main class="alumni-page activity-layout">
      <div class="activity-hero">
        <div class="activity-hero-left">
          <div class="activity-kicker">my signups</div>
          <h1 class="activity-title">我的报名</h1>
          <p class="activity-desc">查看你已报名的活动，快速进入详情完成签到</p>
        </div>
      </div>

      <div v-if="loading" class="loading-block">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <div v-else>
        <AlumniOnlyBlur :locked="isUserNonAlumni">
          <el-empty v-if="list.length === 0" description="暂无报名活动" />

          <div v-else class="activity-grid">
            <div
              v-for="item in list"
              :key="item.id"
              class="activity-card"
              @click="goDetail(item.id)"
            >
              <div class="cover">
                <el-image :src="item.coverDisplayUrl || item.coverImage" fit="cover" class="cover-img" />
                <div class="cover-overlay">
                  <el-tag size="small" :type="mySignupTag(item).type">
                    {{ mySignupTag(item).label }}
                  </el-tag>
                </div>
              </div>

              <div class="body">
                <h3 class="card-title">{{ item.title }}</h3>
                <div class="meta">
                  <span class="meta-item">地点：{{ item.location }}</span>
                </div>
                <div class="meta">
                  <span class="meta-item">时间：{{ fmt(item.startTime) }} - {{ fmt(item.endTime) }}</span>
                </div>

                <div class="bottom">
                  <div class="capacity">
                    {{ item.currentParticipants }}/{{ item.maxParticipants === 0 ? '∞' : item.maxParticipants }}
                  </div>

                  <div class="actions" @click.stop>
                    <el-button size="small" round @click="goDetail(item.id)">
                      进入详情
                    </el-button>
                    <el-button
                      v-if="item.signupStatus === 1 && item.status === 1"
                      size="small"
                      type="success"
                      plain
                      @click="goDetail(item.id)"
                    >
                      去签到
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="pageNum"
              :page-size="pageSize"
              :total="total"
              layout="prev, pager, next"
              @current-change="fetch"
            />
          </div>
        </AlumniOnlyBlur>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import AppShellBar from '../components/AppShellBar.vue'
import AlumniOnlyBlur from '../components/AlumniOnlyBlur.vue'
import { activityApi } from '../api/activity'
import { useUserStore } from '../stores/user'
import { resolveAvatarUrl } from '../utils/avatarUrl'

const router = useRouter()
const userStore = useUserStore()
const isUserNonAlumni = computed(() => userStore.role === 'USER')

const loading = ref(false)
const list = ref([])
const total = ref(0)

const pageNum = ref(1)
const pageSize = ref(10)

const statusTag = (s) => {
  switch (s) {
    case 0:
      return { label: '待发布', type: 'info' }
    case 1:
      return { label: '进行中', type: 'success' }
    case 2:
      return { label: '已结束', type: 'default' }
    case 3:
      return { label: '已取消', type: 'danger' }
    default:
      return { label: '未知', type: 'info' }
  }
}

const mySignupTag = (item) => {
  // signupStatus 来自 participation.state：1-报名中,2-已签到
  if (item?.signupStatus === 2) return { label: '已签到', type: 'success' }
  if (item?.signupStatus === 1) return statusTag(item.status)
  if (item?.signupStatus === 3) return statusTag(item.status)
  return statusTag(item.status)
}

const fmt = (t) => {
  if (!t) return ''
  const d = new Date(t)
  if (Number.isNaN(d.getTime())) return String(t)
  const yyyy = d.getFullYear()
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mi = String(d.getMinutes()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd} ${hh}:${mi}`
}

const fetch = async () => {
  loading.value = true
  try {
    const res = await activityApi.getMySignupActivities({ pageNum: pageNum.value, pageSize: pageSize.value })
    const raw = res.data.list || []
    list.value = await Promise.all(
      raw.map(async (item) => ({
        ...item,
        coverDisplayUrl: item.coverImage ? await resolveAvatarUrl(item.coverImage) : ''
      }))
    )
    total.value = res.data.total || 0
  } catch (e) {
    console.error('获取我的报名活动失败:', e)
    ElMessage.error('获取我的报名活动失败，请重试')
  } finally {
    loading.value = false
  }
}

const goDetail = (id) => router.push(`/activity/${id}`)

onMounted(() => {
  fetch()
})
</script>

<style scoped>
.activity-layout { padding-top: 14px; }
.activity-hero { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; margin-bottom: 16px; }
.activity-kicker { font-size: 11px; font-weight: 700; letter-spacing: 0.14em; color: var(--accent); opacity: 0.85; text-transform: uppercase; margin-bottom: 8px; }
.activity-title { margin: 0 0 8px; font-size: 22px; font-weight: 800; letter-spacing: -0.02em; color: var(--ink); }
.activity-desc { margin: 0; color: var(--ink-muted); font-weight: 500; line-height: 1.5; font-size: 13px; }
.loading-block { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 260px; color: var(--ink-faint); font-weight: 600; gap: 10px; }
.activity-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 14px; }
.activity-card { overflow: hidden; border-radius: var(--radius-lg); border: 1px solid var(--border-subtle); background: var(--surface-solid); box-shadow: var(--shadow-sm); cursor: pointer; transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease; display: flex; flex-direction: column; }
.activity-card:hover { transform: translateY(-3px); box-shadow: var(--shadow-card-hover); border-color: rgba(79, 70, 229, 0.18); }
.cover { position: relative; height: 140px; }
.cover-img { width: 100%; height: 100%; }
.cover-overlay { position: absolute; left: 12px; top: 12px; }
.body { padding: 14px 14px 12px; display: flex; flex-direction: column; gap: 10px; flex: 1; }
.card-title { margin: 0; font-size: 15px; font-weight: 900; letter-spacing: -0.02em; color: var(--ink); line-height: 1.3; }
.meta { color: var(--ink-muted); font-size: 12px; font-weight: 650; }
.bottom { display: flex; align-items: flex-end; justify-content: space-between; gap: 12px; margin-top: auto; padding-top: 8px; border-top: 1px solid var(--border-subtle); }
.capacity { font-weight: 800; color: var(--ink); font-size: 13px; }
.actions { display: flex; gap: 8px; flex-wrap: wrap; justify-content: flex-end; }
.pagination-wrap { margin-top: 18px; display: flex; justify-content: flex-end; }
@media (max-width: 1100px) { .activity-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 720px) { .activity-grid { grid-template-columns: 1fr; } .activity-hero { flex-direction: column; align-items: flex-start; } }
</style>
