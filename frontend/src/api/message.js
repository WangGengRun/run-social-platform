import service from './request'

export const messageApi = {
  // 获取会话列表
  getConversations: (pageNum, pageSize) => {
    return service.get('/message/conversations', {
      params: { pageNum, pageSize }
    })
  },
  
  // 获取总未读消息数
  getUnreadCount: () => {
    return service.get('/message/unread')
  },
  
  // 获取与指定用户的未读消息数
  getUnreadCountByUserId: (userId) => {
    return service.get('/message/unread', {
      params: { userId }
    })
  },
  
  // 获取与指定用户的消息列表
  getMessages: (userId, pageNum, pageSize) => {
    return service.get(`/message/user/${userId}`, {
      params: { pageNum, pageSize }
    })
  },
  
  // 发送消息
  sendMessage: (receiverId, content, messageType = 1) => {
    return service.post('/message', {
      receiverId,
      content,
      messageType
    })
  },
  
  // 标记消息为已读
  markAsRead: (userId) => {
    return service.put(`/message/read/${userId}`)
  }
}

export default messageApi