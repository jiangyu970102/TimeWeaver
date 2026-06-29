import request from '@/utils/request'

export function login(data) {
  return request.post('/auth/login', data)
}

export function register(data) {
  return request.post('/auth/register', data)
}

export function refreshToken(token) {
  return request.post('/auth/refresh', { token })
}

export function parseJwt(token) {
  try {
    return JSON.parse(atob(token.split('.')[1]))
  } catch { return null }
}
