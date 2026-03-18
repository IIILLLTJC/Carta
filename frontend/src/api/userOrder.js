import request from '@/utils/request'

export function fetchUserOrderPage(params) {
  return request.get('/api/user/orders', { params })
}

export function fetchUserOrderDetail(id) {
  return request.get(`/api/user/orders/${id}`)
}

export function fetchUserOrderFormOptions() {
  return request.get('/api/user/orders/form-options')
}

export function createUserOrder(data) {
  return request.post('/api/user/orders', data)
}

export function mockPayUserOrder(id) {
  return request.post(`/api/user/orders/${id}/pay`)
}
