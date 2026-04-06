import { defineStore } from 'pinia'

// 用户存储
export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userId: localStorage.getItem('userId') || '',
    username: localStorage.getItem('username') || '',
    role: localStorage.getItem('role') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}'),
    rememberPassword: localStorage.getItem('rememberPassword') === 'true'
  }),

  getters: {
    isLoggedIn: (state) => !!state.token
  },

  actions: {
    // 登录
    login(userData) {
      this.token = userData.token
      this.userId = userData.userId
      this.username = userData.username
      this.role = userData.role
      this.userInfo = userData.userInfo || {}

      // 保存到localStorage
      localStorage.setItem('token', this.token)
      localStorage.setItem('userId', this.userId)
      localStorage.setItem('username', this.username)
      localStorage.setItem('role', this.role)
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
    },

    // 登出
    logout() {
      this.token = ''
      this.userId = ''
      this.username = ''
      this.role = ''
      this.userInfo = {}

      // 从localStorage中移除
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('username')
      localStorage.removeItem('role')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('rememberPassword')
    },

    // 设置记住密码
    setRememberPassword(remember) {
      this.rememberPassword = remember
      localStorage.setItem('rememberPassword', remember)
    },

    /** 合并更新本地用户信息并持久化（用于改头像、改资料后与登录态一致） */
    patchUserInfo(partial) {
      if (!partial || typeof partial !== 'object') return
      this.userInfo = { ...this.userInfo, ...partial }
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
    }
  }
})
