/**
 * 角色管理相关 API
 * 提供角色 CRUD 和权限分配操作
 */
import request from './request'
import type {
  ApiResponse,
  Role,
  PageResult,
  RoleQueryParams,
  CreateRoleParams,
  UpdateRoleParams
} from './types'

/**
 * 获取角色分页列表
 */
export function getRoleList(params: RoleQueryParams) {
  return request.get<ApiResponse<PageResult<Role>>>(`/roles`, { params })
}

/**
 * 获取角色详情
 */
export function getRoleDetail(id: number) {
  return request.get<ApiResponse<Role>>(`/roles/${id}`)
}

/**
 * 创建角色
 */
export function createRole(data: CreateRoleParams) {
  return request.post<ApiResponse<void>>(`/roles`, data)
}

/**
 * 更新角色
 */
export function updateRole(id: number, data: UpdateRoleParams) {
  return request.put<ApiResponse<void>>(`/roles/${id}`, data)
}

/**
 * 删除角色
 */
export function deleteRole(id: number) {
  return request.delete<ApiResponse<void>>(`/roles/${id}`)
}

/**
 * 获取角色的菜单权限
 * 返回该角色被授权的所有菜单 ID 列表
 */
export function getRoleMenus(roleId: number) {
  return request.get<ApiResponse<number[]>>(`/roles/${roleId}/menus`)
}

/**
 * 分配菜单权限
 * 全量替换：该角色的所有菜单权限将被新列表覆盖
 */
export function assignRoleMenus(roleId: number, menuIds: number[]) {
  return request.put<ApiResponse<void>>(`/roles/${roleId}/menus`, menuIds)
}

/**
 * 获取完整菜单树
 * 用于角色权限分配的菜单选择
 */
export function getMenuTree() {
  return request.get<ApiResponse<import('./types').MenuTree[]>>(`/menus/tree`)
}