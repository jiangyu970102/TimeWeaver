import request from '@/utils/request'

export function startPomodoro(params) {
  return request.post('/pomodoros/start', null, { params })
}

export function completePomodoro(id) {
  return request.put(`/pomodoros/${id}/complete`)
}

export function cancelPomodoro(id) {
  return request.put(`/pomodoros/${id}/cancel`)
}

export function getPomodoroStats() {
  return request.get('/pomodoros/stats')
}
