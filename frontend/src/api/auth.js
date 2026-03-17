import request from '@/utils/request'

export function fetchAuthProfile() {
  return request.get('/api/auth/profile')
}