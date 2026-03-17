import request from '@/utils/request'

export function fetchRegionPage(params) {
  return request.get('/api/admin/regions', { params })
}

export function fetchRegionDetail(id) {
  return request.get(`/api/admin/regions/${id}`)
}

export function createRegion(data) {
  return request.post('/api/admin/regions', data)
}

export function updateRegion(id, data) {
  return request.put(`/api/admin/regions/${id}`, data)
}

export function deleteRegion(id) {
  return request.delete(`/api/admin/regions/${id}`)
}

export function updateRegionStatus(id, status) {
  return request.put(`/api/admin/regions/${id}/status`, { status })
}

export function fetchRegionOptions() {
  return request.get('/api/admin/regions/options')
}