import request from '@/utils/request'

export function getGoals() {
  return request.get('/goals')
}

export function createGoal(data) {
  return request.post('/goals', data)
}

export function updateGoal(id, data) {
  return request.put(`/goals/${id}`, data)
}

export function deleteGoal(id) {
  return request.delete(`/goals/${id}`)
}

export function getGoalSummary() {
  return request.get('/goals/summary')
}

export function updateGoalStatus(id, data) {
  return request.put(`/goals/${id}/status`, data)
}

export function recalculateGoal(id) {
  return request.post(`/goals/${id}/recalculate`)
}
