import { defineStore } from 'pinia'
import request from '@/utils/request'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('cartaxi-token') || '',
    role: localStorage.getItem('cartaxi-role') || '',
    displayName: localStorage.getItem('cartaxi-display-name') || ''
  }),
  actions: {
    async login(payload) {
      const response = await request.post(payload.endpoint, payload.form)
      const data = response.data
      this.token = data.token
      this.role = data.role
      this.displayName = data.displayName
      localStorage.setItem('cartaxi-token', data.token)
      localStorage.setItem('cartaxi-role', data.role)
      localStorage.setItem('cartaxi-display-name', data.displayName)
      return data
    },
    setDisplayName(displayName) {
      this.displayName = displayName
      localStorage.setItem('cartaxi-display-name', displayName)
    },
    logout() {
      this.token = ''
      this.role = ''
      this.displayName = ''
      localStorage.removeItem('cartaxi-token')
      localStorage.removeItem('cartaxi-role')
      localStorage.removeItem('cartaxi-display-name')
    }
  }
})
