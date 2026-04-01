import service from './request'

export const postApi = {
  getRecommendedPosts(pageNum, pageSize) {
    return service.get('/post/recommended', {
      params: { pageNum, pageSize }
    })
  },

  getFollowingPosts(pageNum, pageSize) {
    return service.get('/post/following', {
      params: { pageNum, pageSize }
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

  publishPost(content, imageUrls, visibility) {
    return service.post('/post', {
      content,
      imageUrls,
      visibility
    })
  }
}

export default postApi
