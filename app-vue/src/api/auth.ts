import request from './request'
import type { ApiResponse, LoginParams, LoginResult, RefreshTokenResult } from './types'

export function login(data: LoginParams) {
  return request.post<ApiResponse<LoginResult>>('/auth/login', data)
}

export function refreshToken(refreshToken: string) {
  return request.post<ApiResponse<RefreshTokenResult>>('/auth/refresh', { refreshToken })
}

export function logout() {
  return request.post<ApiResponse<void>>('/auth/logout')
}

export function getCurrentUser() {
  return request.get<ApiResponse<{ id: number; username: string; name: string; roleId: number }>>('/auth/current')
}
