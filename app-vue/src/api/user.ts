/**
 * 用户管理相关 API
 * 提供用户的 CRUD 操作和状态管理
 */
import request from './request'
import type {
  ApiResponse,
  User,
  UserVO,
  Role,
  PageResult,
  UserQueryParams,
  CreateUserParams,
  UpdateUserParams
} from './types'

/**
 * 获取用户分页列表
 * 支持多条件筛选查询
 */
export function getUserList(params: UserQueryParams) {
  return request.get<ApiResponse<PageResult<User>>>(`/users`, { params })
}

/**
 * 获取用户详情
 * 返回包含角色名称的用户信息
 */
export function getUserDetail(id: number) {
  return request.get<ApiResponse<UserVO>>(`/users/${id}`)
}

/**
 * 创建用户
 * 密码需要单独传入，由后端加密存储
 */
export function createUser(data: CreateUserParams) {
  return request.post<ApiResponse<void>>(`/users`, data)
}

/**
 * 更新用户信息
 * 部分字段更新，ID 从路径参数获取
 */
export function updateUser(id: number, data: UpdateUserParams) {
  return request.put<ApiResponse<void>>(`/users/${id}`, data)
}

/**
 * 删除用户
 * 使用逻辑删除
 */
export function deleteUser(id: number) {
  return request.delete<ApiResponse<void>>(`/users/${id}`)
}

/**
 * 重置用户密码
 * 新密码由调用方生成，服务端仅加密存储
 */
export function resetUserPassword(id: number, password: string) {
  return request.put<ApiResponse<void>>(`/users/${id}/password`, { password })
}

/**
 * 获取所有角色列表
 * 用于用户编辑时的角色选择
 */
export function getAllRoles() {
  return request.get<ApiResponse<Role[]>>(`/users/roles/all`)
}

/**
 * 更新用户状态
 * enabled: 1=启用, 0=禁用
 */
export function updateUserStatus(id: number, enabled: number) {
  return request.patch<ApiResponse<void>>(`/users/${id}/status`, { enabled })
}