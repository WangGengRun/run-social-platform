<template>
  <div class="user-info-card">
    <div class="user-header">
      <div class="avatar-section">
        <el-avatar :size="80" :src="userInfo.avatar" />
        <el-tag v-if="userInfo.verifyStatus === 'VERIFIED'" type="success" size="small" class="verified-tag">
          已认证
        </el-tag>
      </div>
      <div class="user-name">
        <h2>{{ userInfo.realName || userInfo.username }}</h2>
        <p v-if="userInfo.bio" class="bio">{{ userInfo.bio }}</p>
      </div>
    </div>
    
    <div class="user-stats">
      <div class="stat-item">
        <span class="stat-value">{{ userInfo.postCount || 0 }}</span>
        <span class="stat-label">动态</span>
      </div>
      <div class="stat-item">
        <span class="stat-value">{{ followStats.followingCount || 0 }}</span>
        <span class="stat-label">关注</span>
      </div>
      <div class="stat-item">
        <span class="stat-value">{{ followStats.followerCount || 0 }}</span>
        <span class="stat-label">粉丝</span>
      </div>
    </div>
    
    <div class="user-details">
      <div v-if="userInfo.enrollYear" class="detail-item">
        <el-icon><Calendar /></el-icon>
        <span>{{ userInfo.enrollYear }}入学</span>
      </div>
      <div v-if="userInfo.graduationYear" class="detail-item">
        <el-icon><Calendar /></el-icon>
        <span>{{ userInfo.graduationYear }}毕业</span>
      </div>
      <div v-if="userInfo.college" class="detail-item">
        <el-icon><School /></el-icon>
        <span>{{ userInfo.college }}</span>
      </div>
      <div v-if="userInfo.major" class="detail-item">
        <el-icon><Reading /></el-icon>
        <span>{{ userInfo.major }}</span>
      </div>
      <div v-if="userInfo.company" class="detail-item">
        <el-icon><OfficeBuilding /></el-icon>
        <span>{{ userInfo.company }}</span>
      </div>
      <div v-if="userInfo.position" class="detail-item">
        <el-icon><Briefcase /></el-icon>
        <span>{{ userInfo.position }}</span>
      </div>
      <div v-if="userInfo.city" class="detail-item">
        <el-icon><Location /></el-icon>
        <span>{{ userInfo.city }}</span>
      </div>
    </div>
    
    <div class="user-actions">
      <el-button
        v-if="!isSelf"
        :type="isFollowing ? 'default' : 'primary'"
        @click="handleFollowClick"
        :loading="followLoading"
      >
        {{ isFollowing ? '取消关注' : '关注' }}
      </el-button>
      <el-button
        v-if="!isSelf"
        type="primary"
        @click="handleMessageClick"
      >
        发私信
      </el-button>
      <el-button
        v-if="isSelf"
        type="primary"
        @click="handleEditClick"
      >
        编辑资料
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Calendar, School, Reading, OfficeBuilding, Briefcase, Location } from '@element-plus/icons-vue'

const router = useRouter()

import { alumniApi } from '../api/alumni'

const props = defineProps({
  userInfo: {
    type: Object,
    required: true
  },
  followStats: {
    type: Object,
    default: () => ({ following: 0, followers: 0 })
  },
  isSelf: {
    type: Boolean,
    default: false
  },
  isFollowing: {
    type: Boolean,
    default: false
  }
})

console.log('UserInfoCard props:', {
  isSelf: props.isSelf,
  userInfo: props.userInfo
})

const emit = defineEmits(['follow', 'unfollow', 'edit'])

const followLoading = ref(false)

const handleFollowClick = async () => {
  followLoading.value = true
  
  try {
    if (props.isFollowing) {
      await alumniApi.unfollowUser(props.userInfo.userId)
      ElMessage.success('已取消关注')
      emit('unfollow')
    } else {
      await alumniApi.followUser(props.userInfo.userId)
      ElMessage.success('关注成功')
      emit('follow')
    }
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败，请重试')
  } finally {
    followLoading.value = false
  }
}

const handleEditClick = () => {
  emit('edit')
}

const handleMessageClick = () => {
  router.push(`/message/detail?userId=${props.userInfo.userId}`)
}
</script>

<style scoped>
.user-info-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
}

.user-header {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.avatar-section {
  position: relative;
}

.verified-tag {
  position: absolute;
  bottom: 0;
  right: 0;
}

.user-name {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.user-name h2 {
  margin: 0 0 8px 0;
  font-size: 20px;
  color: #303133;
}

.bio {
  margin: 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
}

.user-stats {
  display: flex;
  justify-content: space-around;
  padding: 16px 0;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-value {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.user-details {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
}

.detail-item .el-icon {
  color: #909399;
  font-size: 16px;
}

.user-actions {
  display: flex;
  justify-content: center;
}

.user-actions .el-button {
  width: 200px;
}

@media (max-width: 768px) {
  .user-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .user-details {
    grid-template-columns: 1fr;
  }
}
</style>
