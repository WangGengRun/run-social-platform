<template>
  <div class="profile-container">
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载中...</span>
    </div>
    
    <div v-else class="profile-content">

      <UserInfoCard
        :user-info="userInfo"
        :follow-stats="followStats"
        :is-self="isSelf"
        :is-following="isFollowing"
        :content-locked="otherProfileLocked"
        @follow="handleFollow"
        @unfollow="handleUnfollow"
        @edit="handleEditProfile"
      />

      <el-card v-if="isSelf && userStore.role !== 'ADMIN'" class="verify-card">
        <template #header>
          <div class="verify-header">
            <span>校友认证</span>
            <el-tag :type="getVerifyTagType(verifyStatusInfo.verifyStatus)">
              {{ getVerifyText(verifyStatusInfo.verifyStatus) }}
            </el-tag>
          </div>
        </template>
        <div class="verify-content">
          <p v-if="verifyStatusInfo.verifyStatus === -1">你还未提交校友认证信息，提交后由管理员审核。</p>
          <p v-else-if="verifyStatusInfo.verifyStatus === 0">认证申请审核中，请耐心等待。</p>
          <p v-else-if="verifyStatusInfo.verifyStatus === 1">认证已通过，你当前为校友身份。</p>
          <p v-else>认证未通过：{{ verifyStatusInfo.verifyNotes || '请完善信息后重新提交' }}</p>
          <el-button
            v-if="showVerifyActionButton"
            type="primary"
            :disabled="verifyStatusInfo.verifyStatus === 0"
            @click="verifyDialogVisible = true"
          >
            {{ verifyActionText }}
          </el-button>
        </div>
      </el-card>
      
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
              :class="{ locked: isLockedPost(post) }"
              @click="handlePostClick(post)"
            >
              <div class="post-header">
                <div class="user-info" @click.stop="handleUserClick(userInfo.userId)">
                  <ResolvedAvatar :size="40" :src="userInfo.avatar || ''">
                    {{ userInfo.username?.charAt(0) || userInfo.realName?.charAt(0) || '用' }}
                  </ResolvedAvatar>
                  <div class="user-details">
                    <div class="username-row">
                      <div class="username">{{ userInfo.username || userInfo.realName }}</div>
                      <el-tag v-if="post.userRole === 'ALUMNI'" size="small" type="warning" effect="plain">校友</el-tag>
                      <el-tag v-else size="small" effect="plain">普通用户</el-tag>
                    </div>
                    <div class="time">{{ formatTime(post.createdAt) }}</div>
                  </div>
                </div>
              </div>
              
              <div class="post-content">
                <div v-if="isLockedPost(post)" class="lock-banner">🔒 校友可见内容，完成校友认证后可查看</div>
                <div class="text-content">{{ post.content }}</div>
                <div
                  v-if="!isLockedPost(post) && shouldShowMore(post)"
                  class="read-more"
                  @click.stop="handlePostClick(post)"
                >
                  查看更多
                </div>
                
                <div v-if="post.imageUrlList && post.imageUrlList.length > 0" class="image-grid">
                  <div
                    v-for="(imageUrl, index) in post.imageUrlList"
                    :key="index"
                    class="image-item"
                    :class="{ 'single': post.imageUrlList.length === 1 }"
                    @click.stop="handleImageClick(imageUrl, post)"
                  >
                    <el-image
                      :src="imageUrl"
                      fit="cover"
                      :preview-src-list="isLockedPost(post) ? [] : post.imageUrlList"
                      :initial-index="index"
                      :preview-teleported="true"
                    />
                  </div>
                </div>
              </div>
              
              <div class="post-footer">
                <div class="action-row">
                  <div class="action-buttons">
                    <div
                      class="action-button"
                      :class="{ 'liked': post.isLiked }"
                      @click.stop="handleLikeClick(post)"
                    >
                      <el-icon>
                        <component :is="post.isLiked ? 'StarFilled' : 'Star'" />
                      </el-icon>
                      <span>{{ post.likeCount || 0 }}</span>
                    </div>
                    
                    <div
                      class="action-button"
                      @click.stop="handleCommentClick(post)"
                    >
                      <el-icon><ChatDotRound /></el-icon>
                      <span>{{ post.commentCount || 0 }}</span>
                    </div>
                  </div>
                  <el-dropdown v-if="isSelfPost(post)" @command="(cmd) => handlePostCommand(cmd, post)">
                    <span class="post-more-trigger" @click.stop>
                      <el-icon><MoreFilled /></el-icon>
                    </span>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="edit">编辑动态</el-dropdown-item>
                        <el-dropdown-item command="delete" divided>删除动态</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
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
          <AlumniOnlyBlur :locked="otherProfileLocked">
            <ProfileInfoTab :user-info="userInfo" />
          </AlumniOnlyBlur>
        </el-tab-pane>
        
        <el-tab-pane label="关注" name="following">
          <div v-if="followingLoading" class="loading-container">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>加载中...</span>
          </div>
          <div v-else-if="followingList.length === 0" class="empty-container">
            <el-empty description="暂无关注" />
          </div>
          <AlumniOnlyBlur v-else :locked="otherProfileLocked" class="follow-blur-wrap">
            <div class="follow-list">
              <div
                v-for="user in followingList"
                :key="user.userId"
                class="follow-item"
                @click="handleUserClick(user.userId)"
              >
                <ResolvedAvatar :size="50" :src="user.avatar || ''" @click.stop="handleUserClick(user.userId)">
                  {{ user.username?.charAt(0) || user.realName?.charAt(0) || '用' }}
                </ResolvedAvatar>
                <div class="user-details">
                  <div class="username" @click.stop="handleUserClick(user.userId)">{{ user.username || user.realName }}</div>
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
          </AlumniOnlyBlur>
        </el-tab-pane>
        
        <el-tab-pane label="粉丝" name="followers">
          <div v-if="followersLoading" class="loading-container">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>加载中...</span>
          </div>
          <div v-else-if="followersList.length === 0" class="empty-container">
            <el-empty description="暂无粉丝" />
          </div>
          <AlumniOnlyBlur v-else :locked="otherProfileLocked" class="follow-blur-wrap">
            <div class="follow-list">
              <div
                v-for="user in followersList"
                :key="user.userId"
                class="follow-item"
                @click="handleUserClick(user.userId)"
              >
                <ResolvedAvatar :size="50" :src="user.avatar || ''" @click.stop="handleUserClick(user.userId)">
                  {{ user.username?.charAt(0) || user.realName?.charAt(0) || '用' }}
                </ResolvedAvatar>
                <div class="user-details">
                  <div class="username" @click.stop="handleUserClick(user.userId)">{{ user.username || user.realName }}</div>
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
          </AlumniOnlyBlur>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="verifyDialogVisible" title="校友认证申请" width="560px">
      <el-form ref="verifyFormRef" :model="verifyForm" :rules="verifyRules" label-width="92px">
        <el-form-item label="真实姓名" prop="realName"><el-input v-model="verifyForm.realName" :disabled="!canSubmitVerify" /></el-form-item>
        <el-form-item label="学号" prop="studentId"><el-input v-model="verifyForm.studentId" :disabled="!canSubmitVerify" /></el-form-item>
        <el-form-item label="入学年份" prop="admissionYear"><el-input v-model="verifyForm.admissionYear" type="number" :disabled="!canSubmitVerify" /></el-form-item>
        <el-form-item label="毕业年份"><el-input v-model="verifyForm.graduationYear" type="number" :disabled="!canSubmitVerify" /></el-form-item>
        <el-form-item label="学院"><el-input v-model="verifyForm.college" :disabled="!canSubmitVerify" /></el-form-item>
        <el-form-item label="专业"><el-input v-model="verifyForm.major" :disabled="!canSubmitVerify" /></el-form-item>
        <el-form-item label="学生卡照片" prop="studentCardImage">
          <el-upload
            :show-file-list="false"
            :auto-upload="false"
            accept=".jpg,.jpeg,.png,.webp"
            :on-change="handleStudentCardChange"
            :disabled="!canSubmitVerify"
          >
            <el-button :loading="studentCardUploading" :disabled="!canSubmitVerify">上传学生卡</el-button>
          </el-upload>
          <div v-if="studentCardDisplayUrl" class="student-card-preview-wrap">
            <el-image :src="studentCardDisplayUrl" fit="contain" class="student-card-preview" />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="verifyDialogVisible = false">取消</el-button>
        <el-button v-if="canSubmitVerify" type="primary" :loading="verifySubmitting" @click="submitVerify">提交申请</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editPostDialogVisible" title="编辑动态" width="620px">
      <el-form :model="editPostForm" label-width="76px">
        <el-form-item label="动态内容">
          <el-input
            v-model="editPostForm.content"
            type="textarea"
            :rows="5"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="可见性">
          <el-radio-group v-if="userStore.role !== 'USER'" v-model="editPostForm.visibility">
            <el-radio-button :label="0">公开</el-radio-button>
            <el-radio-button :label="1">仅校友</el-radio-button>
            <el-radio-button :label="2">仅自己</el-radio-button>
          </el-radio-group>
          <el-tag v-else type="info">普通用户仅支持公开发布</el-tag>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editPostDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editPostSubmitting" @click="submitEditPost">保存并重新发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, Star, StarFilled, ChatDotRound, MoreFilled } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { alumniApi } from '../api/alumni'
import { postApi } from '../api/post'
import { authApi } from '../api/auth'
import { resolveAvatarUrl } from '../utils/avatarUrl'
import UserInfoCard from '../components/UserInfoCard.vue'
import ProfileInfoTab from '../components/ProfileInfoTab.vue'
import ResolvedAvatar from '../components/ResolvedAvatar.vue'
import AlumniOnlyBlur from '../components/AlumniOnlyBlur.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const alumniId = ref(Number(route.params.id))
const userInfo = ref({})
const followStats = ref({ followingCount: 0, followerCount: 0 })
const userPosts = ref([])
const loading = ref(true)
const postsLoading = ref(false)
const activeTab = ref('posts')
const isFollowing = ref(false)
const hasMorePosts = ref(true)
const postsPageNum = ref(1)
const postsPageSize = ref(10)
const editPostDialogVisible = ref(false)
const editPostSubmitting = ref(false)
const editingPostId = ref(null)
const editPostForm = ref({
  content: '',
  visibility: 0
})

const followingList = ref([])
const followersList = ref([])
const followingLoading = ref(false)
const followersLoading = ref(false)
const followingPageNum = ref(1)
const followersPageNum = ref(1)
const followPageSize = ref(20)
const verifyStatusInfo = ref({
  verifyStatus: -1,
  verifyNotes: '',
  verifyTime: '',
  role: ''
})
const verifyDialogVisible = ref(false)
const verifySubmitting = ref(false)
const verifyFormRef = ref()
const verifyForm = ref({
  realName: '',
  studentId: '',
  admissionYear: '',
  graduationYear: '',
  college: '',
  major: '',
  studentCardImage: ''
})
const studentCardUploading = ref(false)
const studentCardDisplayUrl = ref('')
const verifyRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  studentId: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  admissionYear: [{ required: true, message: '请输入入学年份', trigger: 'blur' }],
  studentCardImage: [{ required: true, message: '请上传学生卡照片', trigger: 'change' }]
}

const showVerifyActionButton = computed(() => isSelf.value && userStore.role !== 'ADMIN')

const verifyActionText = computed(() => {
  const status = verifyStatusInfo.value.verifyStatus
  if (status === 0) return '认证审核中'
  if (status === 1) return '查看认证资料'
  if (status === 2) return '重新提交认证'
  return '申请校友认证'
})

const canSubmitVerify = computed(() => {
  const status = verifyStatusInfo.value.verifyStatus
  return status === -1 || status === 2
})

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

/** 普通用户查看他人主页：档案/关注/粉丝等需模糊 */
const otherProfileLocked = computed(
  () => !isSelf.value && userStore.role === 'USER'
)

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
        const d = currentProfileResponse.data
        userInfo.value = { ...d, enrollYear: d.admissionYear ?? d.enrollYear }
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

// 根据用户ID获取校友信息（与动态圈 /profile/:userId 一致，优先调档案接口）
const fetchProfileByUserId = async (userId) => {
  try {
    const res = await alumniApi.getProfileById(userId)
    if (res.code === 200 && res.data) {
      const d = res.data
      userInfo.value = {
        userId: d.userId,
        username: d.username,
        realName: d.realName,
        avatar: d.avatar ? String(d.avatar).replace(/`/g, '') : '',
        admissionYear: d.admissionYear,
        enrollYear: d.admissionYear,
        graduationYear: d.graduationYear,
        college: d.college,
        major: d.major,
        company: d.company,
        position: d.position,
        city: d.city,
        bio: d.bio || '',
        verifyStatus: d.verifyStatus,
        role: d.role,
        postCount: d.postCount
      }
      return
    }
  } catch (e) {
    console.warn('档案接口未返回，尝试从关注/动态兜底:', e)
  }

  try {
    const allUsers = [...followingList.value, ...followersList.value]
    const userFromList = allUsers.find((user) => user.userId === userId)

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
      return
    }

    const postsResponse = await postApi.getUserPosts(userId, 1, 1)
    if (postsResponse.code === 200 && postsResponse.data.list.length > 0) {
      const firstPost = postsResponse.data.list[0]
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
      return
    }

    userInfo.value = {
      userId,
      username: '',
      realName: '用户',
      avatar: '',
      admissionYear: '',
      graduationYear: '',
      college: '',
      major: '',
      company: '',
      position: '',
      city: '',
      bio: '',
      verifyStatus: null
    }
  } catch (error) {
    console.error('根据用户ID获取校友信息失败:', error)
    userInfo.value = {
      userId,
      username: '',
      realName: '用户',
      avatar: '',
      admissionYear: '',
      graduationYear: '',
      college: '',
      major: '',
      company: '',
      position: '',
      city: '',
      bio: '',
      verifyStatus: null
    }
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
      const normalizedList = await Promise.all((list || []).map(normalizePostImages))
      const totalNum = total != null ? Number(total) : 0

      // 动态总数（依赖后端 MyBatis-Plus 分页插件正确填充 total）
      userInfo.value.postCount = totalNum
      console.log('设置动态数:', totalNum)
      
      if (refresh) {
        userPosts.value = normalizedList
        // 如果用户信息为空，使用动态中的第一个用户信息
        if (normalizedList.length > 0 && Object.keys(userInfo.value).length === 0) {
          const firstPost = normalizedList[0]
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
            followingCount: 0,
            followerCount: 0
          }
        }
      } else {
        userPosts.value = [...userPosts.value, ...normalizedList]
      }
      
      postsPageNum.value = pageNum + 1
      hasMorePosts.value = normalizedList.length === pageSize
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

const isLockedPost = (post) => {
  if (!post) return false
  if (String(post.userId) === String(userStore.userId)) return false
  if (userStore.role !== 'USER') return false
  return post.locked === true || post.visibility === 1
}

const handleLikeClick = async (post) => {
  if (isLockedPost(post)) {
    ElMessage.warning('该动态仅校友可见，请先完成校友认证')
    return
  }
  try {
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

const handleCommentClick = (post) => {
  if (isLockedPost(post)) {
    ElMessage.warning('该动态仅校友可见，请先完成校友认证')
    return
  }
  router.push(`/post/${post.id}`)
}

const handlePostClick = (post) => {
  if (isLockedPost(post)) {
    ElMessage.warning('该动态仅校友可见，请先完成校友认证')
    return
  }
  router.push(`/post/${post.id}`)
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

const handleImageClick = (imageUrl, post) => {
  if (post && isLockedPost(post)) {
    ElMessage.warning('该动态仅校友可见，请先完成校友认证')
    return
  }
  console.log('查看图片:', imageUrl)
}

const isSelfPost = (post) => String(post?.userId) === String(userStore.userId)

const shouldShowMore = (post) => {
  const content = String(post?.content || '')
  const normalized = content.replace(/\r/g, '')
  const lineCount = normalized.split('\n').length
  return normalized.length > 120 || lineCount > 4
}

const normalizePostImages = async (post) => {
  const imageUrlList = Array.isArray(post?.imageUrlList) ? post.imageUrlList : []
  if (imageUrlList.length === 0) {
    return post
  }
  const resolvedList = await Promise.all(imageUrlList.map((url) => resolveAvatarUrl(url)))
  const displayList = resolvedList.map((item, index) => item || imageUrlList[index]).filter(Boolean)
  return {
    ...post,
    imageRawUrlList: imageUrlList,
    imageUrlList: displayList
  }
}

const handlePostCommand = async (command, post) => {
  if (command === 'edit') {
    editingPostId.value = post.id
    editPostForm.value = {
      content: post.content || '',
      visibility: userStore.role === 'USER' ? 0 : (post.visibility ?? 0)
    }
    editPostDialogVisible.value = true
    return
  }

  if (command === 'delete') {
    try {
      await ElMessageBox.confirm('删除后将无法恢复，是否继续？', '删除动态', {
        type: 'warning',
        confirmButtonText: '删除',
        cancelButtonText: '取消'
      })
      await postApi.deletePost(post.id)
      ElMessage.success('删除成功')
      await getUserPosts(true)
    } catch (error) {
      if (error !== 'cancel' && error !== 'close') {
        console.error('删除动态失败:', error)
        ElMessage.error('删除失败，请重试')
      }
    }
  }
}

const submitEditPost = async () => {
  const postId = editingPostId.value
  if (!postId) return
  const content = String(editPostForm.value.content || '').trim()
  if (!content) {
    ElMessage.warning('动态内容不能为空')
    return
  }
  editPostSubmitting.value = true
  try {
    const target = userPosts.value.find((item) => item.id === postId)
    const payload = {
      content,
      visibility: userStore.role === 'USER' ? 0 : Number(editPostForm.value.visibility ?? 0),
      imageUrls: target?.imageRawUrlList || target?.imageUrlList || []
    }
    await postApi.updatePost(postId, payload)
    ElMessage.success('动态已更新并重新发布')
    editPostDialogVisible.value = false
    await getUserPosts(true)
  } catch (error) {
    console.error('更新动态失败:', error)
    ElMessage.error('更新失败，请重试')
  } finally {
    editPostSubmitting.value = false
  }
}

const getVerifyTagType = (status) => {
  if (status === 1) return 'success'
  if (status === 0) return 'warning'
  if (status === 2) return 'danger'
  return 'info'
}

const getVerifyText = (status) => {
  if (status === 1) return '已通过'
  if (status === 0) return '审核中'
  if (status === 2) return '已驳回'
  return '未提交'
}

const syncRoleFromVerifyStatus = (role) => {
  if (!role || role === userStore.role) return
  userStore.role = role
  localStorage.setItem('role', role)
}

const loadVerifyStatus = async () => {
  if (!isSelf.value || userStore.role === 'ADMIN') return
  try {
    const res = await authApi.getAlumniVerifyStatus()
    if (res.code === 200 && res.data) {
      verifyStatusInfo.value = res.data
      syncRoleFromVerifyStatus(res.data.role)
    }
  } catch (error) {
    console.error('获取校友认证状态失败:', error)
  }
}

const openVerifyDialogPrefill = () => {
  verifyForm.value = {
    realName: userInfo.value.realName || '',
    studentId: userInfo.value.studentId || '',
    admissionYear: userInfo.value.admissionYear || '',
    graduationYear: userInfo.value.graduationYear || '',
    college: userInfo.value.college || '',
    major: userInfo.value.major || '',
    studentCardImage: userInfo.value.studentCardImage || ''
  }
  if (verifyForm.value.studentCardImage) {
    resolveAvatarUrl(verifyForm.value.studentCardImage).then((url) => {
      studentCardDisplayUrl.value = url || ''
    })
  } else {
    studentCardDisplayUrl.value = ''
  }
}

const handleStudentCardChange = async (file) => {
  if (!file?.raw) return
  studentCardUploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file.raw)
    const res = await authApi.uploadStudentCard(formData)
    if (res.code === 200 && res.data) {
      verifyForm.value.studentCardImage = res.data
      studentCardDisplayUrl.value = await resolveAvatarUrl(res.data)
      ElMessage.success('学生卡照片上传成功')
      verifyFormRef.value?.validateField?.('studentCardImage')
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (error) {
    console.error('上传学生卡照片失败:', error)
    ElMessage.error('上传失败，请重试')
  } finally {
    studentCardUploading.value = false
  }
}

const submitVerify = async () => {
  if (!canSubmitVerify.value) return
  if (!verifyFormRef.value) return
  await verifyFormRef.value.validate(async (valid) => {
    if (!valid) return
    verifySubmitting.value = true
    try {
      const payload = {
        ...verifyForm.value,
        admissionYear: Number(verifyForm.value.admissionYear),
        graduationYear: verifyForm.value.graduationYear ? Number(verifyForm.value.graduationYear) : null,
        studentCardImage: verifyForm.value.studentCardImage
      }
      const res = await authApi.submitAlumniVerify(payload)
      if (res.code === 200) {
        ElMessage.success('提交成功，等待管理员审核')
        verifyDialogVisible.value = false
        await loadVerifyStatus()
      }
    } catch (error) {
      console.error('提交校友认证失败:', error)
    } finally {
      verifySubmitting.value = false
    }
  })
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
      getUserPosts(true),
      loadVerifyStatus()
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

// 监听路由参数变化（先拉档案再拉动态，避免并行结束时档案覆盖掉已写入的 postCount）
watch(
  () => route.params.id,
  async (newId, oldId) => {
    console.log('路由参数变化:', { newId, oldId })
    if (newId !== oldId) {
      alumniId.value = Number(newId)
      loading.value = true
      try {
        await getUserProfile()
        await Promise.all([
          getFollowStats(),
          getFollowingList(),
          getFollowersList(),
          getUserPosts(true),
          loadVerifyStatus()
        ])
      } finally {
        loading.value = false
      }
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

watch(verifyDialogVisible, (visible) => {
  if (visible) openVerifyDialogPrefill()
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

.verify-card {
  background-color: #fff;
  border-radius: 8px;
}

.verify-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.verify-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  color: #606266;
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
  align-items: center;
}

.post-card {
  width: min(100%, 760px);
  background-color: #fff;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid transparent;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: box-shadow 0.3s;
}

.post-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.post-card.locked {
  border-color: rgba(245, 158, 11, 0.28);
}

.post-card.locked .text-content {
  filter: blur(6px);
  user-select: none;
}

.post-card.locked .image-item :deep(.el-image__inner) {
  filter: blur(8px);
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

.username-row {
  display: flex;
  align-items: center;
  gap: 8px;
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

.lock-banner {
  display: inline-flex;
  align-items: center;
  margin-bottom: 10px;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(245, 158, 11, 0.12);
  color: #b45309;
  font-size: 12px;
  font-weight: 600;
}

.text-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 6px;
  white-space: pre-wrap;
  word-break: break-word;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: calc(1.6em * 4);
}

.read-more {
  display: inline-block;
  font-size: 13px;
  font-weight: 600;
  color: var(--accent);
  margin-bottom: 10px;
  cursor: pointer;
}

.read-more:hover {
  opacity: 0.85;
  text-decoration: underline;
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

.action-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.action-buttons {
  display: flex;
  gap: 24px;
}

.post-more-trigger {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: 999px;
  color: #909399;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.post-more-trigger:hover {
  background: #f5f7fa;
  color: #409eff;
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

.student-card-preview-wrap {
  margin-top: 10px;
}

.student-card-preview {
  width: 220px;
  height: 140px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #fff;
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
