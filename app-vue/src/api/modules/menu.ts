import request from '../request'
import type { ApiResponse, Menu, MenuTree } from '../types'

/**
 * 获取完整菜单树。
 *
 * @return 菜单树列表
 */
export function getMenuTree() {
  return request.get<ApiResponse<MenuTree[]>>('/menus/tree')
}

/**
 * 创建菜单。
 *
 * @param data 菜单参数
 * @return 创建后的菜单
 */
export function createMenu(data: Partial<Menu>) {
  return request.post<ApiResponse<Menu>>('/menus', data)
}

/**
 * 更新菜单。
 *
 * @param id 菜单 ID
 * @param data 更新参数
 * @return 更新后的菜单
 */
export function updateMenu(id: number, data: Partial<Menu>) {
  return request.put<ApiResponse<Menu>>(`/menus/${id}`, data)
}

/**
 * 删除菜单。
 *
 * @param id 菜单 ID
 * @return 删除结果
 */
export function deleteMenu(id: number) {
  return request.delete<ApiResponse<void>>(`/menus/${id}`)
}
