import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from './types'
import { refreshToken as apiRefreshToken } from '@/api/auth'

/**
 * 统一的 Axios 请求实例
 */
const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 标记是否正在刷新 token
let isRefreshing = false
// 重试队列
let refreshSubscribers: Array<(token: string) => void> = []

/**
 * 将 token 添加到重试队列
 */
function subscribeTokenRefresh(callback: (token: string) => void) {
  refreshSubscribers.push(callback)
}

/**
 * 通知所有订阅者 token 已刷新
 */
function onTokenRefreshed(token: string) {
  refreshSubscribers.forEach(callback => callback(token))
  refreshSubscribers = []
}

/**
 * 请求拦截器 - 添加 token
 */
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error: AxiosError) => {
    return Promise.reject(error)
  }
)

/**
 * 提取错误消息
 */
function extractErrorMessage(error: AxiosError): string {
  // 情况1: 后端返回的 JSON 格式错误
  if (error.response?.data) {
    const data = error.response.data as ApiResponse
    if (data.message) {
      return data.message
    }
    if ('msg' in data && data.msg) {
      return data.msg as string
    }
  }

  // 情况2: HTTP 状态码对应的默认消息
  const statusMessages: Record<number, string> = {
    400: '请求参数错误',
    401: '登录已过期，请重新登录',
    403: '没有权限执行此操作',
    404: '请求的资源不存在',
    408: '请求超时，请稍后重试',
    409: '数据冲突，请检查是否重复',
    500: '服务器内部错误，请稍后重试',
    502: '网关错误，请稍后重试',
    503: '服务暂时不可用，请稍后重试',
    504: '网关超时，请稍后重试'
  }

  if (error.response?.status && statusMessages[error.response.status]) {
    return statusMessages[error.response.status]
  }

  // 情况3: 网络错误
  if (error.code === 'ECONNABORTED') {
    return '请求超时，请检查网络或稍后重试'
  }

  if (error.code === 'ERR_NETWORK' || error.message === 'Network Error') {
    return '网络连接失败，请检查网络设置'
  }

  // 情况4: CORS 错误
  if (error.message?.includes('CORS')) {
    return '跨域请求被阻止，请联系管理员'
  }

  // 情况5: 无法解析的后端响应
  if (error.response?.status === 200 && !error.response.data) {
    return '服务器响应格式错误'
  }

  // 默认
  return error.message || '操作失败，请稍后重试'
}

/**
 * 尝试刷新 token
 */
async function tryRefreshToken(): Promise<string | null> {
  if (isRefreshing) {
    // 如果正在刷新，返回一个 Promise 等待刷新完成
    return new Promise((resolve) => {
      subscribeTokenRefresh((token: string) => {
        resolve(token)
      })
    })
  }

  isRefreshing = true
  const refreshToken = localStorage.getItem('refreshToken')

  if (!refreshToken) {
    isRefreshing = false
    return null
  }

  try {
    const res = await apiRefreshToken(refreshToken)
    const newToken = res.data.token
    const newRefreshToken = res.data.refreshToken
    localStorage.setItem('token', newToken)
    localStorage.setItem('refreshToken', newRefreshToken)
    onTokenRefreshed(newToken)
    isRefreshing = false
    return newToken
  } catch {
    // 刷新失败，清除 token 并跳转到登录页
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
    isRefreshing = false
    window.location.href = '/login'
    return null
  }
}

/**
 * 响应拦截器
 */
request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data
    if (res.code === 200) {
      return res
    } else {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  async (error: AxiosError<ApiResponse>) => {
    // 如果是 401 错误，尝试刷新 token
    if (error.response?.status === 401) {
      const originalRequest = error.config
      if (originalRequest && !originalRequest.url?.includes('/auth/')) {
        const newToken = await tryRefreshToken()
        if (newToken && originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${newToken}`
          return request(originalRequest)
        }
      }
    }

    const message = extractErrorMessage(error)
    ElMessage.error(message)
    return Promise.reject(new Error(message))
  }
)

export default request

// 重新导出类型供外部使用
export type { ApiResponse }
