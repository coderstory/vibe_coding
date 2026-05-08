import request from '../request'

export interface MonitorMetrics {
  concurrentCount: number;
  qpsKeys: number;
  timestamp: number;
}

export interface GoodsStockInfo {
  goodsId: number;
  availableStock: number;
}

export const monitorApi = {
  /**
   * 获取系统监控指标。
   *
   * @return 监控指标（包含并发数、QPS 和时间戳）
   */
  getMetrics() {
    return request.get<MonitorMetrics>('/monitor/metrics')
  },

  /**
   * 获取商品库存信息。
   *
   * @param goodsId 商品 ID
   * @return 商品库存信息
   */
  getGoodsStock(goodsId: number) {
    return request.get<GoodsStockInfo>(`/monitor/stock/${goodsId}`)
  },

  /**
   * 健康检查。
   *
   * @return 服务状态
   */
  health() {
    return request.get<{ status: string }>('/monitor/health')
  }
}
