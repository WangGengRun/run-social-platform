import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const service = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
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
    if (res.code !== 200 && res.code !== 0) {
      if (response?.config?.silentBusinessError) {
        return Promise.reject(new Error(res.message || res.msg || '请求失败'))
      }
      ElMessage.error(res.message || res.msg || '请求失败')
      return Promise.reject(new Error(res.message || res.msg || '请求失败'))
    }
    return res
  },
  error => {
    console.error('响应错误:', error)
    let message = '网络错误'
    if (error?.code === 'ECONNABORTED' || String(error?.message || '').toLowerCase().includes('timeout')) {
      message = '请求超时，请稍后重试'
    }
    if (error.response) {
      const data = error.response.data
      if (error.response.status === 401) {
        message = '未授权，请重新登录'
        localStorage.removeItem('token')
        localStorage.removeItem('userId')
        localStorage.removeItem('username')
        localStorage.removeItem('role')
        localStorage.removeItem('userInfo')
        window.location.href = '/login'
      } else if (data && (typeof data.msg === 'string' || typeof data.message === 'string')) {
        message = data.msg || data.message
      } else {
        switch (error.response.status) {
          case 403:
            message = '拒绝访问'
            break
          case 404:
            message = '请求地址不存在'
            break
          case 500:
            message = '服务器内部错误'
            break
          default:
            message = `请求失败 (${error.response.status})`
        }
      }
    }
    if (error?.config?.silentAuthError && error?.response?.status === 403) {
      return Promise.reject(error)
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default service