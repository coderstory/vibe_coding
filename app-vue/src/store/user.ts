import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { login as apiLogin, logout as apiLogout, getCurrentUser, refreshToken as apiRefreshToken } from '@/api/auth'

interface UserInfo {
  id: number
  username: string
  name: string
  roleId: number
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const storedRefreshToken = ref<string>(localStorage.getItem('refreshToken') || '')
  const user = ref<UserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value)

  async function login(username: string, password: string) {
    const res = await apiLogin({ username, password })
    token.value = res.data.token
    storedRefreshToken.value = res.data.refreshToken
    user.value = res.data.user
    localStorage.setItem('token', token.value)
    localStorage.setItem('refreshToken', storedRefreshToken.value)
    localStorage.setItem('user', JSON.stringify(user.value))
  }

  async function refreshToken(): Promise<boolean> {
    if (!storedRefreshToken.value) return false
    try {
      const res = await apiRefreshToken(storedRefreshToken.value)
      token.value = res.data.token
      storedRefreshToken.value = res.data.refreshToken
      localStorage.setItem('token', token.value)
      localStorage.setItem('refreshToken', storedRefreshToken.value)
      return true
    } catch {
      return false
    }
  }

  async function logout() {
    try {
      await apiLogout()
    } catch {
      // 忽略登出错误
    }
    token.value = ''
    storedRefreshToken.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
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
    refreshToken,
    user,
    isLoggedIn,
    login,
    refreshToken,
    logout,
    fetchCurrentUser,
    initFromStorage
  }
})
