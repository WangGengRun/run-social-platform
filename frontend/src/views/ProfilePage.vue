<template>
  <div class="profile-container">
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载中...</span>
    </div>
    
    <div v-else class="profile-content">
      <!-- 调试信息 -->
      <div class="debug-info">
        <h3>调试信息</h3>
        <p>用户登录状态: {{ userStore.isLoggedIn }}</p>
        <p>当前登录用户ID (userStore.userId): {{ userStore.userId }}</p>
        <p>校友信息表ID (alumniId): {{ alumniId }}</p>
        <p>校友用户ID (userInfo.userId): {{ userInfo.userId }}</p>
        <p>isSelf: {{ isSelf }}</p>
      </div>
      
      <UserInfoCard
        :user-info="userInfo"
        :follow-stats="followStats"
        :is-self="isSelf"
        :is-following="isFollowing"
        @follow="handleFollow"
        @unfollow="handleUnfollow"
        @edit="handleEditProfile"
      />
      
      <el-tabs v-model="activeTab" class="profile-tabs">
        <el-tab-pane label="动态" name="posts">
          <div v-if="userPosts.length === 0" class="empty-container">
            <el-empty description="暂无动态" />
          </div>
          <div v-else class="posts-list">
            <div
              v-for="post in userPosts"
              :key="post.id"
              class="post-card"
              @click="handlePostClick(post.id)"
            >
              <div class="post-header">
                <div class="user-info" @click.stop="handleUserClick(userInfo.userId)">
                  <el-avatar :size="40" :src="userInfo.avatar" />
                  <div class="user-details">
                    <div class="username">{{ userInfo.realName || userInfo.username }}</div>
                    <div class="time">{{ formatTime(post.createdAt) }}</div>
                  </div>
                </div>
              </div>
              
              <div class="post-content">
                <div class="text-content">{{ post.content }}</div>
                
                <div v-if="post.imageUrlList && post.imageUrlList.length > 0" class="image-grid">
                  <div
                    v-for="(imageUrl, index) in post.imageUrlList"
                    :key="index"
                    class="image-item"
                    :class="{ 'single': post.imageUrlList.length === 1 }"
                    @click.stop="handleImageClick(imageUrl)"
                  >
                    <el-image
                      :src="imageUrl"
                      fit="cover"
                      :preview-src-list="post.imageUrlList"
                      :initial-index="index"
                      :preview-teleported="true"
                    />
                  </div>
                </div>
              </div>
              
              <div class="post-footer">
                <div class="action-buttons">
                  <div
                    class="action-button"
                    :class="{ 'liked': post.isLiked }"
                    @click.stop="handleLikeClick(post.id)"
                  >
                    <el-icon>
                      <component :is="post.isLiked ? 'StarFilled' : 'Star'" />
                    </el-icon>
                    <span>{{ post.likeCount || 0 }}</span>
                  </div>
                  
                  <div
                    class="action-button"
                    @click.stop="handleCommentClick(post.id)"
                  >
                    <el-icon><ChatDotRound /></el-icon>
                    <span>{{ post.commentCount || 0 }}</span>
                  </div>
                </div>
              </div>
            </div>
            
            <div v-if="postsLoading" class="loading-more">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>加载更多...</span>
            </div>
            
            <div v-if="!hasMorePosts && userPosts.length > 0" class="no-more">
              <span>没有更多了</span>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="档案" name="profile">
          <ProfileInfoTab :user-info="userInfo" />
        </el-tab-pane>
        
        <el-tab-pane label="关注" name="following">
          <div v-if="followingLoading" class="loading-container">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>加载中...</span>
          </div>
          <div v-else-if="followingList.length === 0" class="empty-container">
            <el-empty description="暂无关注" />
          </div>
          <div v-else class="follow-list">
            <div
              v-for="user in followingList"
              :key="user.userId"
              class="follow-item"
              @click="console.log('关注列表点击事件触发:', user.userId); handleUserClick(user.userId)"
            >
              <el-avatar :size="50" :src="user.avatar" @click.stop="console.log('头像点击事件触发:', user.userId); handleUserClick(user.userId)" />
              <div class="user-details">
                <div class="username" @click.stop="console.log('用户名点击事件触发:', user.userId); handleUserClick(user.userId)">{{ user.realName || user.username }}</div>
                <div class="user-meta">
                  <span v-if="user.college">{{ user.college }}</span>
                  <span v-if="user.major"> · {{ user.major }}</span>
                </div>
                <div class="follow-info">
                  <span v-if="user.isMutualFollow" class="mutual-follow">互相关注</span>
                  <span class="follow-time">{{ formatTime(user.followTime) }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="粉丝" name="followers">
          <div v-if="followersLoading" class="loading-container">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>加载中...</span>
          </div>
          <div v-else-if="followersList.length === 0" class="empty-container">
            <el-empty description="暂无粉丝" />
          </div>
          <div v-else class="follow-list">
            <div
              v-for="user in followersList"
              :key="user.userId"
              class="follow-item"
              @click="console.log('粉丝列表点击事件触发:', user.userId); handleUserClick(user.userId)"
            >
              <el-avatar :size="50" :src="user.avatar" @click.stop="console.log('头像点击事件触发:', user.userId); handleUserClick(user.userId)" />
              <div class="user-details">
                <div class="username" @click.stop="console.log('用户名点击事件触发:', user.userId); handleUserClick(user.userId)">{{ user.realName || user.username }}</div>
                <div class="user-meta">
                  <span v-if="user.college">{{ user.college }}</span>
                  <span v-if="user.major"> · {{ user.major }}</span>
                </div>
                <div class="follow-info">
                  <span v-if="user.isMutualFollow" class="mutual-follow">互相关注</span>
                  <span class="follow-time">{{ formatTime(user.followTime) }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, Star, StarFilled, ChatDotRound } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { alumniApi } from '../api/alumni'
import { postApi } from '../api/post'
import UserInfoCard from '../components/UserInfoCard.vue'
import ProfileInfoTab from '../components/ProfileInfoTab.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const alumniId = ref(Number(route.params.id))
const userInfo = ref({})
const followStats = ref({ following: 0, followers: 0 })
const userPosts = ref([])
const loading = ref(true)
const postsLoading = ref(false)
const activeTab = ref('posts')
const isFollowing = ref(false)
const hasMorePosts = ref(true)
const postsPageNum = ref(1)
const postsPageSize = ref(10)

const followingList = ref([])
const followersList = ref([])
const followingLoading = ref(false)
const followersLoading = ref(false)
const followingPageNum = ref(1)
const followersPageNum = ref(1)
const followPageSize = ref(20)

const isSelf = computed(() => {
  const isLoggedIn = userStore.isLoggedIn
  const alumniUserId = userInfo.value.userId
  const currentUserId = userStore.userId
  // 同时比较路由参数和校友信息中的userId，确保正确判断
  const result = isLoggedIn && (String(currentUserId) === String(alumniUserId) || String(currentUserId) === String(alumniId.value))
  console.log('isSelf计算:', {
    isLoggedIn: isLoggedIn,
    currentUserId: currentUserId,
    alumniUserId: alumniUserId,
    alumniId: alumniId.value,
    result: result
  })
  return result
})

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  const month = 30 * day
  const year = 365 * day
  
  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  } else if (diff < month) {
    return `${Math.floor(diff / day)}天前`
  } else if (diff < year) {
    return `${Math.floor(diff / month)}个月前`
  } else {
    return `${Math.floor(diff / year)}年前`
  }
}

const getUserProfile = async () => {
  try {
    // 检查路由参数中的alumniId是否为当前登录用户的ID
    const isCurrentUserProfile = String(alumniId.value) === String(userStore.userId)
    
    if (isCurrentUserProfile) {
      // 如果是当前登录用户，获取当前用户的校友信息
      const currentProfileResponse = await alumniApi.getCurrentProfile()
      console.log('获取当前用户校友信息响应:', currentProfileResponse)
      
      if (currentProfileResponse.code === 200) {
        userInfo.value = currentProfileResponse.data
        console.log('使用当前用户的校友信息')
        console.log('当前用户校友信息:', userInfo.value)
      } else {
        console.log('获取当前用户校友信息失败，响应码:', currentProfileResponse.code)
        // 失败后尝试使用用户ID获取
        await fetchProfileByUserId(alumniId.value)
      }
    } else {
      // 如果是其他用户，使用用户ID获取校友信息
      await fetchProfileByUserId(alumniId.value)
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    // 即使获取用户信息失败，也不影响页面显示，使用动态中的用户信息
    // 不抛出错误，让后续操作继续执行
  }
}

// 根据用户ID获取校友信息
const fetchProfileByUserId = async (userId) => {
  try {
    // 尝试从关注列表或粉丝列表中获取用户信息
    const allUsers = [...followingList.value, ...followersList.value]
    const userFromList = allUsers.find(user => user.userId === userId)
    
    if (userFromList) {
      userInfo.value = {
        userId: userFromList.userId,
        username: userFromList.username,
        realName: userFromList.realName,
        avatar: userFromList.avatar ? userFromList.avatar.replace(/`/g, '') : '',
        admissionYear: userFromList.admissionYear,
        graduationYear: userFromList.graduationYear,
        college: userFromList.college,
        major: userFromList.major,
        company: userFromList.company,
        position: userFromList.position,
        city: userFromList.city,
        bio: '',
        verifyStatus: userFromList.verifyStatus
      }
      console.log('使用从关注/粉丝列表中提取的用户信息')
      console.log('用户信息:', userInfo.value)
      return
    }
    
    // 如果在关注/粉丝列表中找不到，尝试从动态中获取用户信息
    const postsResponse = await postApi.getUserPosts(userId, 1, 1)
    console.log('根据用户ID获取动态响应:', postsResponse)
    
    if (postsResponse.code === 200 && postsResponse.data.list.length > 0) {
      const firstPost = postsResponse.data.list[0]
      // 从动态中提取用户信息
      userInfo.value = {
        userId: firstPost.userId,
        username: firstPost.username,
        realName: firstPost.realName,
        avatar: firstPost.avatar ? firstPost.avatar.replace(/`/g, '') : '',
        admissionYear: firstPost.admissionYear,
        graduationYear: firstPost.graduationYear,
        college: firstPost.college,
        major: firstPost.major,
        company: firstPost.company,
        position: firstPost.position,
        city: firstPost.city,
        bio: '',
        verifyStatus: firstPost.verifyStatus
      }
      console.log('使用从动态中提取的用户信息')
      console.log('用户信息:', userInfo.value)
    } else {
      console.log('根据用户ID获取动态失败，无法提取用户信息')
      // 设置一个默认的用户信息，避免页面空白
      userInfo.value = {
        userId: userId,
        username: '',
        realName: '未知用户',
        avatar: '',
        admissionYear: '',
        graduationYear: '',
        college: '',
        major: '',
        company: '',
        position: '',
        city: '',
        bio: '',
        verifyStatus: 0
      }
      console.log('使用默认用户信息')
      console.log('用户信息:', userInfo.value)
    }
  } catch (error) {
    console.error('根据用户ID获取校友信息失败:', error)
    // 设置一个默认的用户信息，避免页面空白
    userInfo.value = {
      userId: userId,
      username: '',
      realName: '未知用户',
      avatar: '',
      admissionYear: '',
      graduationYear: '',
      college: '',
      major: '',
      company: '',
      position: '',
      city: '',
      bio: '',
      verifyStatus: 0
    }
    console.log('使用默认用户信息')
    console.log('用户信息:', userInfo.value)
  }
}

const getFollowStats = async () => {
  try {
    // 使用路由参数中的alumniId作为用户ID，确保获取的是正确用户的关注统计
    const currentUserId = alumniId.value
    console.log('获取关注统计的用户ID:', currentUserId)
    const response = await alumniApi.getFollowStats(currentUserId)
    if (response.code === 200) {
      followStats.value = response.data
    }
  } catch (error) {
    console.error('获取关注统计失败:', error)
    // 即使获取关注统计失败，也不影响页面显示
  }
}

const getFollowingList = async () => {
  try {
    followingLoading.value = true
    const response = await alumniApi.getUserFollowing(alumniId.value, followingPageNum.value, followPageSize.value)
    console.log('获取指定用户关注列表响应:', response)
    if (response.code === 200) {
      followingList.value = response.data.list || []
      console.log('关注列表数据:', followingList.value)
      // 检查每个用户对象是否有userId字段
      followingList.value.forEach((user, index) => {
        console.log(`关注列表用户${index}:`, user)
        console.log(`用户${index}的userId:`, user.userId)
      })
    }
  } catch (error) {
    console.error('获取关注列表失败:', error)
  } finally {
    followingLoading.value = false
  }
}

const getFollowersList = async () => {
  try {
    followersLoading.value = true
    const response = await alumniApi.getUserFollowers(alumniId.value, followersPageNum.value, followPageSize.value)
    console.log('获取指定用户粉丝列表响应:', response)
    if (response.code === 200) {
      followersList.value = response.data.list || []
      console.log('粉丝列表数据:', followersList.value)
      // 检查每个用户对象是否有userId字段
      followersList.value.forEach((user, index) => {
        console.log(`粉丝列表用户${index}:`, user)
        console.log(`用户${index}的userId:`, user.userId)
      })
    }
  } catch (error) {
    console.error('获取粉丝列表失败:', error)
  } finally {
    followersLoading.value = false
  }
}

const getUserPosts = async (refresh = false) => {
  if (postsLoading.value) return
  
  if (refresh) {
    postsPageNum.value = 1
    userPosts.value = []
    hasMorePosts.value = true
  }
  
  if (!hasMorePosts.value) return
  
  postsLoading.value = true
  
  try {
    // 使用路由参数中的alumniId作为用户ID，确保获取的是正确用户的动态
    const currentUserId = alumniId.value
    console.log('获取动态的用户ID:', currentUserId)
    const response = await postApi.getUserPosts(
      currentUserId,
      postsPageNum.value,
      postsPageSize.value
    )
    
    if (response.code === 200) {
      const { list, total, pageNum, pageSize } = response.data
      
      // 设置动态数
      userInfo.value.postCount = total
      console.log('设置动态数:', total)
      
      if (refresh) {
        userPosts.value = list
        // 如果用户信息为空，使用动态中的第一个用户信息
        if (list.length > 0 && Object.keys(userInfo.value).length === 0) {
          const firstPost = list[0]
          userInfo.value = {
            userId: firstPost.userId,
            username: firstPost.username,
            realName: firstPost.realName,
            avatar: firstPost.avatar ? firstPost.avatar.replace(/`/g, '') : '',
            bio: '',
            postCount: total
          }
          // 设置关注统计
          followStats.value = {
            following: 0,
            followers: 0
          }
        }
      } else {
        userPosts.value = [...userPosts.value, ...list]
      }
      
      postsPageNum.value = pageNum + 1
      hasMorePosts.value = list.length === pageSize
    }
  } catch (error) {
    console.error('获取用户动态失败:', error)
    ElMessage.error('获取用户动态失败，请重试')
  } finally {
    postsLoading.value = false
  }
}

const handleFollow = () => {
  isFollowing.value = true
}

const handleUnfollow = () => {
  isFollowing.value = false
}

const handleEditProfile = () => {
  router.push('/profile/edit')
}

const handleLikeClick = async (postId) => {
  try {
    const post = userPosts.value.find(p => p.id === postId)
    if (!post) return
    
    if (post.isLiked) {
      post.isLiked = false
      post.likeCount--
    } else {
      post.isLiked = true
      post.likeCount++
    }
  } catch (error) {
    ElMessage.error('操作失败，请重试')
  }
}

const handleCommentClick = (postId) => {
  router.push(`/post/${postId}`)
}

const handlePostClick = (postId) => {
  router.push(`/post/${postId}`)
}

const handleUserClick = (userId) => {
  console.log('跳转到个人主页:', userId)
  if (userId) {
    console.log('userId存在，执行跳转:', `/profile/${userId}`)
    router.push(`/profile/${userId}`)
  } else {
    console.error('userId不存在，无法跳转')
    ElMessage.error('用户ID不存在，无法跳转')
  }
}

const handleImageClick = (imageUrl) => {
  console.log('查看图片:', imageUrl)
}

onMounted(async () => {
  if (!alumniId.value) {
    ElMessage.error('校友ID不存在')
    router.push('/feed')
    return
  }
  
  try {
    // 先获取用户信息，再获取其他数据
    await getUserProfile()
    await Promise.all([
      getFollowStats(),
      getUserPosts(true)
    ])
  } catch (error) {
    console.error('初始化失败:', error)
    // 如果获取校友信息失败，尝试使用用户ID获取动态
    try {
      await getUserPosts(true)
    } catch (postsError) {
      console.error('获取动态失败:', postsError)
    } finally {
      loading.value = false
    }
  } finally {
    loading.value = false
  }
})

// 监听路由参数变化
watch(
  () => route.params.id,
  (newId, oldId) => {
    console.log('路由参数变化:', { newId, oldId })
    if (newId !== oldId) {
      alumniId.value = Number(newId)
      // 重新加载数据
      loading.value = true
      Promise.all([
        getUserProfile(),
        getFollowStats(),
        getFollowingList(),
        getFollowersList(),
        getUserPosts(true)
      ]).finally(() => {
        loading.value = false
      })
    }
  }
)

watch(activeTab, (newTab) => {
  if (newTab === 'following' && followingList.value.length === 0) {
    getFollowingList()
  } else if (newTab === 'followers' && followersList.value.length === 0) {
    getFollowersList()
  }
})
</script>

<style scoped>
.profile-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.debug-info {
  background-color: #f0f9ff;
  border: 1px solid #d1ecf1;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
}

.debug-info h3 {
  margin-top: 0;
  color: #17a2b8;
  font-size: 16px;
}

.debug-info p {
  margin: 8px 0;
  font-size: 14px;
  color: #495057;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  color: #909399;
}

.loading-container .el-icon {
  font-size: 32px;
  margin-bottom: 10px;
}

.profile-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-tabs {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.profile-tabs :deep(.el-tabs__header) {
  margin: 0;
  padding: 0 20px;
  border-bottom: 1px solid #e4e7ed;
}

.profile-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.profile-tabs :deep(.el-tabs__item) {
  height: 50px;
  line-height: 50px;
  font-size: 16px;
  color: #606266;
}

.profile-tabs :deep(.el-tabs__item.is-active) {
  color: #409eff;
  font-weight: bold;
}

.profile-tabs :deep(.el-tabs__active-bar) {
  background-color: #409eff;
}

.profile-tabs :deep(.el-tabs__content) {
  padding: 20px;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: box-shadow 0.3s;
}

.post-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.post-header {
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.username {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.time {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.post-content {
  margin-bottom: 12px;
}

.text-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 12px;
  white-space: pre-wrap;
  word-break: break-word;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 4px;
}

.image-grid.single {
  grid-template-columns: 1fr;
}

.image-item {
  aspect-ratio: 1;
  overflow: hidden;
  border-radius: 4px;
  cursor: pointer;
}

.image-item.single {
  max-width: 400px;
  aspect-ratio: auto;
}

.image-item :deep(.el-image) {
  width: 100%;
  height: 100%;
  display: block;
}

.post-footer {
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
}

.action-buttons {
  display: flex;
  gap: 24px;
}

.action-button {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #909399;
  font-size: 14px;
  cursor: pointer;
  transition: color 0.3s;
}

.action-button:hover {
  color: #409eff;
}

.action-button.liked {
  color: #f56c6c;
}

.action-button .el-icon {
  font-size: 18px;
}

.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  color: #909399;
}

.follow-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.follow-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background-color: #fff;
  border-radius: 8px;
  cursor: pointer;
  transition: box-shadow 0.3s;
}

.follow-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.follow-item .user-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.follow-item .username {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.follow-item .user-meta {
  font-size: 14px;
  color: #909399;
}

.follow-item .follow-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #909399;
}

.follow-item .mutual-follow {
  color: #409eff;
  background-color: #ecf5ff;
  padding: 2px 8px;
  border-radius: 4px;
}

.follow-item .follow-time {
  color: #c0c4cc;
}

.loading-more,
.no-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px;
  color: #909399;
  font-size: 14px;
}

.loading-more .el-icon {
  font-size: 20px;
}

@media (max-width: 768px) {
  .profile-container {
    padding: 10px;
  }
  
  .profile-tabs :deep(.el-tabs__content) {
    padding: 12px;
  }
}
</style>
