import axios from 'axios'

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

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res
    } else {
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  error => {
    return Promise.reject(error)
  }
)

export function getRoleList(params) {
  return request({
    url: '/roles',
    method: 'get',
    params
  })
}

export function getRoleDetail(id) {
  return request({
    url: `/roles/${id}`,
    method: 'get'
  })
}

export function createRole(data) {
  return request({
    url: '/roles',
    method: 'post',
    data
  })
}

export function updateRole(id, data) {
  return request({
    url: `/roles/${id}`,
    method: 'put',
    data
  })
}

export function deleteRole(id) {
  return request({
    url: `/roles/${id}`,
    method: 'delete'
  })
}

export function getRoleMenus(roleId) {
  return request({
    url: `/roles/${roleId}/menus`,
    method: 'get'
  })
}

export function assignRoleMenus(roleId, menuIds) {
  return request({
    url: `/roles/${roleId}/menus`,
    method: 'put',
    data: menuIds
  })
}

export function getMenuTree() {
  return request({
    url: '/menus/tree',
    method: 'get'
  })
}

export default request
