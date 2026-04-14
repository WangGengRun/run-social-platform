<template>
  <div class="feed-search-wrap">
    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索动态/校友（昵称）"
        clearable
        @keyup.enter="doSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" :loading="loading" :disabled="!keyword.trim()" @click="doSearch">
        搜索
      </el-button>
    </div>

    <el-drawer
      v-model="drawerVisible"
      title="搜索结果"
      direction="rtl"
      size="420px"
      :with-header="true"
      :modal="false"
      :lock-scroll="false"
      class="search-drawer"
    >
      <el-tabs v-model="activeTab" class="result-tabs">
        <el-tab-pane label="动态" name="posts">
          <div v-if="loading" class="state"><el-icon class="is-loading"><Loading /></el-icon> 加载中...</div>
          <div v-else-if="postList.length === 0" class="state"><el-empty description="没有找到相关动态" /></div>
          <div v-else class="list">
            <div v-for="p in postList" :key="p.id" class="item" @click="goPost(p.id)">
              <div class="title-row">
                <div class="title">{{ p.content }}</div>
              </div>
              <div class="meta">
                <span class="author">{{ p.username || p.realName || '用户' }}</span>
                <span class="sep">·</span>
                <span class="time">{{ fmt(p.createdAt) }}</span>
              </div>
            </div>
            <div class="pager">
              <el-pagination
                v-model:current-page="postPage.pageNum"
                :page-size="postPage.pageSize"
                :total="postPage.total"
                layout="prev, pager, next"
                @current-change="fetchPosts"
              />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane v-if="showAlumniTab" label="校友" name="alumni">
          <div v-if="loading" class="state"><el-icon class="is-loading"><Loading /></el-icon> 加载中...</div>
          <div v-else-if="alumniList.length === 0" class="state"><el-empty description="没有找到相关校友" /></div>
          <div v-else class="list">
            <div v-for="u in alumniList" :key="u.userId" class="item" @click="goUser(u.userId)">
              <div class="title-row">
                <ResolvedAvatar :size="34" :src="u.avatar || ''" />
                <div class="title">
                  {{ u.username || u.realName || '用户' }}
                </div>
              </div>
              <div class="meta">
                <span v-if="u.college">{{ u.college }}</span>
                <span v-if="u.major"> · {{ u.major }}</span>
              </div>
            </div>
            <div class="pager">
              <el-pagination
                v-model:current-page="alumniPage.pageNum"
                :page-size="alumniPage.pageSize"
                :total="alumniPage.total"
                layout="prev, pager, next"
                @current-change="fetchAlumni"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, Search } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { searchApi } from '../api/search'
import ResolvedAvatar from './ResolvedAvatar.vue'

const router = useRouter()
const userStore = useUserStore()
const showAlumniTab = computed(() => userStore.role === 'ALUMNI')

const keyword = ref('')
const drawerVisible = ref(false)
const activeTab = ref('posts')
const loading = ref(false)

const postList = ref([])
const alumniList = ref([])

const postPage = ref({ pageNum: 1, pageSize: 10, total: 0 })
const alumniPage = ref({ pageNum: 1, pageSize: 10, total: 0 })

const fmt = (t) => {
  if (!t) return ''
  const d = new Date(t)
  if (Number.isNaN(d.getTime())) return String(t)
  return `${d.getMonth() + 1}-${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const fetchPosts = async () => {
  const k = keyword.value.trim()
  if (!k) return
  const res = await searchApi.searchPosts(k, postPage.value.pageNum, postPage.value.pageSize)
  const data = res.data || {}
  postList.value = data.records || data.list || []
  postPage.value.total = Number(data.total || 0)
}

const fetchAlumni = async () => {
  if (!showAlumniTab.value) return
  const k = keyword.value.trim()
  if (!k) return
  const res = await searchApi.searchAlumni(k, alumniPage.value.pageNum, alumniPage.value.pageSize)
  const data = res.data || {}
  alumniList.value = data.records || data.list || []
  alumniPage.value.total = Number(data.total || 0)
}

const doSearch = async () => {
  const k = keyword.value.trim()
  if (!k) return
  drawerVisible.value = true
  activeTab.value = 'posts'
  loading.value = true
  postPage.value.pageNum = 1
  alumniPage.value.pageNum = 1
  try {
    await fetchPosts()
    if (showAlumniTab.value) {
      await fetchAlumni()
    } else {
      alumniList.value = []
      alumniPage.value.total = 0
    }
  } catch (e) {
    console.error('search failed:', e)
    ElMessage.error(e?.message || '搜索失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const goPost = (id) => {
  drawerVisible.value = false
  router.push(`/post/${id}`)
}

const goUser = (id) => {
  drawerVisible.value = false
  router.push(`/profile/${id}`)
}
</script>

<style scoped>
.feed-search-wrap {
  width: 100%;
}

.search-bar {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 10px 0 6px;
}

.search-bar :deep(.el-input__wrapper) {
  border-radius: 999px;
}

.search-drawer :deep(.el-drawer) {
  border-radius: 16px 0 0 16px;
  box-shadow: 0 18px 60px rgba(12, 18, 34, 0.18);
}

.state {
  padding: 18px 6px;
  display: flex;
  gap: 10px;
  align-items: center;
  color: #909399;
  font-weight: 600;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.item {
  padding: 12px 12px;
  border: 1px solid rgba(12, 18, 34, 0.06);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.9);
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease, border-color 0.15s ease;
}

.item:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 24px rgba(12, 18, 34, 0.08);
  border-color: rgba(79, 70, 229, 0.18);
}

.title-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title {
  font-weight: 800;
  color: #303133;
  font-size: 14px;
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meta {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.sep {
  opacity: 0.7;
}

.pager {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}
</style>

