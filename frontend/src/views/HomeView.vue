<template>
  <div class="home-container">
    <el-card class="welcome-card">
      <template #header>
        <div class="card-header">
          <span>欢迎回来</span>
          <el-button type="primary" @click="handleLogout">退出登录</el-button>
        </div>
      </template>
      <div class="user-info">
        <el-avatar :size="80" :src="userStore.userInfo?.avatar || ''">{{ userStore.username?.[0] || '用' }}</el-avatar>
        <h2>{{ userStore.username }}</h2>
        <p v-if="userStore.userInfo?.phone">手机号: {{ userStore.userInfo.phone }}</p>
        <p v-if="userStore.userInfo?.email">邮箱: {{ userStore.userInfo.email }}</p>
        <p>角色: {{ userStore.role }}</p>
      </div>
      <el-divider />
      <div class="alumni-profile" v-if="alumniProfile">
        <h3>校友档案</h3>
        <p>真实姓名: {{ alumniProfile.realName }}</p>
        <p>认证状态: {{ alumniProfile.verifyStatus === 1 ? '已认证' : '未认证' }}</p>
        <p>是否本人: {{ alumniProfile.isSelf ? '是' : '否' }}</p>
        <el-button v-if="alumniProfile.verifyStatus !== 1" type="warning">完善认证信息</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { alumniApi } from '../api/alumni'

const router = useRouter()
const userStore = useUserStore()
const alumniProfile = ref(null)

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

const getCurrentProfile = async () => {
  try {
    const response = await alumniApi.getCurrentProfile()
    if (response.code === 200) {
      alumniProfile.value = response.data
    }
  } catch (error) {
    console.error('获取校友档案失败:', error)
  }
}

onMounted(() => {
  getCurrentProfile()
})
</script>

<style scoped>
.home-container {
  max-width: 800px;
  margin: 40px auto;
  padding: 0 20px;
}

.welcome-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  text-align: center;
  margin: 40px 0;
}

.user-info h2 {
  margin: 20px 0 10px;
}

.user-info p {
  color: #606266;
  margin: 5px 0;
}

.alumni-profile {
  margin-top: 20px;
}

.alumni-profile h3 {
  margin-bottom: 15px;
}

.alumni-profile p {
  margin: 8px 0;
}
</style>