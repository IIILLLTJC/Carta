import request from '@/utils/request'

export function fetchReturnPage(params) {
  return request.get('/api/admin/returns', { params })
}

export function fetchReturnDetail(id) {
  return request.get(`/api/admin/returns/${id}`)
}

export function createReturn(data) {
  return request.post('/api/admin/returns', data)
}

export function updateReturn(id, data) {
  return request.put(`/api/admin/returns/${id}`, data)
}

export function deleteReturn(id) {
  return request.delete(`/api/admin/returns/${id}`)
}

export function updateReturnStatus(id, status) {
  return request.put(`/api/admin/returns/${id}/status`, { status })
}

export function fetchReturnFormOptions() {
  return request.get('/api/admin/returns/form-options')
}