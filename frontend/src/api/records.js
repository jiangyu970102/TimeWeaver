import request from '@/utils/request'

export function getRecords(params) {
  return request.get('/records', { params })
}

export function createRecord(data) {
  return request.post('/records', data)
}

export function stopRecord(id) {
  return request.put(`/records/${id}/stop`)
}

export function updateRecord(id, data) {
  return request.put(`/records/${id}`, data)
}

export function deleteRecord(id) {
  return request.delete(`/records/${id}`)
}

export function getCurrentRecord() {
  return request.get('/records/current')
}
