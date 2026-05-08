/**
 * 商品管理 API 模块
 *
 * 提供秒杀商品的 CRUD 操作接口
 *
 * @module api/goods
 */
import request from '../request'

export interface SeckillGoods {
  id?: number;
  activityId: number;
  name: string;
  originalPrice: number;
  seckillPrice: number;
  stock: number;
  sold?: number;
  imageUrl?: string;
  createTime?: string;
  updateTime?: string;
}

export const goodsApi = {
  /**
   * 获取商品分页列表。
   *
   * @param page 页码（从 1 开始，默认 1）
   * @param size 每页数量（默认 20）
   * @param activityId 活动 ID（可选，按活动筛选）
   * @return 商品分页列表
   */
  getGoodsPage(page: number = 1, size: number = 20, activityId?: number) {
    const params: Record<string, any> = { page, size }
    if (activityId) {
      params.activityId = activityId
    }
    return request.get<PageResult<SeckillGoods>>('/goods', { params })
  },

  /**
   * 获取商品详情。
   *
   * @param id 商品 ID
   * @return 商品详情
   */
  getGoods(id: number) {
    return request.get<SeckillGoods>(`/goods/${id}`)
  },

  /**
   * 创建商品。
   *
   * @param data 商品参数
   * @return 创建后的商品
   */
  createGoods(data: SeckillGoods) {
    return request.post<SeckillGoods>('/goods', data)
  },

  /**
   * 更新商品。
   *
   * @param id 商品 ID
   * @param data 更新参数
   * @return 更新后的商品
   */
  updateGoods(id: number, data: SeckillGoods) {
    return request.put<SeckillGoods>(`/goods/${id}`, data)
  },

  /**
   * 删除商品。
   *
   * @param id 商品 ID
   * @return 删除结果
   */
  deleteGoods(id: number) {
    return request.delete<boolean>(`/goods/${id}`)
  }

}
