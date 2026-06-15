import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useSessionStore } from '../stores/session'

export const http = axios.create({
  baseURL: '/api',
  timeout: 15000
})

http.interceptors.request.use((config) => {
  const session = useSessionStore()
  if (session.token) {
    config.headers.Authorization = `Bearer ${session.token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response.data.data,
  (error) => {
    const message = error.response?.data?.message || '请求失败，请稍后重试'
    ElMessage.error(message)
    if (error.response?.status === 401 || error.response?.data?.code === 401) {
      useSessionStore().logout()
    }
    return Promise.reject(error)
  }
)

export type ApiRecord = Record<string, any>
