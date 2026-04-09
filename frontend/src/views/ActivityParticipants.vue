<template>
  <div class="activity-participants-page">
    <AppShellBar />

    <main class="alumni-page activity-detail-layout">
      <div class="top-bar">
        <el-button text @click="router.back()">返回</el-button>
        <div class="top-right">
          <el-tag size="small" :type="statusTag(activity?.status).type">
            {{ statusTag(activity?.status).label }}
          </el-tag>
        </div>
      </div>

      <div v-if="loading" class="loading-block">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <div v-else>
        <AlumniOnlyBlur :locked="isUserNonAlumni">
        <div class="participants-card">
          <div class="participants-header">
            <div>
              <div class="participants-title">报名名单</div>
              <div class="participants-subtitle">
                活动：{{ activity?.title || '未知' }}
              </div>
            </div>
          </div>

          <div class="participants-list">
            <div
              v-for="p in participants"
              :key="p.userId"
              class="participant-row"
              :class="{ me: p.userId === userId }"
            >
              <ResolvedAvatar :size="44" :src="p.avatar || ''" />

              <div class="p-meta">
                <div class="p-name">
                  {{ p.username || p.realName }}
                  <el-tag v-if="p.userId === userId" size="small" type="success" effect="dark">
                    我
                  </el-tag>
                </div>
                <div class="p-hint">
                  <span>{{ statusLabel(p.status) }}</span>
                  <span class="sep">·</span>
                  <span v-if="p.createdAt">报名：{{ fmt(p.createdAt) }}</span>
                  <span v-else>报名：-</span>
                  <span v-if="p.checkinTime" class="sep">·</span>
                  <span v-if="p.checkinTime">签到：{{ fmt(p.checkinTime) }}</span>
                </div>
              </div>

              <div class="p-action">
                <el-button
                  v-if="activity?.status === 1 && p.userId === userId && p.status === 1"
                  size="small"
                  type="primary"
                  round
                  :loading="btnLoading.checkin"
                  @click="checkin"
                >
                  签到
                </el-button>
                <span v-else class="no-action">-</span>
              </div>
            </div>

            <el-empty v-if="participants.length === 0" description="暂无报名" />
          </div>
        </div>

        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="pageNum"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="fetchParticipants"
          />
        </div>
        </AlumniOnlyBlur>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import AppShellBar from '../components/AppShellBar.vue'
import AlumniOnlyBlur from '../components/AlumniOnlyBlur.vue'
import ResolvedAvatar from '../components/ResolvedAvatar.vue'
import { activityApi } from '../api/activity'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isUserNonAlumni = computed(() => userStore.role === 'USER')

const userId = (() => {
  const v = Number(userStore.userId)
  return Number.isNaN(v) ? null : v
})()

const activityId = ref(Number(route.params.id))
const loading = ref(true)

const activity = ref(null)
const participants = ref([])
const total = ref(0)

const pageNum = ref(1)
const pageSize = ref(20)

const btnLoading = ref({ checkin: false })

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

const statusLabel = (s) => {
  switch (s) {
    case 1:
      return '报名中'
    case 2:
      return '已参加'
    case 3:
      return '已取消'
    default:
      return '未知'
  }
}

const fetchActivity = async () => {
  const res = await activityApi.getActivityDetail(activityId.value)
  activity.value = res.data || null
}

const fetchParticipants = async () => {
  loading.value = true
  try {
    const res = await activityApi.getParticipants({
      activityId: activityId.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    participants.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error('获取报名名单失败:', e)
    ElMessage.error('获取报名名单失败，请重试')
  } finally {
    loading.value = false
  }
}

const checkin = async () => {
  if (userStore.role === 'USER') {
    ElMessage.warning('请先完成校友认证')
    return
  }
  if (btnLoading.value.checkin) return
  btnLoading.value.checkin = true
  try {
    await activityApi.checkinActivity(activityId.value)
    ElMessage.success('签到成功')
    await fetchParticipants()
    await fetchActivity()
  } catch (e) {
    console.error('签到失败:', e)
    ElMessage.error('签到失败，请重试')
  } finally {
    btnLoading.value.checkin = false
  }
}

watch(
  () => route.params.id,
  (newId) => {
    activityId.value = Number(newId)
    pageNum.value = 1
    loading.value = true
    fetchActivity().finally(fetchParticipants)
  }
)

onMounted(async () => {
  await fetchActivity()
  await fetchParticipants()
})
</script>

<style scoped>
.activity-detail-layout {
  padding-top: 14px;
  padding-bottom: 48px;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.loading-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 260px;
  color: var(--ink-faint);
  font-weight: 700;
}

.participants-card {
  border-radius: var(--radius-xl);
  border: 1px solid var(--border-subtle);
  background: var(--surface-solid);
  box-shadow: var(--shadow-card);
  overflow: hidden;
}

.participants-header {
  padding: 18px 18px 14px;
  border-bottom: 1px solid var(--border-subtle);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.92) 0%, rgba(248, 249, 252, 0.96) 100%);
}

.participants-title {
  font-size: 18px;
  font-weight: 900;
  letter-spacing: -0.02em;
  color: var(--ink);
}

.participants-subtitle {
  margin-top: 6px;
  color: var(--ink-muted);
  font-weight: 600;
  font-size: 13px;
}

.participants-list {
  padding: 6px 8px 14px;
}

.participant-row {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 14px 14px;
  border-radius: var(--radius-md);
  margin: 10px 8px;
  border: 1px solid rgba(12, 18, 34, 0.06);
  background: rgba(255, 255, 255, 0.75);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.participant-row.me {
  border-color: rgba(79, 70, 229, 0.24);
  box-shadow: 0 10px 28px rgba(79, 70, 229, 0.14);
}

.p-meta {
  flex: 1;
  min-width: 0;
}

.p-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 900;
  color: var(--ink);
}

.p-hint {
  margin-top: 6px;
  color: var(--ink-muted);
  font-weight: 650;
  font-size: 12px;
  line-height: 1.4;
  word-break: break-word;
}

.sep {
  margin: 0 6px;
  opacity: 0.7;
}

.p-action {
  flex-shrink: 0;
}

.no-action {
  color: var(--ink-faint);
  font-weight: 800;
}

.pagination-wrap {
  margin-top: 18px;
  display: flex;
  justify-content: flex-end;
}
</style>

