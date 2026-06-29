import request from '@/utils/request'

export function getWeeklyReport(params) {
  return request.get('/reports/weekly', { params })
}

export function generateReport(params) {
  return request.post('/reports/weekly/generate', null, { params })
}

export function getReportList(params) {
  return request.get('/reports/list', { params })
}
