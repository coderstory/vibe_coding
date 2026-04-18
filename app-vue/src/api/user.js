import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器 - 添加 token
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 提取错误消息
function extractErrorMessage(error) {
  // 情况1: 后端返回的 JSON 格式错误
  if (error.response && error.response.data) {
    const data = error.response.data
    if (data.message) {
      return data.message
    }
    if (data.msg) {
      return data.msg
    }
  }

  // 情况2: HTTP 状态码对应的默认消息
  const statusMessages = {
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

  if (error.response && statusMessages[error.response.status]) {
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
  if (error.message && error.message.includes('CORS')) {
    return '跨域请求被阻止，请联系管理员'
  }

  // 情况5: 无法解析的后端响应
  if (error.response && error.response.status === 200 && !error.response.data) {
    return '服务器响应格式错误'
  }

  // 默认
  return error.message || '操作失败，请稍后重试'
}

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res
    } else {
      // 显示错误消息
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  error => {
    const message = extractErrorMessage(error)
    // 显示错误消息
    ElMessage.error(message)
    return Promise.reject(new Error(message))
  }
)

export function getUserList(params) {
  return request({
    url: '/users',
    method: 'get',
    params
  })
}

export function getUserDetail(id) {
  return request({
    url: `/users/${id}`,
    method: 'get'
  })
}

export function createUser(data) {
  return request({
    url: '/users',
    method: 'post',
    data
  })
}

export function updateUser(id, data) {
  return request({
    url: `/users/${id}`,
    method: 'put',
    data
  })
}

export function deleteUser(id) {
  return request({
    url: `/users/${id}`,
    method: 'delete'
  })
}

export function resetUserPassword(id, password) {
  return request({
    url: `/users/${id}/password`,
    method: 'put',
    data: { password }
  })
}

export function getAllRoles() {
  return request({
    url: '/users/roles/all',
    method: 'get'
  })
}

export function updateUserStatus(id, enabled) {
  return request({
    url: `/users/${id}/status`,
    method: 'patch',
    data: { enabled }
  })
}

export default request
