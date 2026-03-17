import request from '@/utils/request'

export function fetchDeployPage(params) {
  return request.get('/api/admin/deploy-records', { params })
}

export function fetchDeployDetail(id) {
  return request.get(`/api/admin/deploy-records/${id}`)
}

export function createDeploy(data) {
  return request.post('/api/admin/deploy-records', data)
}

export function updateDeploy(id, data) {
  return request.put(`/api/admin/deploy-records/${id}`, data)
}

export function deleteDeploy(id) {
  return request.delete(`/api/admin/deploy-records/${id}`)
}

export function updateDeployStatus(id, status) {
  return request.put(`/api/admin/deploy-records/${id}/status`, { status })
}

export function fetchDeployFormOptions() {
  return request.get('/api/admin/deploy-records/form-options')
}