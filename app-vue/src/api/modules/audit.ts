import request from '../request'
import type { ApiResponse, AuditLog, AuditLogQueryParams, PageResult } from '../types'

export function getAuditLogs(params: AuditLogQueryParams) {
  return request.get<ApiResponse<PageResult<AuditLog>>>('/audit/logs', { params })
}
