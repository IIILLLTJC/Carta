import request from '@/utils/request'

export function fetchUserProfile() {
  return request.get('/api/user/profile')
}

export function updateUserProfile(data) {
  return request.put('/api/user/profile', data)
}
