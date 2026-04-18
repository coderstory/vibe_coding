import request from './request'
import type { ApiResponse, MenuTree, Menu } from './types'

export function getUserMenus(userId: number) {
  return request.get<ApiResponse<MenuTree[]>>(`/menus/user/${userId}`)
}

export function getMenuTree() {
  return request.get<ApiResponse<MenuTree[]>>('/menus/tree')
}

export function createMenu(data: Partial<Menu>) {
  return request.post<ApiResponse<Menu>>('/menus', data)
}

export function updateMenu(id: number, data: Partial<Menu>) {
  return request.put<ApiResponse<Menu>>(`/menus/${id}`, data)
}

export function deleteMenu(id: number) {
  return request.delete<ApiResponse<void>>(`/menus/${id}`)
}
