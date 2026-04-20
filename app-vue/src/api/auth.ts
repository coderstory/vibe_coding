/**
 * 认证相关 API
 * 提供登录、登出、Token刷新等认证功能
 */
import request from './request'
import type { ApiResponse, LoginParams, LoginResult, RefreshTokenResult } from './types'

/**
 * 用户登录
 * 返回 token、refreshToken 和用户信息
 */
export function login(data: LoginParams) {
  return request.post<ApiResponse<LoginResult>>('/auth/login', data)
}

/**
 * 刷新 Access Token
 * 使用 refreshToken 获取新的访问令牌
 */
export function refreshToken(refreshToken: string) {
  return request.post<ApiResponse<RefreshTokenResult>>('/auth/refresh', { refreshToken })
}

/**
 * 用户登出
 * 通知服务端记录登出日志
 */
export function logout() {
  return request.post<ApiResponse<void>>('/auth/logout')
}

/**
 * 获取当前登录用户信息
 * 从服务端验证 token 并获取用户详情
 */
export function getCurrentUser() {
  return request.get<ApiResponse<{ id: number; username: string; name: string; roleId: number }>>('/auth/current')
}