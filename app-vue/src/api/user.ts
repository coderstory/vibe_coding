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

export function getUserList(params: UserQueryParams) {
  return request.get<ApiResponse<PageResult<User>>>('/users', { params })
}

export function getUserDetail(id: number) {
  return request.get<ApiResponse<UserVO>>(`/users/${id}`)
}

export function createUser(data: CreateUserParams) {
  return request.post<ApiResponse<void>>('/users', data)
}

export function updateUser(id: number, data: UpdateUserParams) {
  return request.put<ApiResponse<void>>(`/users/${id}`, data)
}

export function deleteUser(id: number) {
  return request.delete<ApiResponse<void>>(`/users/${id}`)
}

export function resetUserPassword(id: number, password: string) {
  return request.put<ApiResponse<void>>(`/users/${id}/password`, { password })
}

export function getAllRoles() {
  return request.get<ApiResponse<Role[]>>('/users/roles/all')
}

export function updateUserStatus(id: number, enabled: number) {
  return request.patch<ApiResponse<void>>(`/users/${id}/status`, { enabled })
}
