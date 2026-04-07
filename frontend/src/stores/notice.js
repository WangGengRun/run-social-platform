import { defineStore } from 'pinia'
import { noticeApi } from '../api/notice'

export const useNoticeStore = defineStore('notice', {
  state: () => ({
    unreadCount: 0
  }),

  getters: {
    hasUnreadNotices: (state) => state.unreadCount > 0
  },

  actions: {
    async fetchUnreadCount() {
      try {
        const res = await noticeApi.getUnreadCount()
        if (res.code === 200) {
          this.unreadCount = Number(res.data || 0)
        }
      } catch (e) {
        console.error('Failed to fetch notice unread count:', e)
      }
    },

    async markAllAsRead() {
      try {
        const res = await noticeApi.markAllAsRead()
        if (res.code === 200) {
          this.unreadCount = 0
        }
      } catch (e) {
        console.error('Failed to mark notices as read:', e)
      }
    }
  }
})

