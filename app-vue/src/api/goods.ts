/**
 * 商品管理 API 模块
 *
 * 提供秒杀商品的 CRUD 操作接口
 *
 * @module api/goods
 */
import request from './request'

export interface SeckillGoods {
  id?: number
  activityId: number
  name: string
  originalPrice: number
  seckillPrice: number
  stock: number
  sold?: number
  imageUrl?: string
  createTime?: string
  updateTime?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}

export const goodsApi = {
  /**
   * 获取商品分页列表
   */
  getGoodsPage(page: number = 1, size: number = 20, activityId?: number) {
    const params: Record<string, any> = { page, size }
    if (activityId) {
      params.activityId = activityId
    }
    return request.get<PageResult<SeckillGoods>>('/goods', { params })
  },

  /**
   * 获取商品详情
   */
  getGoods(id: number) {
    return request.get<SeckillGoods>(`/goods/${id}`)
  },

  /**
   * 创建商品
   */
  createGoods(data: SeckillGoods) {
    return request.post<SeckillGoods>('/goods', data)
  },

  /**
   * 更新商品
   */
  updateGoods(id: number, data: SeckillGoods) {
    return request.put<SeckillGoods>(`/goods/${id}`, data)
  },

  /**
   * 删除商品
   */
  deleteGoods(id: number) {
    return request.delete<boolean>(`/goods/${id}`)
  },

  /**
   * 获取活动下的商品列表
   */
  getGoodsByActivity(activityId: number, page: number = 1, size: number = 20) {
    return request.get<PageResult<SeckillGoods>>(`/goods/activity/${activityId}`, {
      params: { page, size }
    })
  }
}
