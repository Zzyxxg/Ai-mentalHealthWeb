import axios from 'axios'

import { router } from '../router'
import { useAuthStore } from '../stores/auth'
import { pinia } from '../stores/pinia'

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000,
})

http.interceptors.request.use((config) => {
  const authStore = useAuthStore(pinia)
  if (authStore.token) {
    config.headers = config.headers ?? {}
    config.headers.Authorization = `Bearer ${authStore.token}`
  }
  return config
})

http.interceptors.response.use(
  (resp) => resp,
  async (err) => {
    const authStore = useAuthStore(pinia)
    const status = err?.response?.status
    if (status === 401) {
      authStore.logout()
      if (router.currentRoute.value.path !== '/login') await router.replace('/login')
    }
    if (status === 403) {
      if (router.currentRoute.value.path !== '/403') await router.replace('/403')
    }
    return Promise.reject(err)
  },
)
