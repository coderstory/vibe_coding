import request from './request'
import type {
  ApiResponse,
  Role,
  PageResult,
  RoleQueryParams,
  CreateRoleParams,
  UpdateRoleParams
} from './types'

export function getRoleList(params: RoleQueryParams) {
  return request.get<ApiResponse<PageResult<Role>>>('/roles', { params })
}

export function getRoleDetail(id: number) {
  return request.get<ApiResponse<Role>>(`/roles/${id}`)
}

export function createRole(data: CreateRoleParams) {
  return request.post<ApiResponse<void>>('/roles', data)
}

export function updateRole(id: number, data: UpdateRoleParams) {
  return request.put<ApiResponse<void>>(`/roles/${id}`, data)
}

export function deleteRole(id: number) {
  return request.delete<ApiResponse<void>>(`/roles/${id}`)
}

export function getRoleMenus(roleId: number) {
  return request.get<ApiResponse<number[]>>(`/roles/${roleId}/menus`)
}

export function assignRoleMenus(roleId: number, menuIds: number[]) {
  return request.put<ApiResponse<void>>(`/roles/${roleId}/menus`, menuIds)
}

export function getMenuTree() {
  return request.get<ApiResponse<import('./types').MenuTree[]>>('/menus/tree')
}
