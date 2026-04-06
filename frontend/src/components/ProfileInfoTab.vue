<template>
  <div class="profile-info-tab">
    <el-card class="info-card">
      <template #header>
        <div class="card-header">
          <span>校友档案</span>
        </div>
      </template>
      
      <div class="info-section">
        <div class="section-title">基本信息</div>
        <div class="info-grid">
          <div v-if="userInfo.realName" class="info-item">
            <span class="info-label">真实姓名</span>
            <span class="info-value">{{ userInfo.realName }}</span>
          </div>
          <div v-if="userInfo.username" class="info-item">
            <span class="info-label">用户名</span>
            <span class="info-value">{{ userInfo.username }}</span>
          </div>
          <div v-if="userInfo.enrollYear" class="info-item">
            <span class="info-label">入学年份</span>
            <span class="info-value">{{ userInfo.enrollYear }}</span>
          </div>
          <div v-if="userInfo.graduationYear" class="info-item">
            <span class="info-label">毕业年份</span>
            <span class="info-value">{{ userInfo.graduationYear }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">身份标签</span>
            <el-tag :type="isAlumni ? 'success' : 'info'" size="small">
              {{ isAlumni ? '校友' : '普通用户' }}
            </el-tag>
          </div>
        </div>
      </div>
      
      <div class="info-section">
        <div class="section-title">教育信息</div>
        <div class="info-grid">
          <div v-if="userInfo.college" class="info-item">
            <span class="info-label">学院</span>
            <span class="info-value">{{ userInfo.college }}</span>
          </div>
          <div v-if="userInfo.major" class="info-item">
            <span class="info-label">专业</span>
            <span class="info-value">{{ userInfo.major }}</span>
          </div>
        </div>
      </div>
      
      <div class="info-section">
        <div class="section-title">工作信息</div>
        <div class="info-grid">
          <div v-if="userInfo.company" class="info-item">
            <span class="info-label">公司</span>
            <span class="info-value">{{ userInfo.company }}</span>
          </div>
          <div v-if="userInfo.position" class="info-item">
            <span class="info-label">职位</span>
            <span class="info-value">{{ userInfo.position }}</span>
          </div>
          <div v-if="userInfo.city" class="info-item">
            <span class="info-label">城市</span>
            <span class="info-value">{{ userInfo.city }}</span>
          </div>
        </div>
      </div>
      
      <div v-if="userInfo.bio" class="info-section">
        <div class="section-title">个人简介</div>
        <div class="bio-content">
          {{ userInfo.bio }}
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  userInfo: {
    type: Object,
    required: true
  }
})

const isAlumni = computed(() =>
  props.userInfo?.role === 'ALUMNI' || props.userInfo?.verifyStatus === 1 || props.userInfo?.verifyStatus === 'VERIFIED'
)
</script>

<style scoped>
.profile-info-tab {
  padding: 20px 0;
}

.info-card {
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: bold;
  color: #303133;
}

.info-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 2px solid #409eff;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 12px;
  color: #909399;
}

.info-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.bio-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  white-space: pre-wrap;
  word-break: break-word;
}

@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
