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

export const postApi = {
  getRecommendedPosts(pageNum, pageSize) {
    return service.get('/post/recommended', {
      params: { pageNum, pageSize }
    })
  },

  getFollowingPosts(pageNum, pageSize) {
    return service.get('/post/following', {
      params: { pageNum, pageSize },
      silentAuthError: true,
      silentBusinessError: true
    })
  },

  getHotPosts(pageNum, pageSize) {
    return service.get('/post/hot', {
      params: { pageNum, pageSize }
    })
  },

  getUserPosts(userId, pageNum, pageSize) {
    return service.get(`/post/user/${userId}`, {
      params: { pageNum, pageSize }
    })
  },

  getPostDetail(postId) {
    return service.get(`/post/${postId}`)
  },

  getComments(postId) {
    return service.get(`/post/${postId}/comments`)
  },

  deleteComment(commentId) {
    return service.delete(`/post/comment/${commentId}`)
  },

  addComment(postId, content, parentId = 0) {
    return service.post('/post/comment', {
      postId,
      content,
      parentId
    })
  },

  likePost(postId) {
    return service.post(`/post/${postId}/like`)
  },

  unlikePost(postId) {
    return service.delete(`/post/${postId}/like`)
  },

  uploadImage(formData) {
    return service.post('/post/image/upload', formData, formDataConfig)
  },

  publishPost(content, imageUrls, visibility) {
    return service.post('/post', {
      content,
      imageUrls,
      visibility
    })
  },

  updatePost(postId, payload) {
    return service.put(`/post/${postId}`, payload)
  },

  deletePost(postId) {
    return service.delete(`/post/${postId}`)
  }
}

export default postApi
