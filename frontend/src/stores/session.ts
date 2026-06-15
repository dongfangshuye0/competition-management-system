import { defineStore } from 'pinia'
import { http, type ApiRecord } from '../api/http'

type LoginResult = {
  token: string
  user: ApiRecord
}

export const useSessionStore = defineStore('session', {
  state: () => ({
    token: localStorage.getItem('cms_token') || '',
    user: JSON.parse(localStorage.getItem('cms_user') || 'null') as ApiRecord | null
  }),
  getters: {
    role: (state) => state.user?.role || '',
    isLoggedIn: (state) => Boolean(state.token && state.user)
  },
  actions: {
    async login(username: string, password: string) {
      const data = await http.post<any, LoginResult>('/auth/login', { username, password })
      this.token = data.token
      this.user = data.user
      localStorage.setItem('cms_token', data.token)
      localStorage.setItem('cms_user', JSON.stringify(data.user))
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('cms_token')
      localStorage.removeItem('cms_user')
    },
    async refreshMe() {
      if (!this.token) return
      this.user = await http.get<any, ApiRecord>('/users/me')
      localStorage.setItem('cms_user', JSON.stringify(this.user))
    }
  }
})
