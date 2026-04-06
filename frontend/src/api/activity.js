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

export const activityApi = {
  /** 上传活动封面，返回 MinIO objectName，创建活动时填入 coverImage */
  uploadCover: (formData) => {
    return service.post('/activity/cover/upload', formData, formDataConfig)
  },

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

  getMySignupActivities: ({ pageNum = 1, pageSize = 20 } = {}) => {
    return service.get('/activity/my-signups', {
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
