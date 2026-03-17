import request from '@/utils/request'

export function fetchRecoveryPage(params) {
  return request.get('/api/admin/recovery-records', { params })
}

export function fetchRecoveryDetail(id) {
  return request.get(`/api/admin/recovery-records/${id}`)
}

export function createRecovery(data) {
  return request.post('/api/admin/recovery-records', data)
}

export function updateRecovery(id, data) {
  return request.put(`/api/admin/recovery-records/${id}`, data)
}

export function deleteRecovery(id) {
  return request.delete(`/api/admin/recovery-records/${id}`)
}

export function updateRecoveryStatus(id, status) {
  return request.put(`/api/admin/recovery-records/${id}/status`, { status })
}

export function fetchRecoveryFormOptions() {
  return request.get('/api/admin/recovery-records/form-options')
}