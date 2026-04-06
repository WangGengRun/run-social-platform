import { defineStore } from 'pinia'
import { postApi } from '../api/post'
import { resolveAvatarUrl } from '../utils/avatarUrl'

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
    mergeUniquePosts(oldList = [], newList = []) {
      const merged = [...oldList]
      const idSet = new Set(oldList.map(item => item?.id))
      for (const item of newList) {
        const id = item?.id
        if (id == null || idSet.has(id)) continue
        merged.push(item)
        idSet.add(id)
      }
      return merged
    },

    async normalizePostImages(list = []) {
      return Promise.all((list || []).map(async (post) => {
        const imageUrlList = Array.isArray(post?.imageUrlList) ? post.imageUrlList : []
        if (imageUrlList.length === 0) return post
        const resolvedList = await Promise.all(imageUrlList.map((url) => resolveAvatarUrl(url)))
        const displayList = resolvedList.map((item, index) => item || imageUrlList[index]).filter(Boolean)
        return {
          ...post,
          imageRawUrlList: imageUrlList,
          imageUrlList: displayList
        }
      }))
    },

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
          const { list, total, pageNum, pageSize, current, size, totalPages, pages } = response.data
          const normalizedList = await this.normalizePostImages(list || [])
          const currentPage = Number(current ?? pageNum ?? this.recommendedPosts.pageNum)
          const currentSize = Number(size ?? pageSize ?? this.recommendedPosts.pageSize)
          const totalPageCount = Number(totalPages ?? pages ?? 0)
          const oldLen = this.recommendedPosts.list.length
          
          if (refresh) {
            this.recommendedPosts.list = normalizedList
          } else {
            this.recommendedPosts.list = this.mergeUniquePosts(this.recommendedPosts.list, normalizedList)
          }
          
          this.recommendedPosts.total = total
          this.recommendedPosts.pageNum = Number.isFinite(currentPage) ? currentPage + 1 : this.recommendedPosts.pageNum + 1
          if (Number.isFinite(totalPageCount) && totalPageCount > 0 && Number.isFinite(currentPage)) {
            this.recommendedPosts.hasMore = currentPage < totalPageCount
          } else {
            this.recommendedPosts.hasMore = normalizedList.length >= currentSize && normalizedList.length > 0
          }
          if (!refresh && this.recommendedPosts.list.length === oldLen) {
            this.recommendedPosts.hasMore = false
          }
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
          const { list, total, pageNum, pageSize, current, size, totalPages, pages } = response.data
          const normalizedList = await this.normalizePostImages(list || [])
          const currentPage = Number(current ?? pageNum ?? this.followingPosts.pageNum)
          const currentSize = Number(size ?? pageSize ?? this.followingPosts.pageSize)
          const totalPageCount = Number(totalPages ?? pages ?? 0)
          const oldLen = this.followingPosts.list.length
          
          if (refresh) {
            this.followingPosts.list = normalizedList
          } else {
            this.followingPosts.list = this.mergeUniquePosts(this.followingPosts.list, normalizedList)
          }
          
          this.followingPosts.total = total
          this.followingPosts.pageNum = Number.isFinite(currentPage) ? currentPage + 1 : this.followingPosts.pageNum + 1
          if (Number.isFinite(totalPageCount) && totalPageCount > 0 && Number.isFinite(currentPage)) {
            this.followingPosts.hasMore = currentPage < totalPageCount
          } else {
            this.followingPosts.hasMore = normalizedList.length >= currentSize && normalizedList.length > 0
          }
          if (!refresh && this.followingPosts.list.length === oldLen) {
            this.followingPosts.hasMore = false
          }
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
          const { list, total, pageNum, pageSize, current, size, totalPages, pages } = response.data
          const normalizedList = await this.normalizePostImages(list || [])
          const currentPage = Number(current ?? pageNum ?? this.hotPosts.pageNum)
          const currentSize = Number(size ?? pageSize ?? this.hotPosts.pageSize)
          const totalPageCount = Number(totalPages ?? pages ?? 0)
          const oldLen = this.hotPosts.list.length
          
          if (refresh) {
            this.hotPosts.list = normalizedList
          } else {
            this.hotPosts.list = this.mergeUniquePosts(this.hotPosts.list, normalizedList)
          }
          
          this.hotPosts.total = total
          this.hotPosts.pageNum = Number.isFinite(currentPage) ? currentPage + 1 : this.hotPosts.pageNum + 1
          if (Number.isFinite(totalPageCount) && totalPageCount > 0 && Number.isFinite(currentPage)) {
            this.hotPosts.hasMore = currentPage < totalPageCount
          } else {
            this.hotPosts.hasMore = normalizedList.length >= currentSize && normalizedList.length > 0
          }
          if (!refresh && this.hotPosts.list.length === oldLen) {
            this.hotPosts.hasMore = false
          }
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
