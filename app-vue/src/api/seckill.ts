/**
 * 秒杀 API 模块
 *
 * 提供秒杀相关的所有接口调用
 *
 * 功能说明：
 * 1. 抢购接口 - 执行秒杀下单
 * 2. 结果查询 - 查询秒杀处理结果
 * 3. 签名获取 - 获取秒杀资格签名
 * 4. SSE 订阅 - 实时接收秒杀结果通知
 *
 * @example
 * import { seckillApi } from '@/api/seckill'
 */
import request from './request'

/**
 * 秒杀请求参数
 */
export interface SeckillRequest {
  /** 商品ID */
  goodsId: number
  /** 活动ID */
  activityId: number
  /** 签名（防篡改） */
  sign?: string
  /** 时间戳 */
  timestamp?: number
  /** 幂等键 */
  idempotentKey?: string
}

/**
 * 秒杀响应结果
 */
export interface SeckillResponse {
  /** 队列ID（用于查询结果和 SSE 订阅） */
  queueId?: string
  /** 状态：0-排队中 1-成功 2-失败 */
  status: number
  /** 状态消息 */
  message: string
  /** 订单ID（成功时返回） */
  orderId?: number
}

/**
 * 秒杀 API 对象
 */
export const seckillApi = {
  /**
   * 执行秒杀抢购
   *
   * @description
   * 调用此接口执行秒杀下单请求
   * 返回排队中状态后，前端应建立 SSE 连接监听结果
   *
   * @param data 秒杀请求参数
   * @returns 抢购结果（包含队列ID用于 SSE 订阅）
   *
   * @example
   * ```typescript
   * const res = await seckillApi.buy({
   *   goodsId: 1,
   *   activityId: 1,
   *   sign: 'xxx',
   *   timestamp: Date.now(),
   *   idempotentKey: 'user1_goods1_xxx'
   * })
   *
   * if (res.code === 200) {
   *   // 建立 SSE 连接
   *   subscribeSeckillResult(res.data.queueId)
   * }
   * ```
   */
  buy(data: SeckillRequest) {
    return request.post<SeckillResponse>('/seckill/buy', data)
  },

  /**
   * 查询秒杀结果
   *
   * @description
   * 通过 queueId 查询秒杀处理结果
   * 也可以通过 SSE 实时接收结果
   *
   * @param queueId 队列ID
   * @returns 秒杀处理结果
   *
   * @example
   * ```typescript
   * const res = await seckillApi.getResult('queue-123')
   * if (res.data.status === 1) {
   *   alert('抢购成功，订单号：' + res.data.orderId)
   * }
   * ```
   */
  getResult(queueId: string) {
    return request.get<SeckillResponse>(`/seckill/result/${queueId}`)
  },

  /**
   * 获取秒杀签名
   *
   * @description
   * 在抢购前获取签名，用于验证请求合法性
   * 签名具有时效性（5分钟）
   *
   * @param goodsId 商品ID
   * @returns 签名信息 { sign, timestamp }
   *
   * @example
   * ```typescript
   * const res = await seckillApi.getSign(1)
   * if (res.code === 200) {
   *   return {
   *     sign: res.data.sign,
   *     timestamp: res.data.timestamp
   *   }
   * }
   * ```
   */
  getSign(goodsId: number) {
    return request.get<{ sign: string; timestamp: number }>(`/seckill/sign/${goodsId}`)
  },

  /**
   * 订阅秒杀结果通知（SSE）
   *
   * @description
   * 建立 SSE 连接，实时接收秒杀处理结果
   *
   * @param queueId 队列ID
   * @param onMessage 消息回调
   * @param onError 错误回调
   * @returns EventSource 对象（用于手动关闭连接）
   *
   * @example
   * ```typescript
   * // 建立 SSE 连接
   * const eventSource = seckillApi.subscribeSeckillResult('queue-123',
   *   (data) => {
   *     if (data.status === 1) {
   *       alert('抢购成功！')
   *     } else if (data.status === 2) {
   *       alert('抢购失败：' + data.message)
   *     }
   *   },
   *   (error) => {
   *     console.error('SSE 连接错误', error)
   *   }
   * )
   *
   * // 关闭连接
   * eventSource.close()
   * ```
   */
  subscribeSeckillResult(
    queueId: string,
    onMessage?: (data: SeckillResponse) => void,
    onError?: (error: Event) => void
  ): EventSource {
    const eventSource = new EventSource(`/seckill/subscribe/${queueId}`)

    // 连接成功
    eventSource.onopen = () => {
      console.log('SSE 连接已建立')
    }

    // 秒杀结果事件
    eventSource.addEventListener('seckill_result', (event) => {
      try {
        const data = JSON.parse(event.data) as SeckillResponse
        console.log('收到秒杀结果:', data)
        onMessage?.(data)
      } catch (error) {
        console.error('解析秒杀结果失败', error)
      }
    })

    // 状态更新事件
    eventSource.addEventListener('seckill_status', (event) => {
      try {
        const data = JSON.parse(event.data)
        console.log('状态更新:', data)
      } catch (error) {
        console.error('解析状态更新失败', error)
      }
    })

    // 心跳事件
    eventSource.addEventListener('heartbeat', (event) => {
      console.log('心跳:', event.data)
    })

    // 连接完成
    eventSource.addEventListener('completed', (event) => {
      console.log('SSE 连接完成:', event.data)
    })

    // 连接错误
    eventSource.onerror = (error) => {
      console.error('SSE 连接错误', error)
      onError?.(error)
    }

    return eventSource
  },

  /**
   * 获取商品当前库存
   *
   * @param goodsId 商品ID
   * @returns 库存数量
   */
  getStock(goodsId: number) {
    return request.get<{ stock: number }>(`/seckill/stock/${goodsId}`)
  }
}

/**
 * 活动 API 模块
 */
export interface Activity {
  id: number
  name: string
  description: string
  startTime: string
  endTime: string
  status: number
  perLimit: number
  totalStock: number
}

export const activityApi = {
  /**
   * 获取活动分页列表
   *
   * @param page 页码
   * @param size 每页数量
   * @returns 分页后的活动列表
   */
  list(page: number = 1, size: number = 20) {
    return request.get<{ records: Activity[]; total: number }>(`/seckill/activity`, { params: { page, size } })
  },

  /**
   * 获取活动详情
   *
   * @param id 活动ID
   * @returns 活动详情
   */
  get(id: number) {
    return request.get<Activity>(`/seckill/activity/${id}`)
  },

  /**
   * 创建活动
   *
   * @param data 活动参数
   * @returns 创建的活动
   */
  create(data: Partial<Activity>) {
    return request.post<Activity>('/seckill/activity', data)
  },

  /**
   * 更新活动
   *
   * @param id 活动ID
   * @param data 活动参数
   * @returns 更新后的活动
   */
  update(id: number, data: Partial<Activity>) {
    return request.put<Activity>(`/seckill/activity/${id}`, data)
  },

  /**
   * 删除活动
   *
   * @param id 活动ID
   * @returns 删除结果
   */
  delete(id: number) {
    return request.delete<boolean>(`/seckill/activity/${id}`)
  },

  /**
   * 发布活动
   *
   * @param id 活动ID
   * @returns 发布结果
   */
  publish(id: number) {
    return request.post<boolean>(`/seckill/activity/${id}/publish`)
  },

  /**
   * 开启活动
   *
   * @param id 活动ID
   */
  start(id: number) {
    return request.post<boolean>(`/activity/${id}/start`)
  },

  /**
   * 结束活动
   *
   * @param id 活动ID
   */
  end(id: number) {
    return request.post<boolean>(`/activity/${id}/end`)
  },

  /**
   * 预约活动
   *
   * @param activityId 活动ID
   */
  reserve(activityId: number) {
    return request.post<void>(`/activity/${activityId}/reserve`)
  },

  /**
   * 预热活动数据
   *
   * @param activityId 活动ID
   */
  preheat(activityId: number) {
    return request.post<any>(`/seckill/preheat/${activityId}`)
  }
}
