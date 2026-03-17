import request from '@/utils/request'

export function fetchUserPointPage(params) {
  return request.get('/api/user/points', { params })
}