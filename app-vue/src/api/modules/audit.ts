import request from '../request'
import type { ApiResponse, AuditLog, AuditLogQueryParams, PageResult } from '../types'

/**
 * 获取审计日志分页列表。
 *
 * @param params 查询参数（支持多条件筛选）
 * @return 审计日志分页列表
 */
export function getAuditLogs(params: AuditLogQueryParams) {
  return request.get<ApiResponse<PageResult<AuditLog>>>('/audit/logs', { params })
}
