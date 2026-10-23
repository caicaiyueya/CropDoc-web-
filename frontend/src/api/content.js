import request from './request'

export function newsList(params) {
  return request.get('/user/news/list', { params })
}

export function newsDetail(id) {
  return request.get(`/user/news/${id}`)
}

export function diseaseList(params) {
  return request.get('/user/disease/list', { params })
}

export function diseaseDetail(id) {
  return request.get(`/user/disease/${id}`)
}

export function cropList() {
  return request.get('/user/crop/list')
}

export function getProfile() {
  return request.get('/user/profile')
}

export function updateProfile(data) {
  return request.put('/user/profile', data)
}

export function favoriteList() {
  return request.get('/user/favorite/list')
}

export function createFeedback(data) {
  return request.post('/user/feedback/create', data)
}

export function feedbackList(params) {
  return request.get('/user/feedback/list', { params })
}

export function bannerList() {
  return request.get('/user/banner/list')
}

export function noticeList() {
  return request.get('/user/notice/list')
}
