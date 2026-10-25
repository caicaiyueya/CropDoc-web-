import request from './request'

export function dashboardStats() {
  return request.get('/admin/dashboard/stats')
}

/** 补齐与用户端一致的演示数据（病害 / 资讯 / 社区帖） */
export function syncDemoContent() {
  return request.post('/admin/demo/sync')
}

export function adminUserList(params) {
  return request.get('/admin/user/list', { params })
}

export function resetUserPassword(id, data) {
  return request.post(`/admin/user/${id}/reset-password`, data || {})
}

export function changeUserRole(id, data) {
  return request.post(`/admin/user/${id}/role`, data)
}

export function disableUser(id) {
  return request.post(`/admin/user/${id}/disable`)
}

export function enableUser(id) {
  return request.post(`/admin/user/${id}/enable`)
}

export function pendingPosts(params) {
  return request.get('/admin/post/pending', { params })
}

/** 管理员帖子列表；params.status 可选 PENDING/APPROVED/REJECTED/OFFLINE */
export function adminPostList(params) {
  return request.get('/admin/post/list', { params })
}

export function approvePost(id) {
  return request.post(`/admin/post/${id}/approve`)
}

export function rejectPost(id) {
  return request.post(`/admin/post/${id}/reject`)
}

export function offlinePost(id) {
  return request.post(`/admin/post/${id}/offline`)
}

export function reportList(params) {
  return request.get('/admin/report/list', { params })
}

export function warnUser(id, data) {
  return request.post(`/admin/user/${id}/warn`, data || {})
}

export function adminNewsList(params) {
  return request.get('/admin/news/list', { params })
}

export function saveNews(data) {
  return request.post('/admin/news/save', data)
}

export function deleteNews(id) {
  return request.post(`/admin/news/${id}/delete`)
}

export function newsOnline(id) {
  return request.post(`/admin/news/${id}/online`)
}

export function newsOffline(id) {
  return request.post(`/admin/news/${id}/offline`)
}

export function adminDiseaseList(params) {
  return request.get('/admin/disease/list', { params })
}

export function saveDisease(data) {
  return request.post('/admin/disease/save', data)
}

export function deleteDisease(id) {
  return request.post(`/admin/disease/${id}/delete`)
}

export function saveCrop(data) {
  return request.post('/admin/crop/save', data)
}

export function adminBannerList() {
  return request.get('/admin/banner/list')
}

export function saveBanner(data) {
  return request.post('/admin/banner/save', data)
}

export function deleteBanner(id) {
  return request.post(`/admin/banner/${id}/delete`)
}

export function adminNoticeList() {
  return request.get('/admin/notice/list')
}

export function saveNotice(data) {
  return request.post('/admin/notice/save', data)
}

export function adminFeedbackList(params) {
  return request.get('/admin/feedback/list', { params })
}

export function replyFeedback(id, data) {
  return request.post(`/admin/feedback/${id}/reply`, data)
}
