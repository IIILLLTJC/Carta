import request from '@/utils/request'

export function fetchUserPointPage(params) {
  return request.get('/api/user/points', { params })
}

export function fetchUserPointMap(params) {
  return request.get('/api/user/points/map', { params })
}
