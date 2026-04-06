<template>
  <div class="post-publish-page">
    <AppShellBar />
    <div class="post-publish-container alumni-page">
    <el-card class="publish-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>发布动态</span>
        </div>
      </template>
      
      <div class="publish-form">
        <div class="form-item">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="5"
            placeholder="分享你的动态..."
            class="content-input"
            ref="contentInput"
          />
        </div>
        
        <div class="form-item">
          <div class="upload-section">
            <div class="upload-label">
              <el-icon><Picture /></el-icon>
              <span>添加图片</span>
            </div>
            <div class="image-uploader">
              <el-upload
                v-model:file-list="imageFileList"
                :auto-upload="false"
                :on-change="handleImageChange"
                :limit="9"
                :on-exceed="handleExceed"
                accept=".jpg,.jpeg,.png,.gif"
                list-type="picture-card"
                class="image-upload"
              >
                <template #default>
                  <div v-if="imageFileList.length < 9">
                    <el-icon class="upload-icon"><Plus /></el-icon>
                    <div class="upload-text">点击上传</div>
                  </div>
                </template>
                <template #file="{
                  file,
                  delete: handleDelete,
                }">
                  <div class="image-item">
                    <img :src="file.url" alt="" class="preview-image">
                    <div class="image-actions">
                      <el-icon class="delete-icon" @click="handleDelete(file)"><Delete /></el-icon>
                    </div>
                  </div>
                </template>
              </el-upload>
            </div>
          </div>
        </div>
        
        <div class="form-item">
          <div class="visibility-section">
            <div class="visibility-label">
              <el-icon><View /></el-icon>
              <span>可见性</span>
            </div>
            <div class="visibility-options">
              <el-radio-group v-if="!isUserRole" v-model="form.visibility" class="visibility-radio-group">
                <el-radio-button label="0">公开</el-radio-button>
                <el-radio-button label="1">仅校友</el-radio-button>
                <el-radio-button label="2">仅自己</el-radio-button>
              </el-radio-group>
              <el-tag v-else type="info">普通用户仅支持公开发布</el-tag>
            </div>
          </div>
        </div>
        
        <div class="form-actions">
          <el-button @click="handleCancel" class="cancel-button">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="loading" class="submit-button">发布</el-button>
        </div>
      </div>
    </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture, View, Plus, Delete } from '@element-plus/icons-vue'
import { postApi } from '../api/post'
import { useUserStore } from '../stores/user'
import AppShellBar from '../components/AppShellBar.vue'

const router = useRouter()
const userStore = useUserStore()
const contentInput = ref()
const loading = ref(false)
const isUserRole = computed(() => userStore.role === 'USER')

const form = ref({
  content: '',
  visibility: 0
})

const imageFileList = ref([])

const handleImageChange = (file, fileList) => {
  imageFileList.value = fileList
  // 处理图片上传逻辑
  // 这里需要将文件转换为URL或上传到服务器
  // 暂时使用本地预览
  if (file.raw) {
    const reader = new FileReader()
    reader.readAsDataURL(file.raw)
    reader.onload = (e) => {
      file.url = e.target.result
    }
  }
}

const handleDelete = (file) => {
  const index = imageFileList.value.findIndex(item => item.uid === file.uid)
  if (index !== -1) {
    imageFileList.value.splice(index, 1)
  }
}

const handleExceed = () => {
  ElMessage.warning('最多只能上传9张图片')
}

const handleCancel = () => {
  router.back()
}

const handleSubmit = async () => {
  if (!form.value.content.trim() && imageFileList.value.length === 0) {
    ElMessage.warning('请输入内容或添加图片')
    return
  }
  
  loading.value = true
  
  try {
    const imageUrlList = []
    for (const file of imageFileList.value) {
      if (!file.raw) continue
      const formData = new FormData()
      formData.append('file', file.raw)
      const uploadRes = await postApi.uploadImage(formData)
      if (uploadRes.code !== 200 || !uploadRes.data) {
        throw new Error(uploadRes.message || '图片上传失败')
      }
      imageUrlList.push(uploadRes.data)
    }
    
    const response = await postApi.publishPost(
      form.value.content,
      imageUrlList,
      isUserRole.value ? 0 : form.value.visibility
    )
    
    if (response.code === 200) {
      ElMessage.success('发布成功')
      router.push('/feed')
    } else {
      ElMessage.error('发布失败，请重试')
    }
  } catch (error) {
    console.error('发布动态失败:', error)
    ElMessage.error('发布失败，请重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (isUserRole.value) {
    form.value.visibility = 0
  }
  // 自动获取焦点
  if (contentInput.value) {
    contentInput.value.focus()
  }
})
</script>

<style scoped>
.post-publish-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.post-publish-container {
  flex: 1;
}

.publish-card {
  border-radius: var(--radius-lg) !important;
  border: 1px solid var(--border-subtle) !important;
  box-shadow: var(--shadow-card) !important;
  overflow: hidden;
}

.publish-card :deep(.el-card__header) {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.95) 0%, rgba(248, 249, 252, 0.98) 100%);
  border-bottom: 1px solid var(--border-subtle);
}

.card-header {
  font-size: 17px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--ink);
}

.publish-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-item {
  width: 100%;
}

.content-input {
  font-size: 14px;
  line-height: 1.5;
}

.upload-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.upload-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
}

.image-uploader {
  width: 100%;
}

.image-upload {
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

.image-item {
  position: relative;
  width: 100%;
  height: 100%;
  border-radius: 4px;
  overflow: hidden;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-actions {
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

.visibility-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.visibility-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
}

.visibility-options {
  width: 100%;
}

.visibility-radio-group {
  width: 100%;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}

.cancel-button {
  width: 100px;
}

.submit-button {
  width: 120px;
}

@media (max-width: 768px) {
  .post-publish-container {
    padding: 0 10px;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .cancel-button,
  .submit-button {
    width: 100%;
  }
}
</style>
