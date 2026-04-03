import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { login as apiLogin, logout as apiLogout, getCurrentUser } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(null)
  
  const isLoggedIn = computed(() => !!token.value)
  
  async function login(username, password) {
    const res = await apiLogin(username, password)
    token.value = res.data.token
    user.value = res.data.user
    localStorage.setItem('token', token.value)
    localStorage.setItem('user', JSON.stringify(user.value))
  }
  
  async function logout() {
    try {
      await apiLogout()
    } catch (e) {
      // 忽略登出错误
    }
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }
  
  async function fetchCurrentUser() {
    if (!token.value) return null
    try {
      const res = await getCurrentUser()
      user.value = res.data
      localStorage.setItem('user', JSON.stringify(user.value))
      return user.value
    } catch (e) {
      logout()
      return null
    }
  }
  
  function initFromStorage() {
    const savedUser = localStorage.getItem('user')
    if (savedUser) {
      try {
        user.value = JSON.parse(savedUser)
      } catch (e) {
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