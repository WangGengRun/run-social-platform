import service from './request'

export const activityApi = {
  createActivity: (activityData) => {
    return service.post('/activity', activityData)
  },
  updateActivity: (id, activityData) => {
    return service.put(`/activity/${id}`, activityData)
  },

  publishActivity: (id) => {
    return service.post(`/activity/${id}/publish`)
  },

  cancelActivity: (id) => {
    return service.post(`/activity/${id}/cancel`)
  },

  getActivityDetail: (id) => {
    return service.get(`/activity/${id}`)
  },

  getActivityList: ({ pageNum = 1, pageSize = 20, status, keyword } = {}) => {
    return service.get('/activity/list', {
      params: {
        pageNum,
        pageSize,
        status,
        keyword
      }
    })
  },

  getMyActivities: ({ pageNum = 1, pageSize = 20 } = {}) => {
    return service.get('/activity/my', {
      params: {
        pageNum,
        pageSize
      }
    })
  },

  signupActivity: (id) => {
    return service.post(`/activity/${id}/signup`)
  },

  cancelSignupActivity: (id) => {
    return service.post(`/activity/${id}/cancel-signup`)
  },

  checkinActivity: (id) => {
    return service.post(`/activity/${id}/checkin`)
  },

  getParticipants: ({ activityId, pageNum = 1, pageSize = 50 } = {}) => {
    return service.get(`/activity/${activityId}/participants`, {
      params: {
        pageNum,
        pageSize
      }
    })
  }
}

export default activityApi
