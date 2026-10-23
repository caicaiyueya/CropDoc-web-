import request from './request'

export function postList(params) {
  return request.get('/user/post/list', { params })
}

export function postDetail(id) {
  return request.get(`/user/post/${id}`)
}

export function createPost(data) {
  return request.post('/user/post/create', data)
}

export function createGuidePost(data) {
  return request.post('/user/post/create-guide', data)
}

export function createComment(data) {
  return request.post('/user/comment/create', data)
}

export function markQuality(id) {
  return request.post(`/user/comment/${id}/mark-quality`)
}

export function toggleFavorite(postId) {
  return request.post('/user/favorite/toggle', { postId })
}

export function createReport(data) {
  return request.post('/user/report/create', data)
}
