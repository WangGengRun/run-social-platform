import service from './request'

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
  }
}

export default authApi