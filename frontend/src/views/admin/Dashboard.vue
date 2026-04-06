<template>
  <div class="dashboard">
    <el-skeleton :rows="8" animated v-if="loading" />
    <el-card class="dashboard-card">
      <template #header>
        <div class="card-header">
          <span>系统概览</span>
        </div>
      </template>
      
      <!-- 统计卡片 -->
      <!-- 第一行：核心用户数据 -->
      <el-row :gutter="20" class="stat-row">
        <el-col :span="8">
          <el-card class="stat-card" @click="navigateTo('/admin/users')">
            <div class="stat-content">
              <el-icon class="stat-icon"><User /></el-icon>
              <div class="stat-info">
                <p class="stat-label">总用户数</p>
                <p class="stat-value">{{ dashboardData.totalUsers || 0 }}</p>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="stat-card" @click="navigateTo('/admin/users?status=active')">
            <div class="stat-content">
              <el-icon class="stat-icon"><UserFilled /></el-icon>
              <div class="stat-info">
                <p class="stat-label">活跃用户数</p>
                <p class="stat-value">{{ dashboardData.activeUsers || 0 }}</p>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="stat-card" @click="navigateTo('/admin/users?status=pending')">
            <div class="stat-content">
              <el-icon class="stat-icon"><Timer /></el-icon>
              <div class="stat-info">
                <p class="stat-label">待认证用户数</p>
                <p class="stat-value">{{ dashboardData.pendingVerifyUsers || 0 }}</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 第二行：动态数据 -->
      <el-row :gutter="20" class="stat-row">
        <el-col :span="12">
          <el-card class="stat-card" @click="navigateTo('/admin/content')">
            <div class="stat-content">
              <el-icon class="stat-icon"><Document /></el-icon>
              <div class="stat-info">
                <p class="stat-label">总动态数</p>
                <p class="stat-value">{{ dashboardData.totalPosts || 0 }}</p>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="stat-card" @click="navigateTo('/admin/content?status=pending')">
            <div class="stat-content">
              <el-icon class="stat-icon"><ChatLineSquare /></el-icon>
              <div class="stat-info">
                <p class="stat-label">待审核动态数</p>
                <p class="stat-value">{{ dashboardData.pendingAuditPosts || 0 }}</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 第三行：活动数据 -->
      <el-row :gutter="20" class="stat-row">
        <el-col :span="12">
          <el-card class="stat-card" @click="navigateTo('/admin/activities')">
            <div class="stat-content">
              <el-icon class="stat-icon"><Calendar /></el-icon>
              <div class="stat-info">
                <p class="stat-label">总活动数</p>
                <p class="stat-value">{{ dashboardData.totalActivities || 0 }}</p>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="stat-card" @click="navigateTo('/admin/activities?status=ongoing')">
            <div class="stat-content">
              <el-icon class="stat-icon"><Clock /></el-icon>
              <div class="stat-info">
                <p class="stat-label">进行中活动数</p>
                <p class="stat-value">{{ dashboardData.ongoingActivities || 0 }}</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 第四行：评论数据 -->
      <!-- 评论审核功能已移除 -->
      
      <!-- 第五行：互动数据 -->
      <el-row :gutter="20" class="stat-row">
        <el-col :span="12">
          <el-card class="stat-card">
            <div class="stat-content">
              <el-icon class="stat-icon"><Connection /></el-icon>
              <div class="stat-info">
                <p class="stat-label">总互动数</p>
                <p class="stat-value">{{ dashboardData.totalInteractions || 0 }}</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, UserFilled, Timer, Document, ChatLineSquare, Calendar, Clock, Connection } from '@element-plus/icons-vue'
import adminApi from '../../api/admin'

const router = useRouter()

// 仪表盘数据
const dashboardData = ref({})
const loading = ref(false)

// 导航到指定页面
const navigateTo = (path) => {
  router.push(path)
}

// 获取仪表盘数据
const getDashboardData = async () => {
  try {
    loading.value = true
    const response = await adminApi.getDashboardData()
    dashboardData.value = response.data
  } catch (error) {
    console.error('获取仪表盘数据失败:', error)
    ElMessage.error('获取仪表盘数据失败，请重试')
  } finally {
    loading.value = false
  }
}

// 定时刷新数据
let refreshInterval = null

// 页面挂载时获取数据并设置定时刷新
onMounted(() => {
  getDashboardData()
  // 每60秒刷新一次数据
  refreshInterval = setInterval(getDashboardData, 60000)
})

// 页面卸载时清除定时器
onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
  }
})
</script>

<style scoped>
.dashboard {
  padding: 20px 0;
}

.dashboard-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 统计卡片样式 */
.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  transition: all 0.3s ease;
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  font-size: 32px;
  color: #1890ff;
  margin-right: 20px;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  margin: 0 0 5px 0;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stat-row {
    display: flex;
    flex-direction: column;
  }
  
  .stat-row .el-col {
    width: 100%;
    margin-bottom: 20px;
  }
}
</style>