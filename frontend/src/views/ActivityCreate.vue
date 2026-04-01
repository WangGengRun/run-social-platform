<template>
  <div class="activity-create-container">
    <el-card class="create-card">
      <template #header>
        <div class="card-header">
          <span>创建活动</span>
        </div>
      </template>
      
      <div class="create-form">
        <el-form
          :model="form"
          :rules="rules"
          ref="formRef"
          label-width="120px"
          class="activity-form"
        >
          <el-form-item label="封面图" prop="coverImage">
            <div class="cover-uploader">
              <el-upload
                v-model:file-list="coverFileList"
                :auto-upload="false"
                :on-change="handleCoverChange"
                :limit="1"
                :on-exceed="handleExceed"
                accept=".jpg,.jpeg,.png,.gif"
                list-type="picture-card"
                class="cover-upload"
              >
                <template #default>
                  <div v-if="coverFileList.length === 0">
                    <el-icon class="upload-icon"><Plus /></el-icon>
                    <div class="upload-text">点击上传封面图</div>
                  </div>
                </template>
                <template #file="{
                  file,
                  delete: handleCoverDelete,
                }">
                  <div class="cover-item">
                    <img :src="file.url" alt="" class="cover-image">
                    <div class="cover-actions">
                      <el-icon class="delete-icon" @click="handleCoverDelete(file)"><Delete /></el-icon>
                    </div>
                  </div>
                </template>
              </el-upload>
            </div>
          </el-form-item>
          
          <el-form-item label="标题" prop="title" required>
            <el-input v-model="form.title" placeholder="请输入活动标题" />
          </el-form-item>
          
          <el-form-item label="描述" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="3"
              placeholder="请输入活动描述"
            />
          </el-form-item>
          
          <el-form-item label="地点" prop="location" required>
            <el-input v-model="form.location" placeholder="请输入活动地点" />
          </el-form-item>
          
          <el-form-item label="开始时间" prop="startTime" required>
            <el-date-picker
              v-model="form.startTime"
              type="datetime"
              placeholder="选择开始时间"
              style="width: 100%"
            />
          </el-form-item>
          
          <el-form-item label="结束时间" prop="endTime" required>
            <el-date-picker
              v-model="form.endTime"
              type="datetime"
              placeholder="选择结束时间"
              style="width: 100%"
            />
          </el-form-item>
          
          <el-form-item label="人数上限" prop="maxParticipants">
            <el-input
              v-model="form.maxParticipants"
              type="number"
              placeholder="请输入人数上限（0表示不限）"
            />
            <div class="hint-text">0表示不限人数</div>
          </el-form-item>
          
          <el-form-item class="form-actions">
            <el-button @click="handleCancel" class="cancel-button">取消</el-button>
            <el-button type="primary" @click="handleSubmit" :loading="loading" class="submit-button">创建</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import { activityApi } from '../api/activity'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = ref({
  title: '',
  description: '',
  location: '',
  startTime: '',
  endTime: '',
  maxParticipants: 0,
  coverImage: ''
})

const coverFileList = ref([])

const rules = ref({
  title: [
    {
      required: true,
      message: '请输入活动标题',
      trigger: 'blur'
    }
  ],
  location: [
    {
      required: true,
      message: '请输入活动地点',
      trigger: 'blur'
    }
  ],
  startTime: [
    {
      required: true,
      message: '请选择开始时间',
      trigger: 'blur'
    }
  ],
  endTime: [
    {
      required: true,
      message: '请选择结束时间',
      trigger: 'blur'
    }
  ]
})

const handleCoverChange = (file, fileList) => {
  coverFileList.value = fileList
  // 处理图片上传逻辑
  // 这里需要将文件转换为URL或上传到服务器
  // 暂时使用本地预览
  if (file.raw) {
    const reader = new FileReader()
    reader.readAsDataURL(file.raw)
    reader.onload = (e) => {
      file.url = e.target.result
      form.value.coverImage = e.target.result
    }
  }
}

const handleCoverDelete = (file) => {
  coverFileList.value = []
  form.value.coverImage = ''
}

const handleExceed = () => {
  ElMessage.warning('最多只能上传1张封面图')
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
        const response = await activityApi.createActivity(form.value)
        
        if (response.code === 200) {
          ElMessage.success('创建成功')
          // 跳转到活动详情页（占位）
          router.push('/activity/' + response.data.id || '/')
        } else {
          ElMessage.error('创建失败，请重试')
        }
      } catch (error) {
        console.error('创建活动失败:', error)
        ElMessage.error('创建活动失败，请重试')
      } finally {
        loading.value = false
      }
    } else {
      console.log('表单验证失败')
      return false
    }
  })
}
</script>

<style scoped>
.activity-create-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 0 20px;
}

.create-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.create-form {
  padding: 20px 0;
}

.activity-form {
  width: 100%;
}

.cover-uploader {
  width: 100%;
  max-width: 300px;
}

.cover-upload {
  width: 100%;
}

.upload-icon {
  font-size: 24px;
  color: #909399;
}

.upload-text {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.cover-item {
  position: relative;
  width: 100%;
  height: 100%;
  border-radius: 4px;
  overflow: hidden;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-actions {
  position: absolute;
  top: 8px;
  right: 8px;
  background-color: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.delete-icon {
  color: #fff;
  font-size: 14px;
}

.hint-text {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
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
  .activity-create-container {
    padding: 0 10px;
  }
  
  .create-form {
    padding: 10px 0;
  }
  
  .activity-form {
    label-width: 80px !important;
  }
  
  .cover-uploader {
    max-width: 200px;
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
