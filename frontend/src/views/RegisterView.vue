<template>
  <div class="auth-page register-container">
    <div class="auth-card register-form-wrapper">
      <div class="auth-brand">
        <div class="auth-brand-mark">social</div>
        <h1 class="auth-title">创建账号</h1>
        <p class="auth-subtitle">加入校友网络，开启连接</p>
      </div>
      
      <el-form
        :model="registerForm"
        :rules="registerRules"
        ref="registerFormRef"
        label-width="80px"
        class="register-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            size="large"
          />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请确认密码"
            prefix-icon="Lock"
            show-password
            size="large"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleRegister"
            size="large"
            class="register-button"
          >
            注册
          </el-button>
        </el-form-item>
        
        <el-form-item class="login-link">
          <span>已有账号？</span>
          <el-link type="primary" @click="goToLogin">立即登录</el-link>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../api/auth'

const router = useRouter()
const registerFormRef = ref()
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 注册
const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  try {
    await registerFormRef.value.validate()
    loading.value = true
    
    const response = await authApi.register({
      username: registerForm.username,
      password: registerForm.password,
      confirmPassword: registerForm.confirmPassword
    })
    
    if (response.code === 200) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    }
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    loading.value = false
  }
}

// 跳转到登录页
const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-form-wrapper {
  max-width: 440px;
  padding: 40px 36px 36px;
}

.register-form {
  margin-top: 4px;
}

.register-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.register-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  margin-top: 4px;
  box-shadow: 0 6px 20px rgba(79, 70, 229, 0.3);
}

.login-link {
  text-align: center;
  margin-top: 8px;
}

.login-link span {
  color: var(--ink-muted);
  font-size: 14px;
  font-weight: 500;
}

@media (max-width: 480px) {
  .register-form-wrapper {
    padding: 28px 20px 24px;
  }
}
</style>