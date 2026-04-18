import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { login as apiLogin, logout as apiLogout, getCurrentUser } from '@/api/auth'
import type { LoginResult } from '@/api/types'

interface UserInfo {
  id: number
  username: string
  name: string
  roleId: number
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const user = ref<UserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value)

  async function login(username: string, password: string) {
    const res = await apiLogin({ username, password })
    token.value = res.data.token
    user.value = res.data.user
    localStorage.setItem('token', token.value)
    localStorage.setItem('user', JSON.stringify(user.value))
  }

  async function logout() {
    try {
      await apiLogout()
    } catch {
      // 忽略登出错误
    }
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  async function fetchCurrentUser(): Promise<UserInfo | null> {
    if (!token.value) return null
    try {
      const res = await getCurrentUser()
      user.value = res.data
      localStorage.setItem('user', JSON.stringify(user.value))
      return user.value
    } catch {
      logout()
      return null
    }
  }

  function initFromStorage() {
    const savedUser = localStorage.getItem('user')
    if (savedUser) {
      try {
        user.value = JSON.parse(savedUser)
      } catch {
        user.value = null
      }
    }
  }

  return {
    token,
    user,
    isLoggedIn,
    login,
    logout,
    fetchCurrentUser,
    initFromStorage
  }
})
