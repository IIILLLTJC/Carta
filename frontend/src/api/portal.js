import request from '@/utils/request'

export function fetchAdminDashboard() {
  return request.get('/api/admin/dashboard')
}

export function fetchUserDashboard() {
  return request.get('/api/user/dashboard')
}