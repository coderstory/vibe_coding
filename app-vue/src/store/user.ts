/**
 * 用户状态管理
 * 管理登录状态、Token、用户信息等全局状态
 */
import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import {
  getCurrentUser,
  login as apiLogin,
  logout as apiLogout,
  refreshToken as apiRefreshToken
} from '@/api/modules/auth'

/**
 * 用户信息结构
 */
interface UserInfo {
  id: number;
  username: string;
  name: string;
  roleId: number;
}

/**
 * 用户状态管理
 * 使用 Pinia Composition API 风格
 */
export const useUserStore = defineStore('user', () => {
  // 从 localStorage 恢复登录状态
  const token = ref<string>(localStorage.getItem('token') || '')
  const storedRefreshToken = ref<string>(localStorage.getItem('refreshToken') || '')
  const user = ref<UserInfo | null>(null)

  // 计算属性：是否已登录
  const isLoggedIn = computed(() => !!token.value)

  /**
   * 用户登录
   * 保存 Token 和用户信息到 localStorage
   * @param username 用户名
   * @param password 密码
   */
  async function login(username: string, password: string) {
    const res = await apiLogin({ username, password })
    token.value = res.data.token
    storedRefreshToken.value = res.data.refreshToken
    user.value = res.data.user
    localStorage.setItem('token', token.value)
    localStorage.setItem('refreshToken', storedRefreshToken.value)
    localStorage.setItem('user', JSON.stringify(user.value))
  }

  /**
   * 刷新 Token
   * 使用 refreshToken 获取新的访问令牌
   * @return 是否刷新成功
   */
  async function refreshToken(): Promise<boolean> {
    if (!storedRefreshToken.value) return false
    try {
      const res = await apiRefreshToken(storedRefreshToken.value)
      token.value = res.data.token
      storedRefreshToken.value = res.data.refreshToken
      localStorage.setItem('token', token.value)
      localStorage.setItem('refreshToken', storedRefreshToken.value)
      return true
    }
    catch {
      return false
    }
  }

  /**
   * 用户登出
   * 清除本地状态，忽略服务端错误
   */
  async function logout() {
    try {
      await apiLogout()
    }
    catch {
      // 忽略登出错误，确保本地状态被清除
    }
    token.value = ''
    storedRefreshToken.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
  }

  /**
   * 获取当前用户信息
   * 失败时自动登出
   * @return 当前用户信息（获取失败返回 null）
   */
  async function fetchCurrentUser(): Promise<UserInfo | null> {
    if (!token.value) return null
    try {
      const res = await getCurrentUser()
      user.value = res.data
      localStorage.setItem('user', JSON.stringify(user.value))
      return user.value
    }
    catch {
      logout()
      return null
    }
  }

  /**
   * 从 localStorage 初始化用户状态
   */
  function initFromStorage() {
    const savedUser = localStorage.getItem('user')
    if (savedUser) {
      try {
        user.value = JSON.parse(savedUser)
      }
      catch {
        user.value = null
      }
    }
  }

  return {
    token,
    user,
    isLoggedIn,
    login,
    refreshToken,
    logout,
    fetchCurrentUser,
    initFromStorage
  }
})
