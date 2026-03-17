import request from '../utils/request'

export function fetchUserProfile() {
  return request({
    url: '/user/profile',
    method: 'get',
  })
}

export function updateUserProfile(data) {
  return request({
    url: '/user/profile',
    method: 'put',
    data,
  })
}
