import request from './request'

export function register(data) {
  return request.post('/auth/register', data)
}

export function login(data) {
  return request.post('/auth/login', data)
}

export function changePassword(data) {
  return request.post('/auth/change-password', data)
}

export function resetPassword(data) {
  return request.post('/auth/reset-password', data)
}
