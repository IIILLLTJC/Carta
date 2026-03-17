import request from '@/utils/request'

export function fetchUserReturnPage(params) {
  return request.get('/api/user/returns', { params })
}

export function fetchUserReturnDetail(id) {
  return request.get(`/api/user/returns/${id}`)
}

export function fetchUserReturnFormOptions() {
  return request.get('/api/user/returns/form-options')
}

export function createUserReturn(data) {
  return request.post('/api/user/returns', data)
}