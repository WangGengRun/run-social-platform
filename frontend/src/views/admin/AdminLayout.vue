<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside width="200px" class="sidebar">
      <div class="logo">
        <h3>遇见校友</h3>
        <p>后台管理系统</p>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        router
        @select="handleMenuSelect"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><House /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/alumni-verify">
          <el-icon><DataAnalysis /></el-icon>
          <span>校友认证审核</span>
        </el-menu-item>
        <el-menu-item index="/admin/content">
          <el-icon><Document /></el-icon>
          <span>动态审核</span>
        </el-menu-item>
        <el-menu-item index="/admin/activities">
          <el-icon><Calendar /></el-icon>
          <span>活动管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/statistics">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据统计</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部栏 -->
      <el-header class="top-bar">
        <div class="top-bar-left">
          <h2 class="admin-title">系统管理后台</h2>
          <span class="current-page">{{ currentPageTitle }}</span>
        </div>
        <div class="top-bar-right">
          <el-button type="text" @click="handleGoToFrontend">
            <el-icon><House /></el-icon>
            <span>返回前台</span>
          </el-button>
          <span class="admin-info">管理员</span>
          <el-button type="text" @click="handleLogout" :loading="loading">
            <el-icon><SwitchButton /></el-icon>
            <span>退出登录</span>
          </el-button>
        </div>
      </el-header>
      
      <!-- 内容区 -->
      <el-main class="content">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { House, User, Document, Calendar, DataAnalysis, SwitchButton } from '@element-plus/icons-vue'
import adminApi from '../../api/admin'

const router = useRouter()
const route = useRoute()
const loading = ref(false)

// 计算当前激活的菜单
const activeMenu = computed(() => {
  return route.fullPath
})

// 页面标题映射
const pageTitles = {
  '/admin/dashboard': '仪表盘',
  '/admin/users': '用户管理',
  '/admin/alumni-verify': '校友认证审核',
  '/admin/content': '动态审核',
  '/admin/activities': '活动管理',
  '/admin/statistics': '数据统计'
}

// 当前页面标题
const currentPageTitle = computed(() => {
  return pageTitles[route.fullPath] || '仪表盘'
})

// 处理菜单选择
const handleMenuSelect = (key, keyPath) => {
  console.log('菜单选择:', key, keyPath)
}

// 处理返回前台
const handleGoToFrontend = () => {
  router.push('/')
}

// 处理退出登录
const handleLogout = async () => {
  try {
    loading.value = true
    // 调用后端登出API
    await adminApi.logout()
  } catch (error) {
    console.error('登出错误:', error)
    // 即使后端登出失败，也清除本地token
  } finally {
    // 清除本地存储的token
    localStorage.removeItem('token')
    ElMessage.success('退出登录成功')
    router.push('/login')
    loading.value = false
  }
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background-color: #f5f7fa;
}

/* 侧边栏样式 */
.sidebar {
  background-color: #2c3e50;
  color: #fff;
  box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35);
}

.logo {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid #3a4e63;
}

.logo h3 {
  margin: 0 0 5px 0;
  font-size: 18px;
}

.logo p {
  margin: 0;
  font-size: 12px;
  color: #a0a0a0;
}

.sidebar-menu {
  background-color: transparent;
  border-right: none;
}

.sidebar-menu .el-menu-item {
  color: #fff;
  height: 60px;
  line-height: 60px;
  margin: 0;
}

.sidebar-menu .el-menu-item:hover {
  background-color: #3a4e63;
}

.sidebar-menu .el-menu-item.is-active {
  background-color: #1890ff;
  color: #fff;
}

/* 顶部栏样式 */
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  height: 60px;
}

.top-bar-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.admin-title {
  font-size: 18px;
  font-weight: bold;
  color: #1890ff;
  margin: 0;
}

.current-page {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.top-bar-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.admin-info {
  color: #606266;
}

/* 内容区样式 */
.content {
  padding: 20px;
  background-color: #f5f7fa;
}
</style>