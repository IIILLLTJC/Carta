import request from '@/utils/request'

export function fetchOrderPage(params) {
  return request.get('/api/admin/orders', { params })
}

export function fetchOrderDetail(id) {
  return request.get(`/api/admin/orders/${id}`)
}

export function createOrder(data) {
  return request.post('/api/admin/orders', data)
}

export function updateOrder(id, data) {
  return request.put(`/api/admin/orders/${id}`, data)
}

export function deleteOrder(id) {
  return request.delete(`/api/admin/orders/${id}`)
}

export function updateOrderStatus(id, status) {
  return request.put(`/api/admin/orders/${id}/status`, { status })
}

export function fetchOrderFormOptions() {
  return request.get('/api/admin/orders/form-options')
}