import service from './request'

export const noticeApi = {
  getNoticeList: (pageNum = 1, pageSize = 20) => {
    return service.get('/notice/list', {
      params: { pageNum, pageSize }
    })
  },

  getUnreadCount: () => {
    return service.get('/notice/unread')
  },

  markAllAsRead: () => {
    return service.put('/notice/read')
  }
}

export default noticeApi

