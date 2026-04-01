import service from './request'

export const activityApi = {
  createActivity: (activityData) => {
    return service.post('/activity', activityData)
  }
}

export default activityApi
