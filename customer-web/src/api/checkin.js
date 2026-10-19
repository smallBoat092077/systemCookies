import request from './request'

/** 某道菜的打卡/评论列表（公开，按时间倒序） */
export const fetchCheckInPage = (recipeId, pageNum = 1, pageSize = 10) =>
  request.get(`/check-in/recipe/${recipeId}`, { params: { pageNum, pageSize } })

/** 某道菜的评论总数 */
export const fetchCheckInCount = (recipeId) => request.get(`/check-in/recipe/${recipeId}/count`)

/** 发布打卡 */
export const createCheckIn = (data) => request.post('/check-in', data)

/** 删除自己的打卡 */
export const deleteCheckIn = (id) => request.delete(`/check-in/${id}`)

/** 我的打卡 */
export const fetchMyCheckIns = (pageNum = 1, pageSize = 10) =>
  request.get('/check-in/mine', { params: { pageNum, pageSize } })

/** 今日打卡次数 */
export const fetchTodayCount = () => request.get('/check-in/today-count')

/** 某条打卡的回复列表（公开，按时间正序） */
export const fetchReplies = (checkInId, pageNum = 1, pageSize = 10) =>
  request.get(`/check-in/${checkInId}/replies`, { params: { pageNum, pageSize } })

/** 回复某条打卡 */
export const createReply = (checkInId, data) => request.post(`/check-in/${checkInId}/replies`, data)

/** 删除自己的回复 */
export const deleteReply = (id) => request.delete(`/check-in/replies/${id}`)

/** 我收到的回复（别人回复我的打卡） */
export const fetchReplyInbox = (pageNum = 1, pageSize = 10) =>
  request.get('/check-in/replies/inbox', { params: { pageNum, pageSize } })

/** 我发出的回复 */
export const fetchMyReplies = (pageNum = 1, pageSize = 10) =>
  request.get('/check-in/replies/mine', { params: { pageNum, pageSize } })

/** 上传打卡图片，返回 { url } */
export const uploadImage = (file) => {
  const form = new FormData()
  form.append('file', file)
  return request.post('/upload/image', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
