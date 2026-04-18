import request from './request'

export function getUserMenus(userId) {
  return request({
    url: `/menus/user/${userId}`,
    method: 'get'
  })
}

export function getMenuTree() {
  return request({
    url: '/menus/tree',
    method: 'get'
  })
}

export default request
