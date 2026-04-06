<template>
  <div class="activity-page">
    <AppShellBar />

    <main class="alumni-page activity-layout">
      <div class="activity-hero">
        <div class="activity-hero-left">
          <div class="activity-kicker">activities</div>
          <h1 class="activity-title">活动中心</h1>
          <p class="activity-desc">查看最新活动、报名参与、管理你的创建活动</p>
        </div>

        <div class="activity-hero-right">
          <el-button type="primary" round @click="goCreate">
            创建活动
          </el-button>
          <el-button round @click="goMy">
            我的活动
          </el-button>
          <el-button round @click="goMySignups">
            我的报名
          </el-button>
        </div>
      </div>

      <AlumniOnlyBlur :locked="isUserNonAlumni">
      <div class="activity-filters">
        <el-select
          v-model="status"
          placeholder="状态：全部"
          class="filter-item"
          clearable
        >
          <el-option :value="0" label="待发布" />
          <el-option :value="1" label="进行中" />
          <el-option :value="2" label="已结束" />
          <el-option :value="3" label="已取消" />
        </el-select>

        <el-input
          v-model="keyword"
          placeholder="搜索标题/地点..."
          class="filter-item"
          clearable
        />

        <el-button type="primary" class="filter-item" @click="refresh">
          搜索
        </el-button>
      </div>

      <div v-if="loading" class="loading-block">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <div v-else>
        <el-empty v-if="list.length === 0" description="暂无活动" />

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
                <el-tag size="small" :type="statusTag(item.status).type">
                  {{ statusTag(item.status).label }}
                </el-tag>
              </div>
            </div>

            <div class="body">
              <div class="row">
                <h3 class="card-title">{{ item.title }}</h3>
              </div>
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
                  <el-button
                    v-if="item.status === 1 && !item.joined"
                    size="small"
                    type="primary"
                    :loading="btnLoadingMap[item.id]"
                    :disabled="!canSignupFor(item)"
                    :title="!canSignupFor(item) ? '仅可在活动开始前报名' : ''"
                    @click="signup(item.id)"
                  >
                    报名
                  </el-button>
                  <el-button
                    v-if="item.status === 1 && item.joined"
                    size="small"
                    type="danger"
                    plain
                    :loading="btnLoadingMap[item.id]"
                    @click="cancelSignup(item.id)"
                  >
                    取消报名
                  </el-button>
                  <el-button size="small" @click="goDetail(item.id)">
                    详情
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
      </div>
      </AlumniOnlyBlur>
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
const pageSize = ref(12)

const status = ref(null)
const keyword = ref('')

const btnLoadingMap = ref({})

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

const canSignupFor = (item) => {
  if (!item || item.status !== 1 || item.joined) return false
  const now = Date.now()
  const start = new Date(item.startTime).getTime()
  if (Number.isNaN(start)) return false
  return now < start
}

const fetch = async () => {
  loading.value = true
  try {
    const res = await activityApi.getActivityList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      status: status.value,
      keyword: keyword.value || undefined
    })
    const raw = res.data.list || []
    list.value = await Promise.all(
      raw.map(async (item) => ({
        ...item,
        coverDisplayUrl: item.coverImage ? await resolveAvatarUrl(item.coverImage) : ''
      }))
    )
    total.value = res.data.total || 0
  } catch (e) {
    console.error('获取活动列表失败:', e)
    ElMessage.error('获取活动列表失败，请重试')
  } finally {
    loading.value = false
  }
}

const refresh = () => {
  pageNum.value = 1
  fetch()
}

const goCreate = () => router.push('/activity/create')
const goMy = () => router.push('/activity/my')
const goMySignups = () => router.push('/activity/my-signups')
const goDetail = (id) => router.push(`/activity/${id}`)

const signup = async (id) => {
  if (userStore.role === 'USER') {
    ElMessage.warning('请先完成校友认证，认证后可报名活动')
    return
  }
  const row = list.value.find((x) => Number(x.id) === Number(id))
  if (row && !canSignupFor(row)) {
    ElMessage.warning('仅可在活动开始前报名')
    return
  }
  btnLoadingMap.value = { ...btnLoadingMap.value, [id]: true }
  try {
    await activityApi.signupActivity(id)
    ElMessage.success('报名成功')
    fetch()
  } catch (e) {
    console.error('报名失败:', e)
    ElMessage.error('报名失败，请重试')
  } finally {
    btnLoadingMap.value = { ...btnLoadingMap.value, [id]: false }
  }
}

const cancelSignup = async (id) => {
  if (userStore.role === 'USER') {
    ElMessage.warning('请先完成校友认证')
    return
  }
  btnLoadingMap.value = { ...btnLoadingMap.value, [id]: true }
  try {
    await activityApi.cancelSignupActivity(id)
    ElMessage.success('已取消报名')
    fetch()
  } catch (e) {
    console.error('取消报名失败:', e)
    ElMessage.error('取消报名失败，请重试')
  } finally {
    btnLoadingMap.value = { ...btnLoadingMap.value, [id]: false }
  }
}

onMounted(() => {
  fetch()
})
</script>

<style scoped>
.activity-layout {
  padding-top: 14px;
}

.activity-hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.activity-kicker {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.14em;
  color: var(--accent);
  opacity: 0.85;
  text-transform: uppercase;
  margin-bottom: 8px;
}

.activity-title {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--ink);
}

.activity-desc {
  margin: 0;
  color: var(--ink-muted);
  font-weight: 500;
  line-height: 1.5;
  font-size: 13px;
}

.activity-hero-right {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.activity-filters {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.filter-item {
  min-width: 220px;
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

.loading-block .el-icon {
  font-size: 34px;
}

.activity-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.activity-card {
  overflow: hidden;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-subtle);
  background: var(--surface-solid);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
  display: flex;
  flex-direction: column;
}

.activity-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-card-hover);
  border-color: rgba(79, 70, 229, 0.18);
}

.cover {
  position: relative;
  height: 138px;
  background: #f3f4f6;
}

.cover-img {
  width: 100%;
  height: 100%;
}

.cover-overlay {
  position: absolute;
  left: 12px;
  top: 12px;
}

.body {
  padding: 14px 14px 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
}

.card-title {
  margin: 0;
  font-size: 15px;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--ink);
  line-height: 1.3;
}

.meta {
  color: var(--ink-muted);
  font-size: 12px;
  font-weight: 600;
  line-height: 1.35;
}

.bottom {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
  margin-top: auto;
  padding-top: 6px;
  border-top: 1px solid var(--border-subtle);
}

.capacity {
  font-weight: 800;
  color: var(--ink);
  font-size: 13px;
}

.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.pagination-wrap {
  margin-top: 18px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 1100px) {
  .activity-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  .filter-item {
    min-width: 100%;
  }
  .activity-grid {
    grid-template-columns: 1fr;
  }
}
</style>

