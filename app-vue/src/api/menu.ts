import request from './request'
import type { ApiResponse, MenuTree } from './types'

export function getUserMenus(userId: number) {
  return request.get<ApiResponse<MenuTree[]>>(`/menus/user/${userId}`)
}

export function getMenuTree() {
  return request.get<ApiResponse<MenuTree[]>>('/menus/tree')
}
