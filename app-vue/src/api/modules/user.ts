/**
 * 用户管理相关 API
 * 提供用户的 CRUD 操作和状态管理
 */
import request from '../request'
import type {
  ApiResponse,
  CreateUserParams,
  PageResult,
  Role,
  UpdateUserParams,
  User,
  UserQueryParams,
  UserVO
} from '../types'

/**
 * 获取用户分页列表。
 *
 * @param params 查询参数（支持多条件筛选）
 * @return 用户分页列表
 */
export function getUserList(params: UserQueryParams) {
  return request.get<ApiResponse<PageResult<User>>>('/users', { params })
}

/**
 * 获取用户详情。
 *
 * @param id 用户 ID
 * @return 用户详情（包含角色名称）
 */
export function getUserDetail(id: number) {
  return request.get<ApiResponse<UserVO>>(`/users/${id}`)
}

/**
 * 创建用户。
 *
 * @param data 创建用户参数（密码由后端加密存储）
 * @return 创建结果
 */
export function createUser(data: CreateUserParams) {
  return request.post<ApiResponse<void>>('/users', data)
}

/**
 * 更新用户信息。
 *
 * @param id 用户 ID
 * @param data 更新参数（部分字段更新）
 * @return 更新结果
 */
export function updateUser(id: number, data: UpdateUserParams) {
  return request.put<ApiResponse<void>>(`/users/${id}`, data)
}

/**
 * 删除用户。
 *
 * @param id 用户 ID
 * @return 删除结果（逻辑删除）
 */
export function deleteUser(id: number) {
  return request.delete<ApiResponse<void>>(`/users/${id}`)
}

/**
 * 重置用户密码。
 *
 * @param id 用户 ID
 * @param password 新密码
 * @return 重置结果
 */
export function resetUserPassword(id: number, password: string) {
  return request.put<ApiResponse<void>>(`/users/${id}/password`, { password })
}

/**
 * 获取所有角色列表。
 *
 * @return 角色列表（用于用户编辑时的角色选择）
 */
export function getAllRoles() {
  return request.get<ApiResponse<Role[]>>('/users/roles/all')
}

/**
 * 更新用户状态。
 *
 * @param id 用户 ID
 * @param enabled 状态（1=启用，0=禁用）
 * @return 更新结果
 */
export function updateUserStatus(id: number, enabled: number) {
  return request.patch<ApiResponse<void>>(`/users/${id}/status`, { enabled })
}
