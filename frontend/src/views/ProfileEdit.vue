<template>
  <div class="profile-edit-container">
    <el-card class="edit-card">
      <template #header>
        <div class="card-header">
          <span>编辑资料</span>
        </div>
      </template>
      
      <div class="edit-form">
        <el-form
          :model="form"
          :rules="rules"
          ref="formRef"
          label-width="120px"
          class="profile-form"
        >
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="form.realName" placeholder="请输入真实姓名" />
          </el-form-item>
          
          <el-form-item label="学号" prop="studentId">
            <el-input v-model="form.studentId" placeholder="请输入学号" />
          </el-form-item>
          
          <el-form-item label="入学年份" prop="admissionYear" required>
            <el-input v-model="form.admissionYear" type="number" placeholder="请输入入学年份" />
          </el-form-item>
          
          <el-form-item label="毕业年份" prop="graduationYear">
            <el-input v-model="form.graduationYear" type="number" placeholder="请输入毕业年份" />
          </el-form-item>
          
          <el-form-item label="学院" prop="college">
            <el-input v-model="form.college" placeholder="请输入学院" />
          </el-form-item>
          
          <el-form-item label="专业" prop="major">
            <el-input v-model="form.major" placeholder="请输入专业" />
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
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { alumniApi } from '../api/alumni'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = ref({
  realName: '',
  studentId: '',
  admissionYear: '',
  graduationYear: '',
  college: '',
  major: '',
  company: '',
  position: '',
  city: '',
  bio: ''
})

const rules = ref({
  admissionYear: [
    {
      required: true,
      message: '请输入入学年份',
      trigger: 'blur'
    },
    {
      pattern: /^\d+$/,
      message: '入学年份必须是数字',
      trigger: 'blur'
    }
  ]
})

const initForm = async () => {
  try {
    const response = await alumniApi.getCurrentProfile()
    if (response.code === 200) {
      form.value = {
        realName: response.data.realName || '',
        studentId: response.data.studentId || '',
        admissionYear: response.data.admissionYear || response.data.enrollYear || '',
        graduationYear: response.data.graduationYear || '',
        college: response.data.college || '',
        major: response.data.major || '',
        company: response.data.company || '',
        position: response.data.position || '',
        city: response.data.city || '',
        bio: response.data.bio || ''
      }
    }
  } catch (error) {
    console.error('获取个人信息失败:', error)
    ElMessage.error('获取个人信息失败，请重试')
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
        const response = await alumniApi.updateProfile(form.value)
        
        if (response.code === 200) {
          ElMessage.success('保存成功')
          // 跳转到个人主页并刷新
          router.push('/profile/' + form.value.userId || '')
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
.profile-edit-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 0 20px;
}

.edit-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.edit-form {
  padding: 20px 0;
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
}
</style>
