/**
 * 角色管理相关 API
 * 提供角色 CRUD 和权限分配操作
 */
import request from '../request'
import type { ApiResponse, CreateRoleParams, PageResult, Role, RoleQueryParams, UpdateRoleParams } from '../types'

/**
 * 获取角色分页列表。
 *
 * @param params 查询参数
 * @return 角色分页列表
 */
export function getRoleList(params: RoleQueryParams) {
  return request.get<ApiResponse<PageResult<Role>>>('/roles', { params })
}

/**
 * 创建角色。
 *
 * @param data 创建角色参数
 * @return 创建结果
 */
export function createRole(data: CreateRoleParams) {
  return request.post<ApiResponse<void>>('/roles', data)
}

/**
 * 更新角色。
 *
 * @param id 角色 ID
 * @param data 更新参数
 * @return 更新结果
 */
export function updateRole(id: number, data: UpdateRoleParams) {
  return request.put<ApiResponse<void>>(`/roles/${id}`, data)
}

/**
 * 删除角色。
 *
 * @param id 角色 ID
 * @return 删除结果
 */
export function deleteRole(id: number) {
  return request.delete<ApiResponse<void>>(`/roles/${id}`)
}

/**
 * 获取角色的菜单权限。
 *
 * @param roleId 角色 ID
 * @return 该角色被授权的菜单 ID 列表
 */
export function getRoleMenus(roleId: number) {
  return request.get<ApiResponse<number[]>>(`/roles/${roleId}/menus`)
}

/**
 * 分配菜单权限。
 *
 * @param roleId 角色 ID
 * @param menuIds 菜单 ID 列表（全量替换）
 * @return 分配结果
 */
export function assignRoleMenus(roleId: number, menuIds: number[]) {
  return request.put<ApiResponse<void>>(`/roles/${roleId}/menus`, menuIds)
}

/**
 * 获取完整菜单树。
 *
 * @return 菜单树列表（用于角色权限分配的菜单选择）
 */
export function getMenuTree() {
  return request.get<ApiResponse<import('./types').MenuTree[]>>('/menus/tree')
}
