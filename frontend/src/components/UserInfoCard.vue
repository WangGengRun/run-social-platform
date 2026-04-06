<template>
  <div class="user-info-card">
    <div class="user-top">
      <div class="avatar-section">
        <ResolvedAvatar :size="80" :src="userInfo.avatar || ''">
          {{ userInfo.realName?.charAt(0) || userInfo.username?.charAt(0) || '用' }}
        </ResolvedAvatar>
        <el-tag :type="isAlumni ? 'success' : 'info'" size="small" class="verified-tag">
          {{ isAlumni ? '校友' : '普通用户' }}
        </el-tag>
      </div>

      <AlumniOnlyBlur :locked="contentLocked" class="user-top-blur">
        <div class="user-name">
          <h2>{{ userInfo.realName || userInfo.username }}</h2>
          <p v-if="userInfo.bio" class="bio">{{ userInfo.bio }}</p>
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
      </AlumniOnlyBlur>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Calendar, School, Reading, OfficeBuilding, Briefcase, Location } from '@element-plus/icons-vue'
import ResolvedAvatar from './ResolvedAvatar.vue'
import AlumniOnlyBlur from './AlumniOnlyBlur.vue'

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
  },
  contentLocked: {
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
const isAlumni = computed(() =>
  props.userInfo?.role === 'ALUMNI' || props.userInfo?.verifyStatus === 1 || props.userInfo?.verifyStatus === 'VERIFIED'
)

const handleFollowClick = async () => {
  if (props.contentLocked) {
    ElMessage.warning('请先完成校友认证，认证后可使用关注等功能')
    return
  }
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

.user-top {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.user-top-blur {
  flex: 1;
  min-width: 0;
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
  .user-top {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .user-details {
    grid-template-columns: 1fr;
  }
}
</style>
