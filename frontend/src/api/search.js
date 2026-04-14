import service from './request'

export const searchApi = {
  searchPosts(keyword, pageNum = 1, pageSize = 20) {
    return service.get('/post/search', { params: { keyword, pageNum, pageSize } })
  },
  searchAlumni(keyword, pageNum = 1, pageSize = 20) {
    return service.post('/alumni/search', { keyword, pageNum, pageSize })
  }
}

export default searchApi

