/**
 * 活动管理 API 模块
 *
 * 模块说明：
 * 提供秒杀活动的 CRUD 操作接口
 *
 * API 端点前缀：/api/activity
 *
 * @module api/activity
 */
import request from './request'

/**
 * 活动实体接口
 */
export interface Activity {
  /** 活动 ID */
  id: number
  /** 活动名称 */
  name: string
  /** 活动描述 */
  description: string
  /** 开始时间 */
  startTime: string
  /** 结束时间 */
  endTime: string
  /** 活动状态：0-未开始 1-进行中 2-已结束 */
  status: number
  /** 每人限购数量 */
  perLimit: number
  /** 活动总库存 */
  totalStock?: number
}

/**
 * 活动 API 对象
 */
export const activityApi = {
  /**
   * 获取活动列表
   * @returns 秒杀活动列表
   */
  getActivity(id: number) {
    return request.get<Activity>(`/api/activity/${id}`)
  },

  /**
   * 开启活动
   * @param id - 活动 ID
   */
  startActivity(id: number) {
    return request.post<Boolean>(`/api/activity/${id}/start`)
  },

  /**
   * 结束活动
   * @param id - 活动 ID
   */
  endActivity(id: number) {
    return request.post<Boolean>(`/api/activity/${id}/end`)
  }
}
