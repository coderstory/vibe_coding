import request from './request'

export interface MonitorMetrics {
  concurrentCount: number
  qpsKeys: number
  timestamp: number
}

export interface GoodsStockInfo {
  goodsId: number
  availableStock: number
}

export const monitorApi = {
  getMetrics() {
    return request.get<MonitorMetrics>('/api/monitor/metrics')
  },
  getGoodsStock(goodsId: number) {
    return request.get<GoodsStockInfo>(`/api/monitor/stock/${goodsId}`)
  },
  health() {
    return request.get<{ status: string }>('/api/monitor/health')
  }
}