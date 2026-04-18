import request from './request'
import type { ApiResponse, LoginParams, LoginResult } from './types'

export function login(data: LoginParams) {
  return request.post<ApiResponse<LoginResult>>('/auth/login', data)
}

export function logout() {
  return request.post<ApiResponse<void>>('/auth/logout')
}

export function getCurrentUser() {
  return request.get<ApiResponse<{ id: number; username: string; name: string; roleId: number }>>('/auth/current')
}
