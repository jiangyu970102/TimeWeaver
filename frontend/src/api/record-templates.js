import request from '@/utils/request'

export function getTemplates() {
  return request.get('/record-templates')
}

export function createTemplate(data) {
  return request.post('/record-templates', data)
}

export function updateTemplate(id, data) {
  return request.put(`/record-templates/${id}`, data)
}

export function deleteTemplate(id) {
  return request.delete(`/record-templates/${id}`)
}
