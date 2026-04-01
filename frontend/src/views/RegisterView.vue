<template>
  <div class="register-container">
    <div class="register-form-wrapper">
      <h1 class="register-title">校友社交平台</h1>
      <h2 class="register-subtitle">用户注册</h2>
      
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
        
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入手机号"
            prefix-icon="Mobile"
            size="large"
          >
            <template #append>
              <el-button
                type="primary"
                :disabled="countdown > 0"
                @click="sendCaptcha"
                size="small"
              >
                {{ countdown > 0 ? `${countdown}s后重新获取` : '获取验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="验证码" prop="captcha">
          <el-input
            v-model="registerForm.captcha"
            placeholder="请输入验证码"
            prefix-icon="ChatLineRound"
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
const countdown = ref(0)
let countdownTimer = null

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  captcha: '',
  captchaKey: ''
})

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
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

// 发送验证码
const sendCaptcha = async () => {
  if (!registerForm.phone) {
    ElMessage.warning('请输入手机号')
    return
  }
  
  try {
    const response = await authApi.sendCaptcha(registerForm.phone)
    if (response.code === 200) {
      registerForm.captchaKey = response.data.captchaKey
      ElMessage.success('验证码发送成功')
      startCountdown()
    }
  } catch (error) {
    console.error('发送验证码失败:', error)
  }
}

// 开始倒计时
const startCountdown = () => {
  countdown.value = 60
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
  countdownTimer = setInterval(() => {
    if (countdown.value > 0) {
      countdown.value--
    } else {
      clearInterval(countdownTimer)
    }
  }, 1000)
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
      confirmPassword: registerForm.confirmPassword,
      phone: registerForm.phone,
      captcha: registerForm.captcha,
      captchaKey: registerForm.captchaKey
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
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-form-wrapper {
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: 100%;
  max-width: 400px;
}

.register-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  text-align: center;
  margin-bottom: 10px;
}

.register-subtitle {
  font-size: 16px;
  color: #606266;
  text-align: center;
  margin-bottom: 30px;
}

.register-form {
  margin-top: 20px;
}

.register-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
}

.login-link {
  text-align: center;
  margin-top: 20px;
}

.login-link span {
  color: #606266;
}

@media (max-width: 480px) {
  .register-form-wrapper {
    padding: 20px;
  }
  
  .register-title {
    font-size: 20px;
  }
  
  .register-subtitle {
    font-size: 14px;
  }
}
</style>