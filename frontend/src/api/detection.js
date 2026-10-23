import request from './request'

export function uploadFile(file) {
  const form = new FormData()
  form.append('file', file)
  return request.post('/user/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function detectDisease(data) {
  return request.post('/user/detection/detect', data)
}

export function detectionHistory(params) {
  return request.get('/user/detection/history', { params })
}

export function diagnosisChat(data) {
  return request.post('/user/diagnosis/chat', data)
}
