import service from './request'

export const alumniApi = {
  getCurrentProfile: () => {
    return service.get('/alumni/profile/current')
  },

  getProfileById: (id) => {
    return service.get(`/alumni/profile/${id}`)
  },

  followUser: (followeeId) => {
    return service.post(`/follow/${followeeId}`)
  },

  unfollowUser: (followeeId) => {
    return service.delete(`/follow/${followeeId}`)
  },

  getFollowStats: (userId) => {
    return service.get(`/follow/stats/${userId}`)
  },

  updateProfile: (profileData) => {
    return service.put('/alumni/profile/current', profileData)
  },

  uploadAvatar: (formData) => {
    // 不要手写 multipart/form-data（缺 boundary）；并去掉实例默认的 application/json，交给浏览器带 boundary
    return service.post('/avatar/upload', formData, {
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
    })
  },

  getAvatarUrl: (objectName) => {
    return service.get('/avatar/getUrl', {
      params: { objectName }
    })
  },

  deleteAvatar: (objectName) => {
    return service.delete('/avatar/delete', {
      params: { objectName }
    })
  },

  getFollowers: (pageNum, pageSize) => {
    return service.get('/follow/follower', {
      params: { pageNum, pageSize }
    })
  },

  getFollowing: (pageNum, pageSize) => {
    return service.get('/follow/following', {
      params: { pageNum, pageSize }
    })
  },

  getUserFollowing: (userId, pageNum, pageSize) => {
    return service.get(`/follow/user/${userId}/following`, {
      params: { pageNum, pageSize }
    })
  },

  getUserFollowers: (userId, pageNum, pageSize) => {
    return service.get(`/follow/user/${userId}/follower`, {
      params: { pageNum, pageSize }
    })
  }
}

export default alumniApi