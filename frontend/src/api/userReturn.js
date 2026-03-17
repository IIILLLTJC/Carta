import request from '../utils/request'

export function fetchUserReturnPage(params) {
  return request({
    url: '/user/returns',
    method: 'get',
    params,
  })
}

export function fetchUserReturnDetail(id) {
  return request({
    url: `/user/returns/${id}`,
    method: 'get',
  })
}

export function fetchUserReturnFormOptions() {
  return request({
    url: '/user/returns/form-options',
    method: 'get',
  })
}

export function createUserReturn(data) {
  return request({
    url: '/user/returns',
    method: 'post',
    data,
  })
}
