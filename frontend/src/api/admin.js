import axios from 'axios'

// 创建axios实例
const service = axios.create({
  baseURL: '',
  timeout: 10000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 从本地存储获取token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      console.error('响应错误:', res.message)
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  error => {
    console.error('响应错误:', error)
    return Promise.reject(error)
  }
)

// 系统管理模块API
const adminApi = {
  // 认证相关API
  login(loginDTO) {
    return service.post('/api/auth/login', loginDTO)
  },
  
  register(registerDTO) {
    return service.post('/api/auth/register', registerDTO)
  },
  
  logout() {
    return service.post('/api/auth/logout')
  },
  
  sendCaptcha(target, type, length) {
    return service.post('/api/auth/captcha/send', {}, {
      params: {
        target,
        type,
        length
      }
    })
  },
  
  checkCaptcha(target) {
    return service.get('/api/auth/captcha/check', {
      params: {
        target
      }
    })
  },
  
  changePassword(oldPassword, newPassword) {
    return service.post('/api/auth/password/change', {}, {
      params: {
        oldPassword,
        newPassword
      }
    })
  },
  
  resetPassword(email, newPassword, captcha, captchaKey) {
    return service.post('/api/auth/password/reset', {}, {
      params: {
        email,
        newPassword,
        captcha,
        captchaKey
      }
    })
  },
  
  // 获取仪表盘数据
  getDashboardData() {
    return service.get('/api/admin/dashboard')
  },
  
  // 用户管理
  getUserList(pageNum, pageSize, queryDTO) {
    return service.get('/api/admin/users', {
      params: {
        pageNum,
        pageSize,
        ...queryDTO
      }
    })
  },
  
  getUserDetail(userId) {
    return service.get(`/api/admin/users/${userId}`)
  },
  
  updateUserStatus(userId, status) {
    return service.put(`/api/admin/users/${userId}/status`, {}, {
      params: { status }
    })
  },
  
  updateUserRole(userId, role) {
    return service.put(`/api/admin/users/${userId}/role`, {}, {
      params: { role }
    })
  },
  
  // 内容审核
  getPostList(pageNum, pageSize, status, keyword) {
    return service.get('/api/admin/posts', {
      params: {
        pageNum,
        pageSize,
        status,
        keyword
      }
    })
  },
  
  auditPost(postId, status, reason) {
    return service.post(`/api/admin/posts/${postId}/audit`, {}, {
      params: {
        status,
        reason
      }
    })
  },
  
  getCommentList(pageNum, pageSize, status, keyword) {
    return service.get('/api/admin/comments', {
      params: {
        pageNum,
        pageSize,
        status,
        keyword
      }
    })
  },
  
  auditComment(commentId, status, reason) {
    return service.post(`/api/admin/comments/${commentId}/audit`, {}, {
      params: {
        status,
        reason
      }
    })
  },
  
  // 活动管理
  getActivityList(pageNum, pageSize, status, keyword) {
    return service.get('/api/admin/activities', {
      params: {
        pageNum,
        pageSize,
        status,
        keyword
      }
    })
  },
  
  updateActivityStatus(activityId, status) {
    return service.put(`/api/admin/activities/${activityId}/status`, {}, {
      params: { status }
    })
  },
  
  deleteActivity(activityId) {
    return service.delete(`/api/admin/activities/${activityId}`)
  },
  
  // 数据统计
  getUserStatistics() {
    return service.get('/api/admin/statistics/users')
  },
  
  getActivityStatistics() {
    return service.get('/api/admin/statistics/activities')
  },
  
  getInteractionStatistics() {
    return service.get('/api/admin/statistics/interactions')
  }
}

export default adminApi