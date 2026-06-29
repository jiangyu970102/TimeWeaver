import request from '@/utils/request'

export function getDailyStats(params) {
  return request.get('/stats/daily', { params })
}

export function getWeeklyStats(params) {
  return request.get('/stats/weekly', { params })
}

export function getMonthlyStats(params) {
  return request.get('/stats/monthly', { params })
}

export function getHeatmapData(params) {
  return request.get('/stats/heatmap', { params })
}

export function getTrendData(params) {
  return request.get('/stats/trend', { params })
}

export function getComparisonData(params) {
  return request.get('/stats/comparison', { params })
}

export function getCategoryStats(params) {
  return request.get('/stats/categories', { params })
}
