import { defineStore } from 'pinia'
import { postApi } from '../api/post'

export const usePostStore = defineStore('post', {
  state: () => ({
    recommendedPosts: {
      list: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
      loading: false,
      hasMore: true
    },
    followingPosts: {
      list: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
      loading: false,
      hasMore: true
    },
    hotPosts: {
      list: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
      loading: false,
      hasMore: true
    }
  }),

  getters: {
    getPostsByType: (state) => (type) => {
      return state[`${type}Posts`]
    }
  },

  actions: {
    async fetchRecommendedPosts(refresh = false) {
      if (this.recommendedPosts.loading) return
      
      if (refresh) {
        this.recommendedPosts.pageNum = 1
        this.recommendedPosts.list = []
        this.recommendedPosts.hasMore = true
      }
      
      if (!this.recommendedPosts.hasMore) return
      
      this.recommendedPosts.loading = true
      
      try {
        const response = await postApi.getRecommendedPosts(
          this.recommendedPosts.pageNum,
          this.recommendedPosts.pageSize
        )
        
        if (response.code === 200) {
          const { list, total, pageNum, pageSize } = response.data
          
          if (refresh) {
            this.recommendedPosts.list = list
          } else {
            this.recommendedPosts.list = [...this.recommendedPosts.list, ...list]
          }
          
          this.recommendedPosts.total = total
          this.recommendedPosts.pageNum = pageNum + 1
          this.recommendedPosts.hasMore = list.length === pageSize
        }
      } catch (error) {
        console.error('获取推荐动态失败:', error)
      } finally {
        this.recommendedPosts.loading = false
      }
    },

    async fetchFollowingPosts(refresh = false) {
      if (this.followingPosts.loading) return
      
      if (refresh) {
        this.followingPosts.pageNum = 1
        this.followingPosts.list = []
        this.followingPosts.hasMore = true
      }
      
      if (!this.followingPosts.hasMore) return
      
      this.followingPosts.loading = true
      
      try {
        const response = await postApi.getFollowingPosts(
          this.followingPosts.pageNum,
          this.followingPosts.pageSize
        )
        
        if (response.code === 200) {
          const { list, total, pageNum, pageSize } = response.data
          
          if (refresh) {
            this.followingPosts.list = list
          } else {
            this.followingPosts.list = [...this.followingPosts.list, ...list]
          }
          
          this.followingPosts.total = total
          this.followingPosts.pageNum = pageNum + 1
          this.followingPosts.hasMore = list.length === pageSize
        }
      } catch (error) {
        console.error('获取关注动态失败:', error)
      } finally {
        this.followingPosts.loading = false
      }
    },

    async fetchHotPosts(refresh = false) {
      if (this.hotPosts.loading) return
      
      if (refresh) {
        this.hotPosts.pageNum = 1
        this.hotPosts.list = []
        this.hotPosts.hasMore = true
      }
      
      if (!this.hotPosts.hasMore) return
      
      this.hotPosts.loading = true
      
      try {
        const response = await postApi.getHotPosts(
          this.hotPosts.pageNum,
          this.hotPosts.pageSize
        )
        
        if (response.code === 200) {
          const { list, total, pageNum, pageSize } = response.data
          
          if (refresh) {
            this.hotPosts.list = list
          } else {
            this.hotPosts.list = [...this.hotPosts.list, ...list]
          }
          
          this.hotPosts.total = total
          this.hotPosts.pageNum = pageNum + 1
          this.hotPosts.hasMore = list.length === pageSize
        }
      } catch (error) {
        console.error('获取热门动态失败:', error)
      } finally {
        this.hotPosts.loading = false
      }
    },

    async likePost(postId, type) {
      const posts = this[`${type}Posts`]
      const post = posts.list.find(p => p.id === postId)
      
      if (!post) return
      
      const originalLiked = post.isLiked
      const originalLikeCount = post.likeCount
      
      post.isLiked = true
      post.likeCount += 1
      
      try {
        await postApi.likePost(postId)
      } catch (error) {
        post.isLiked = originalLiked
        post.likeCount = originalLikeCount
        console.error('点赞失败:', error)
        throw error
      }
    },

    async unlikePost(postId, type) {
      const posts = this[`${type}Posts`]
      const post = posts.list.find(p => p.id === postId)
      
      if (!post) return
      
      const originalLiked = post.isLiked
      const originalLikeCount = post.likeCount
      
      post.isLiked = false
      post.likeCount -= 1
      
      try {
        await postApi.unlikePost(postId)
      } catch (error) {
        post.isLiked = originalLiked
        post.likeCount = originalLikeCount
        console.error('取消点赞失败:', error)
        throw error
      }
    },

    toggleLike(postId, type) {
      const posts = this[`${type}Posts`]
      const post = posts.list.find(p => p.id === postId)
      
      if (!post) return Promise.reject(new Error('动态不存在'))
      
      if (post.isLiked) {
        return this.unlikePost(postId, type)
      } else {
        return this.likePost(postId, type)
      }
    }
  }
})
