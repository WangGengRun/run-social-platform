<template>
  <div class="login-container">
    <div class="login-form-wrapper">
      <h1 class="login-title">校友社交平台</h1>
      <h2 class="login-subtitle">用户登录</h2>
      
      <el-form
        :model="loginForm"
        :rules="loginRules"
        ref="loginFormRef"
        label-width="80px"
        class="login-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            size="large"
          />
        </el-form-item>
        
        <el-form-item>
          <el-checkbox v-model="loginForm.rememberPassword">记住密码</el-checkbox>
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleLogin"
            size="large"
            class="login-button"
          >
            登录
          </el-button>
        </el-form-item>
        
        <el-form-item class="register-link">
          <span>还没有账号？</span>
          <el-link type="primary" @click="goToRegister">立即注册</el-link>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { useMessageStore } from '../stores/message'
import { authApi } from '../api/auth'
import websocketManager from '../utils/websocket-manager'

const router = useRouter()
const userStore = useUserStore()
const messageStore = useMessageStore()
const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  rememberPassword: userStore.rememberPassword
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    const response = await authApi.login({
      username: loginForm.username,
      password: loginForm.password
    })
    
    if (response.code === 200) {
      // 保存登录状态
      userStore.login({
        token: response.data.token,
        userId: response.data.userId,
        username: response.data.username,
        role: response.data.role,
        userInfo: response.data.userInfo
      })

      // 设置记住密码
      userStore.setRememberPassword(loginForm.rememberPassword)

      // 连接 WebSocket
      messageStore.connectWebSocket(response.data.token)

      ElMessage.success('登录成功')
      
      // 使用 nextTick 确保状态更新后再跳转
      await new Promise(resolve => setTimeout(resolve, 100))
      
      // 根据角色决定跳转路径
      if (response.data.role === 'ADMIN') {
        await router.push('/admin/dashboard')
      } else {
        await router.push('/')
      }
    }
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}

const goToRegister = () => {
  router.push('/register')
}

onMounted(() => {
  // 如果记住密码，可以在这里从localStorage加载保存的用户名和密码
  // 注意：实际项目中密码不应明文存储
})
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-form-wrapper {
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: 100%;
  max-width: 400px;
}

.login-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  text-align: center;
  margin-bottom: 10px;
}

.login-subtitle {
  font-size: 16px;
  color: #606266;
  text-align: center;
  margin-bottom: 30px;
}

.login-form {
  margin-top: 20px;
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
}

.register-link {
  text-align: center;
  margin-top: 20px;
}

.register-link span {
  color: #606266;
}

@media (max-width: 480px) {
  .login-form-wrapper {
    padding: 20px;
  }
  
  .login-title {
    font-size: 20px;
  }
  
  .login-subtitle {
    font-size: 14px;
  }
}
</style>