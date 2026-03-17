import request from '@/utils/request'

export function fetchAdminPage(params) {
  return request.get('/api/admin/admins', { params })
}

export function fetchAdminDetail(id) {
  return request.get(`/api/admin/admins/${id}`)
}

export function createAdmin(data) {
  return request.post('/api/admin/admins', data)
}

export function updateAdmin(id, data) {
  return request.put(`/api/admin/admins/${id}`, data)
}

export function deleteAdmin(id) {
  return request.delete(`/api/admin/admins/${id}`)
}

export function updateAdminStatus(id, status) {
  return request.put(`/api/admin/admins/${id}/status`, { status })
}