import request from '@/utils/request'

export function fetchCarPage(params) {
  return request.get('/api/admin/cars', { params })
}

export function fetchCarDetail(id) {
  return request.get(`/api/admin/cars/${id}`)
}

export function createCar(data) {
  return request.post('/api/admin/cars', data)
}

export function updateCar(id, data) {
  return request.put(`/api/admin/cars/${id}`, data)
}

export function deleteCar(id) {
  return request.delete(`/api/admin/cars/${id}`)
}

export function updateCarStatus(id, status) {
  return request.put(`/api/admin/cars/${id}/status`, { status })
}