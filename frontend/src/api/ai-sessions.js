import request from '@/utils/request'

export function getAiSessions(params) {
  return request.get('/ai-sessions', { params })
}

export function getAiSessionStats(params) {
  return request.get('/ai-sessions/stats', { params })
}

export function getAiSessionDetail(id) {
  return request.get(`/ai-sessions/${id}`)
}

export function createAiSession(data) {
  return request.post('/ai-sessions', data)
}

export function updateAiSession(id, data) {
  return request.put(`/ai-sessions/${id}`, data)
}

export function deleteAiSession(id) {
  return request.delete(`/ai-sessions/${id}`)
}
