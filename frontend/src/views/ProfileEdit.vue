<template>
  <div class="profile-edit-page">
    <AppShellBar />
    <div class="profile-edit-container alumni-page">
    <el-card class="edit-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>编辑资料</span>
        </div>
      </template>
      
      <div class="edit-form">
        <div class="avatar-section">
          <div class="avatar-preview">
            <el-avatar :size="120" :src="avatarUrl" class="current-avatar">
              <img v-if="avatarUrl" :src="avatarUrl" alt="头像" />
              <span v-else>{{ userStore.userInfo?.realName?.charAt(0) || userStore.username?.charAt(0) || '用' }}</span>
            </el-avatar>
            <div class="avatar-actions">
              <el-button type="primary" size="small" @click="triggerFileInput">
                选择头像
              </el-button>
              <el-button v-if="avatarUrl" size="small" @click="handleDeleteAvatar">
                删除头像
              </el-button>
            </div>
          </div>
          <input
            ref="fileInput"
            type="file"
            accept="image/*"
            class="file-input"
            @change="handleFileChange"
          />
        </div>
        
        <el-form
          :model="form"
          :rules="rules"
          ref="formRef"
          label-width="120px"
          class="profile-form"
        >
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入邮箱" />
          </el-form-item>

          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号" />
          </el-form-item>

          <el-form-item label="公司" prop="company">
            <el-input v-model="form.company" placeholder="请输入公司" />
          </el-form-item>
          
          <el-form-item label="职位" prop="position">
            <el-input v-model="form.position" placeholder="请输入职位" />
          </el-form-item>
          
          <el-form-item label="城市" prop="city">
            <el-input v-model="form.city" placeholder="请输入城市" />
          </el-form-item>
          
          <el-form-item label="个人简介" prop="bio">
            <el-input
              v-model="form.bio"
              type="textarea"
              :rows="3"
              placeholder="请输入个人简介"
            />
          </el-form-item>
          
          <el-form-item class="form-actions">
            <el-button @click="handleCancel" class="cancel-button">取消</el-button>
            <el-button type="primary" @click="handleSubmit" :loading="loading" class="submit-button">保存</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { alumniApi } from '../api/alumni'
import { resolveAvatarUrl, clearAvatarUrlCache } from '../utils/avatarUrl'
import { useUserStore } from '../stores/user'
import AppShellBar from '../components/AppShellBar.vue'

function parseUploadObjectName(data) {
  if (data == null || data === '') return ''
  if (typeof data === 'string') return data
  return data.key || data.objectName || data.path || ''
}

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const fileInput = ref()
const loading = ref(false)
const avatarUrl = ref('')
const avatarObjectName = ref('')

const form = ref({
  email: '',
  phone: '',
  company: '',
  position: '',
  city: '',
  bio: ''
})

const rules = ref({
  email: [
    {
      type: 'email',
      message: '邮箱格式不正确',
      trigger: 'blur'
    }
  ]
})

const initForm = async () => {
  try {
    const response = await alumniApi.getCurrentProfile()
    if (response.code === 200) {
      form.value = {
        email: response.data.email || '',
        phone: response.data.phone || '',
        company: response.data.company || '',
        position: response.data.position || '',
        city: response.data.city || '',
        bio: response.data.bio || ''
      }
      
      // 处理头像
      if (response.data.avatar) {
        avatarObjectName.value = response.data.avatar
        avatarUrl.value = await resolveAvatarUrl(response.data.avatar)
      }
    }
  } catch (error) {
    console.error('获取个人信息失败:', error)
    ElMessage.error('获取个人信息失败，请重试')
  }
}

const triggerFileInput = () => {
  fileInput.value?.click()
}

const handleFileChange = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  const formData = new FormData()
  formData.append('file', file)
  
  try {
    const response = await alumniApi.uploadAvatar(formData)
    if ((response.code === 200 || response.code === 0) && response.data != null && response.data !== '') {
      const objectName = parseUploadObjectName(response.data)
      if (objectName) {
        avatarObjectName.value = objectName
        avatarUrl.value = await resolveAvatarUrl(objectName)
        userStore.patchUserInfo({ avatar: objectName })
        ElMessage.success('头像上传成功')
      } else {
        ElMessage.error('头像上传失败：响应格式错误')
      }
    } else {
      ElMessage.error(response.msg || response.message || '头像上传失败，请重试')
    }
  } catch (error) {
    console.error('上传头像失败:', error)
    ElMessage.error('上传头像失败：' + (error.message || '请重试'))
  }
  
  // 重置文件输入
  event.target.value = ''
}

const handleDeleteAvatar = async () => {
  if (!avatarObjectName.value) return
  
  try {
    const key = avatarObjectName.value
    const response = await alumniApi.deleteAvatar(key)
    if (response.code === 200 || response.code === 0) {
      clearAvatarUrlCache(key)
      avatarUrl.value = ''
      avatarObjectName.value = ''
      userStore.patchUserInfo({ avatar: '' })
      ElMessage.success('头像删除成功')
    } else {
      ElMessage.error(response.msg || response.message || '头像删除失败，请重试')
    }
  } catch (error) {
    console.error('删除头像失败:', error)
    ElMessage.error('删除头像失败，请重试')
  }
}

const handleCancel = () => {
  router.back()
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      
      try {
        const profileData = {
          ...form.value,
          avatar: avatarObjectName.value
        }
        
        const response = await alumniApi.updateProfile(profileData)
        
        if (response.code === 200) {
          userStore.patchUserInfo({
            avatar: avatarObjectName.value || '',
            realName: userStore.userInfo?.realName
          })
          ElMessage.success('保存成功')
          // 跳转到个人主页并刷新
          router.push(`/profile/${userStore.userId}`)
        } else {
          ElMessage.error('保存失败，请重试')
        }
      } catch (error) {
        console.error('更新资料失败:', error)
        ElMessage.error('更新资料失败，请重试')
      } finally {
        loading.value = false
      }
    } else {
      console.log('表单验证失败')
      return false
    }
  })
}

onMounted(() => {
  initForm()
})
</script>

<style scoped>
.profile-edit-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.profile-edit-container {
  flex: 1;
}

.edit-card {
  border-radius: var(--radius-lg) !important;
  border: 1px solid var(--border-subtle) !important;
  box-shadow: var(--shadow-card) !important;
  overflow: hidden;
}

.edit-card :deep(.el-card__header) {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.95) 0%, rgba(248, 249, 252, 0.98) 100%);
  border-bottom: 1px solid var(--border-subtle);
}

.card-header {
  font-size: 17px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--ink);
}

.edit-form {
  padding: 20px 0;
}

.avatar-section {
  text-align: center;
  margin-bottom: 30px;
}

.avatar-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.current-avatar {
  border: 3px solid rgba(255, 255, 255, 0.95);
  box-shadow: 0 8px 28px rgba(79, 70, 229, 0.18);
  transition: box-shadow 0.25s ease;
}

.current-avatar:hover {
  box-shadow: 0 10px 36px rgba(79, 70, 229, 0.28);
}

.avatar-actions {
  display: flex;
  gap: 12px;
}

.file-input {
  display: none;
}

.profile-form {
  width: 100%;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 30px;
  padding-left: 120px;
}

.cancel-button {
  width: 100px;
}

.submit-button {
  width: 120px;
}

@media (max-width: 768px) {
  .profile-edit-container {
    padding: 0 10px;
  }
  
  .edit-form {
    padding: 10px 0;
  }
  
  .profile-form {
    label-width: 80px !important;
  }
  
  .form-actions {
    padding-left: 80px;
    flex-direction: column;
  }
  
  .cancel-button,
  .submit-button {
    width: 100%;
  }
  
  .avatar-actions {
    flex-direction: column;
    align-items: center;
  }
}
</style>
