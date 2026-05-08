/**
 * 认证相关 API
 * 提供登录、登出、Token刷新等认证功能
 */
import request from '../request'
import type { ApiResponse, LoginParams, LoginResult, RefreshTokenResult } from '../types'

/**
 * 用户登录。
 *
 * @param data 登录参数
 * @return 登录结果（包含 token、refreshToken 和用户信息）
 */
export function login(data: LoginParams) {
  return request.post<ApiResponse<LoginResult>>('/auth/login', data)
}

/**
 * 刷新 Access Token。
 *
 * @param refreshToken 刷新令牌
 * @return 新的令牌结果
 */
export function refreshToken(refreshToken: string) {
  return request.post<ApiResponse<RefreshTokenResult>>('/auth/refresh', { refreshToken })
}

/**
 * 用户登出。
 *
 * @return 登出结果
 */
export function logout() {
  return request.post<ApiResponse<void>>('/auth/logout')
}

/**
 * 获取当前登录用户信息。
 *
 * @return 当前登录用户详情（包含 ID、用户名、姓名和角色 ID）
 */
export function getCurrentUser() {
  return request.get<ApiResponse<{ id: number; username: string; name: string; roleId: number }>>('/auth/current')
}
