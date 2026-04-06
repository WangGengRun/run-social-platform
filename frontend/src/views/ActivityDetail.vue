<template>
  <div class="activity-detail-page">
    <AppShellBar />

    <main class="alumni-page activity-detail-layout">
      <div class="top-bar">
        <el-button text @click="router.back()">返回</el-button>
        <div class="top-actions">
          <el-button round @click="goParticipants">
            报名名单
          </el-button>
        </div>
      </div>

      <div v-if="loading" class="loading-block">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <div v-else-if="detail">
        <AlumniOnlyBlur :locked="isUserNonAlumni">
        <div class="detail-card">
          <div class="cover">
            <el-image :src="detail.coverDisplayUrl || detail.coverImage" fit="cover" class="cover-img" />
            <div class="cover-grad" />
            <div class="cover-content">
              <div class="status-row">
                <el-tag size="small" :type="statusTag(detail.status).type">
                  {{ statusTag(detail.status).label }}
                </el-tag>
                <el-tag v-if="detail.joined" size="small" type="success" effect="dark">
                  已报名
                </el-tag>
              </div>
              <h1 class="title">{{ detail.title }}</h1>
              <div class="subtitle">
                <span>地点：{{ detail.location }}</span>
                <span class="dot">·</span>
                <span>{{ fmt(detail.startTime) }} - {{ fmt(detail.endTime) }}</span>
              </div>
            </div>
          </div>

          <div class="detail-body">
            <div class="two-col">
              <section class="section card-surface">
                <div class="section-title">活动介绍</div>
                <div class="section-content">
                  <p class="desc">{{ detail.description }}</p>
                  <div class="info-row">
                    <span class="info-item">
                      参与人数：{{ detail.currentParticipants }}/{{ detail.maxParticipants === 0 ? '∞' : detail.maxParticipants }}
                    </span>
                  </div>
                </div>
              </section>

              <section class="section card-surface">
                <div class="section-title">组织者</div>
                <div class="organizer">
                  <ResolvedAvatar :size="54" :src="detail.organizerAvatar || ''" />
                  <div class="organizer-meta">
                    <div class="organizer-name">{{ detail.organizerName }}</div>
                    <div class="organizer-hint">欢迎与组织者沟通</div>
                  </div>
                </div>

                <div class="divider" />

                <div class="action-stack">
                  <el-button
                    v-if="detail.status === 1 && !detail.joined"
                    type="primary"
                    :loading="btnLoading.signup"
                    :disabled="!canSignup"
                    :title="!canSignup ? '仅可在活动开始前报名' : ''"
                    @click="signup"
                  >
                    报名活动
                  </el-button>

                  <el-button
                    v-if="detail.status === 1 && detail.joined"
                    type="danger"
                    plain
                    :loading="btnLoading.cancelSignup"
                    @click="cancelSignup"
                  >
                    取消报名
                  </el-button>

                  <el-button
                    v-if="detail.status === 1 && detail.joined"
                    type="success"
                    plain
                    :loading="btnLoading.checkin"
                    :disabled="!canCheckin"
                    :title="checkinDisabledReason"
                    @click="checkin"
                  >
                    签到
                  </el-button>

                  <el-button
                    v-if="isOrganizer && detail.status === 0"
                    type="primary"
                    round
                    :loading="btnLoading.publish"
                    @click="publish"
                  >
                    发布活动
                  </el-button>

                  <el-button
                    v-if="isOrganizer && detail.status === 1"
                    type="danger"
                    round
                    :loading="btnLoading.cancel"
                    @click="cancel"
                  >
                    取消活动
                  </el-button>
                </div>
              </section>
            </div>
          </div>
        </div>
        </AlumniOnlyBlur>
      </div>

      <el-empty v-else description="活动不存在或不可访问" />
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
import { resolveAvatarUrl } from '../utils/avatarUrl'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isUserNonAlumni = computed(() => userStore.role === 'USER')

const loading = ref(true)
const detail = ref(null)
const isOrganizer = ref(false)

const btnLoading = ref({
  signup: false,
  publish: false,
  cancel: false,
  cancelSignup: false,
  checkin: false
})

const canSignup = computed(() => {
  if (!detail.value || detail.value.status !== 1 || detail.value.joined) return false
  const now = Date.now()
  const start = new Date(detail.value.startTime).getTime()
  if (Number.isNaN(start)) return false
  return now < start
})

const canCheckin = computed(() => {
  if (!detail.value || detail.value.status !== 1 || !detail.value.joined) return false
  const now = Date.now()
  const start = new Date(detail.value.startTime).getTime()
  const end = new Date(detail.value.endTime).getTime()
  if (Number.isNaN(start) || Number.isNaN(end)) return false
  return now >= start && now <= end
})

const checkinDisabledReason = computed(() => {
  if (canCheckin.value) return ''
  if (!detail.value?.joined) return ''
  const now = Date.now()
  const start = new Date(detail.value.startTime).getTime()
  const end = new Date(detail.value.endTime).getTime()
  if (now < start) return '活动尚未开始，无法签到'
  if (now > end) return '活动已结束，无法签到'
  return ''
})

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

const getId = () => Number(route.params.id)

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await activityApi.getActivityDetail(getId())
    const d = res.data || null
    if (d) {
      d.coverDisplayUrl = d.coverImage ? await resolveAvatarUrl(d.coverImage) : ''
    }
    detail.value = d

    // 判断是否组织者：用“我的活动”列表做一次侧判断
    const my = await activityApi.getMyActivities({ pageNum: 1, pageSize: 50 })
    const myList = my.data?.list || []
    isOrganizer.value = myList.some(x => Number(x.id) === Number(getId()))
  } catch (e) {
    console.error('获取活动详情失败:', e)
    ElMessage.error('获取活动详情失败，请重试')
    detail.value = null
  } finally {
    loading.value = false
  }
}

const goParticipants = () => router.push(`/activity/${getId()}/participants`)

const signup = async () => {
  if (userStore.role === 'USER') {
    ElMessage.warning('请先完成校友认证，认证后可报名活动')
    return
  }
  if (!canSignup.value) {
    ElMessage.warning('仅可在活动开始前报名')
    return
  }
  btnLoading.value.signup = true
  try {
    await activityApi.signupActivity(getId())
    ElMessage.success('报名成功')
    await fetchDetail()
  } catch (e) {
    console.error('报名失败:', e)
    ElMessage.error('报名失败，请重试')
  } finally {
    btnLoading.value.signup = false
  }
}

const cancelSignup = async () => {
  if (userStore.role === 'USER') {
    ElMessage.warning('请先完成校友认证')
    return
  }
  btnLoading.value.cancelSignup = true
  try {
    await activityApi.cancelSignupActivity(getId())
    ElMessage.success('已取消报名')
    await fetchDetail()
  } catch (e) {
    console.error('取消报名失败:', e)
    ElMessage.error('取消报名失败，请重试')
  } finally {
    btnLoading.value.cancelSignup = false
  }
}

const checkin = async () => {
  if (userStore.role === 'USER') {
    ElMessage.warning('请先完成校友认证')
    return
  }
  if (!canCheckin.value) {
    ElMessage.warning(checkinDisabledReason.value || '当前不可签到')
    return
  }
  btnLoading.value.checkin = true
  try {
    await activityApi.checkinActivity(getId())
    ElMessage.success('签到成功')
    await fetchDetail()
  } catch (e) {
    console.error('签到失败:', e)
    ElMessage.error('签到失败，请重试')
  } finally {
    btnLoading.value.checkin = false
  }
}

const publish = async () => {
  if (userStore.role === 'USER') {
    ElMessage.warning('请先完成校友认证')
    return
  }
  btnLoading.value.publish = true
  try {
    await activityApi.publishActivity(getId())
    ElMessage.success('发布成功')
    await fetchDetail()
  } catch (e) {
    console.error('发布失败:', e)
    ElMessage.error('发布失败，请重试')
  } finally {
    btnLoading.value.publish = false
  }
}

const cancel = async () => {
  if (userStore.role === 'USER') {
    ElMessage.warning('请先完成校友认证')
    return
  }
  btnLoading.value.cancel = true
  try {
    await activityApi.cancelActivity(getId())
    ElMessage.success('活动已取消')
    await fetchDetail()
  } catch (e) {
    console.error('取消活动失败:', e)
    ElMessage.error('取消活动失败，请重试')
  } finally {
    btnLoading.value.cancel = false
  }
}

onMounted(() => {
  fetchDetail()
})

watch(() => route.params.id, () => {
  fetchDetail()
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
  gap: 12px;
  margin-bottom: 12px;
}

.loading-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 280px;
  color: var(--ink-faint);
  font-weight: 600;
  gap: 10px;
}
.loading-block .el-icon { font-size: 34px; }

.detail-card {
  overflow: hidden;
  border-radius: var(--radius-xl);
  border: 1px solid var(--border-subtle);
  background: var(--surface-solid);
  box-shadow: var(--shadow-card);
}

.cover {
  position: relative;
  height: 240px;
  background: #f3f4f6;
}

.cover-img {
  width: 100%;
  height: 100%;
}

.cover-grad {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.12) 0%, rgba(0, 0, 0, 0.55) 100%);
}

.cover-content {
  position: absolute;
  left: 20px;
  right: 20px;
  bottom: 18px;
  color: #fff;
}

.status-row {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.title {
  margin: 0 0 8px;
  font-size: 26px;
  font-weight: 900;
  letter-spacing: -0.03em;
}

.subtitle {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  color: rgba(255, 255, 255, 0.92);
  font-weight: 600;
  font-size: 13px;
}

.dot { opacity: 0.7; }

.detail-body {
  padding: 18px;
}

.two-col {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 14px;
}

.section {
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-subtle);
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  padding: 16px;
  box-shadow: var(--shadow-sm);
}

.card-surface {
  background: rgba(255, 255, 255, 0.86);
}

.section-title {
  font-size: 14px;
  font-weight: 900;
  letter-spacing: -0.02em;
  color: var(--ink);
  margin-bottom: 10px;
}

.desc {
  margin: 0;
  color: var(--ink-muted);
  line-height: 1.7;
  font-weight: 500;
}

.info-row {
  margin-top: 14px;
}

.info-item {
  font-size: 13px;
  font-weight: 700;
  color: var(--ink);
}

.organizer {
  display: flex;
  align-items: center;
  gap: 12px;
}

.organizer-name {
  font-weight: 900;
  color: var(--ink);
  font-size: 14px;
}

.organizer-hint {
  margin-top: 3px;
  color: var(--ink-faint);
  font-weight: 600;
  font-size: 12px;
}

.divider {
  height: 1px;
  background: var(--border-subtle);
  margin: 14px 0;
}

.action-stack {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

@media (max-width: 980px) {
  .two-col { grid-template-columns: 1fr; }
  .cover { height: 220px; }
  .title { font-size: 22px; }
}
</style>

