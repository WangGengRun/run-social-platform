import service from './request'

const formDataConfig = {
  transformRequest: [
    (data, headers) => {
      if (typeof FormData !== 'undefined' && data instanceof FormData) {
        if (headers && typeof headers.delete === 'function') {
          headers.delete('Content-Type')
        } else if (headers) {
          delete headers['Content-Type']
        }
      }
      return data
    }
  ]
}

export const authApi = {
  // 发送验证码
  sendCaptcha: (target, type = 'SMS', length = 6) => {
    return service.post('/auth/captcha/send', {
      target,
      type,
      length
    })
  },

  // 注册
  register: (registerDTO) => {
    return service.post('/auth/register', registerDTO)
  },

  // 登录
  login: (loginDTO) => {
    return service.post('/auth/login', loginDTO)
  },

  // 提交校友认证
  submitAlumniVerify: (verifyDTO) => {
    return service.post('/auth/alumni/verify', verifyDTO)
  },

  uploadStudentCard: (formData) => {
    return service.post('/auth/alumni/student-card/upload', formData, formDataConfig)
  },

  // 获取当前认证状态
  getAlumniVerifyStatus: () => {
    return service.get('/auth/alumni/verify/status')
  }
}

export default authApi