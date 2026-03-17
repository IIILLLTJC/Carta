import axios from 'axios'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 10000
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('cartaxi-token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const payload = response.data
    if (payload.code !== 200) {
      return Promise.reject(new Error(payload.message || '请求失败'))
    }
    return payload
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('cartaxi-token')
      localStorage.removeItem('cartaxi-role')
      localStorage.removeItem('cartaxi-display-name')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default request
