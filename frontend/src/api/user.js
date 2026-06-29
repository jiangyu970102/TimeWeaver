import request from '@/utils/request'

export function getProfile() {
  return request.get('/user/profile')
}

export function updateProfile(data) {
  return request.put('/user/profile', data)
}

export function updatePreferences(data) {
  return request.put('/user/preferences', data)
}
