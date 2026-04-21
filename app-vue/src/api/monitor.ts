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
    return request.get<MonitorMetrics>('/monitor/metrics')
  },
  getGoodsStock(goodsId: number) {
    return request.get<GoodsStockInfo>(`/monitor/stock/${goodsId}`)
  },
  health() {
    return request.get<{ status: string }>('/monitor/health')
  }
}
