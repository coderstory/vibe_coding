import request from './request'

export function getAuditLogs(params) {
  return request({
    url: '/audit/logs',
    method: 'get',
    params
  })
}

export default request
