import request from '@/utils/request'

export function fetchUserPage(params) {
  return request.get('/api/admin/users', { params })
}

export function fetchUserDetail(id) {
  return request.get(`/api/admin/users/${id}`)
}

export function createUser(data) {
  return request.post('/api/admin/users', data)
}

export function updateUser(id, data) {
  return request.put(`/api/admin/users/${id}`, data)
}

export function deleteUser(id) {
  return request.delete(`/api/admin/users/${id}`)
}

export function updateUserStatus(id, status) {
  return request.put(`/api/admin/users/${id}/status`, { status })
}