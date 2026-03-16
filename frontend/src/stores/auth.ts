import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

import type { UserMe, UserRole } from '../types/auth'

const TOKEN_KEY = 'mh_token'
const ROLE_KEY = 'mh_role'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) || '')
  const role = ref<UserRole | ''>((localStorage.getItem(ROLE_KEY) as UserRole) || '')
  const me = ref<UserMe | null>(null)

  const isAuthed = computed(() => Boolean(token.value))

  function setToken(nextToken: string) {
    token.value = nextToken
    localStorage.setItem(TOKEN_KEY, nextToken)
  }

  function setRole(nextRole: UserRole) {
    role.value = nextRole
    localStorage.setItem(ROLE_KEY, nextRole)
  }

  function setMe(nextMe: UserMe | null) {
    me.value = nextMe
    if (nextMe?.role) setRole(nextMe.role)
  }

  function logout() {
    token.value = ''
    role.value = ''
    me.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(ROLE_KEY)
  }

  return {
    token,
    role,
    me,
    isAuthed,
    setToken,
    setRole,
    setMe,
    logout,
  }
})
