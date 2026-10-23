import request from './request'

/** 用户端认证 */
export function submitCertification(data) {
  return request.post('/user/certification/submit', data)
}

export function myCertifications() {
  return request.get('/user/certification/my')
}

export function certificationStatus() {
  return request.get('/user/certification/status')
}

/** 管理员认证审核 */
export function adminCertList(params) {
  return request.get('/admin/certification/list', { params })
}

export function approveCertification(id, data) {
  return request.post(`/admin/certification/${id}/approve`, data || {})
}

export function rejectCertification(id, data) {
  return request.post(`/admin/certification/${id}/reject`, data || {})
}

export function certificationLogs() {
  return request.get('/admin/certification/logs')
}
