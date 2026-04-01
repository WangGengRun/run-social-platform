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
    return service.post('/avatar/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
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