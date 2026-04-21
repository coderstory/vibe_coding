/**
 * 秒杀 API 模块
 *
 * 提供秒杀活动相关的所有接口调用封装
 *
 * 接口列表：
 * - seckillApi.buy()           - 执行秒杀抢购
 * - seckillApi.getResult()     - 查询秒杀结果
 * - seckillApi.getSign()       - 获取秒杀签名
 * - seckillApi.subscribeSeckillResult() - SSE 订阅秒杀结果
 * - seckillApi.getStock()      - 获取活动库存
 *
 * - activityApi.list()         - 获取活动列表
 * - activityApi.get()          - 获取活动详情
 * - activityApi.create()       - 创建活动
 * - activityApi.update()        - 更新活动
 * - activityApi.delete()        - 删除活动
 * - activityApi.publish()       - 发布活动
 * - activityApi.start()        - 开启活动
 * - activityApi.end()          - 结束活动
 * - activityApi.reserve()      - 预约活动
 * - activityApi.preheat()      - 预热活动
 *
 * 使用示例：
 * ```typescript
 * import { seckillApi, activityApi } from '@/api/seckill'
 *
 * // 获取活动详情
 * const res = await activityApi.get(1)
 * console.log(res.data) // Activity 类型
 *
 * // 执行抢购
 * const buyRes = await seckillApi.buy({
 *   activityId: 1,
 *   goodsId: 1,
 *   idempotentKey: 'xxx'
 * })
 * ```
 */
import request from './request'

// ==================== 秒杀相关接口 ====================

/**
 * 秒杀请求参数
 *
 * @property goodsId - 商品ID
 * @property activityId - 活动ID
 * @property sign - 签名（可选，用于后端验证请求合法性）
 * @property timestamp - 时间戳（配合签名使用）
 * @property idempotentKey - 幂等键（防止重复提交）
 */
export interface SeckillRequest {
  /** 商品ID */
  goodsId: number
  /** 活动ID */
  activityId: number
  /** 签名（防篡改，后端用于验证请求合法性） */
  sign?: string
  /** 时间戳（签名的一部分，用于验证签名是否过期） */
  timestamp?: number
  /** 幂等键（格式: user_{userId}_{activityId}_{timestamp}） */
  idempotentKey?: string
}

/**
 * 秒杀响应结果
 *
 * @property queueId - 队列ID（用于 SSE 订阅和结果查询）
 * @property status - 状态码（0=排队中, 1=成功, 2=失败）
 * @property message - 状态消息
 * @property orderId - 订单ID（成功时返回）
 */
export interface SeckillResponse {
  /** 队列ID（用于查询结果和 SSE 订阅） */
  queueId?: string
  /** 状态：0-排队中 1-成功 2-失败 */
  status: number
  /** 状态消息（失败时返回错误原因） */
  message: string
  /** 订单ID（成功时返回） */
  orderId?: number
}

/**
 * 秒杀 API 对象
 *
 * 提供抢购接口、结果查询、签名获取、SSE 订阅等功能
 */
export const seckillApi = {
  /**
   * 执行秒杀抢购
   *
   * 业务流程：
   * 1. 前端生成幂等键，调用此接口
   * 2. 后端将请求入队，返回 queueId
   * 3. 前端建立 SSE 连接，监听处理结果
   * 4. 后端异步处理，返回成功/失败通知
   *
   * 幂等设计：
   * - 前端生成唯一的 idempotentKey
   * - 后端用这个键做去重，防止重复扣减库存
   *
   * @param data 秒杀请求参数
   * @returns 抢购结果（包含队列ID用于 SSE 订阅）
   *
   * @example
   * ```typescript
   * const res = await seckillApi.buy({
   *   goodsId: 1,
   *   activityId: 1,
   *   sign: 'xxx',        // 可选
   *   timestamp: Date.now(), // 可选
   *   idempotentKey: 'user_123_1_1703001234567'
   * })
   *
   * if (res.code === 200 && res.data.queueId) {
   *   // 建立 SSE 连接监听结果
   *   subscribeSeckillResult(res.data.queueId)
   * }
   * ```
   */
  buy(data: SeckillRequest) {
    return request.post<SeckillResponse>('/seckill/buy', data)
  },

  /**
   * 查询秒杀结果（轮询方式）
   *
   * 适用场景：
   * - SSE 连接不可用时（如某些不支持 SSE 的环境）
   * - 作为 SSE 的降级方案
   *
   * 注意：推荐使用 subscribeSeckillResult() 实时性更好
   *
   * @param queueId 队列ID（从 buy 接口返回）
   * @returns 秒杀处理结果
   *
   * @example
   * ```typescript
   * const res = await seckillApi.getResult('queue-123')
   * if (res.data.status === 1) {
   *   console.log('抢购成功，订单号：', res.data.orderId)
   * }
   * ```
   */
  getResult(queueId: string) {
    return request.get<SeckillResponse>(`/seckill/result/${queueId}`)
  },

  /**
   * 获取秒杀签名
   *
   * 签名机制：
   * - 签名 = HMAC-SHA256(content, activitySignKey)
   * - content = userId:goodsId:timestamp
   * - 签名有效期：5分钟
   *
   * 作用：
   * - 防止请求被篡改
   * - 防止请求过期后被重放
   *
   * @param goodsId 商品ID
   * @returns 签名信息 { sign: 签名, timestamp: 时间戳 }
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
   * 订阅秒杀结果通知（SSE - Server-Sent Events）
   *
   * 为什么用 SSE？
   * - 实时性：服务端推送，无需轮询
   * - 轻量：基于 HTTP，不需要 WebSocket
   * - 可靠：自动重连，断线后会自动恢复
   *
   * SSE 事件类型：
   * - seckill_result: 最终结果（status=1成功 或 status=2失败）
   * - seckill_status: 状态更新（如 status=0 排队中）
   * - heartbeat: 心跳（保持连接活跃）
   * - completed: 连接正常关闭
   *
   * @param queueId 队列ID（从 buy 接口返回）
   * @param onMessage 消息回调（处理秒杀结果）
   * @param onError 错误回调（处理连接异常）
   * @returns EventSource 实例（用于手动关闭连接）
   *
   * @example
   * ```typescript
   * const eventSource = seckillApi.subscribeSeckillResult(
   *   'queue-123',
   *   // 消息回调
   *   (data) => {
   *     if (data.status === 1) {
   *       alert('恭喜！抢购成功！')
   *       router.push('/order/confirm')
   *     } else if (data.status === 2) {
   *       alert('抢购失败：' + data.message)
   *     }
   *   },
   *   // 错误回调
   *   (error) => {
   *     console.error('SSE 连接错误', error)
   *     alert('连接中断，请刷新页面')
   *   }
   * )
   *
   * // 组件卸载时关闭连接
   * eventSource.close()
   * ```
   */
  subscribeSeckillResult(
    queueId: string,
    onMessage?: (data: SeckillResponse) => void,
    onError?: (error: Event) => void
  ): EventSource {
    // 建立 SSE 连接
    // 路径格式: /api/seckill/subscribe/{queueId}
    const eventSource = new EventSource(`/seckill/subscribe/${queueId}`)

    // 连接成功
    eventSource.onopen = () => {
      console.log('SSE 连接已建立')
    }

    // 秒杀结果事件（最终结果）
    eventSource.addEventListener('seckill_result', (event) => {
      try {
        const data = JSON.parse(event.data) as SeckillResponse
        console.log('收到秒杀结果:', data)
        onMessage?.(data)
      } catch (error) {
        console.error('解析秒杀结果失败', error)
      }
    })

    // 状态更新事件（处理中）
    eventSource.addEventListener('seckill_status', (event) => {
      try {
        const data = JSON.parse(event.data)
        console.log('状态更新:', data)
        // 可以在这里更新页面上的状态显示
      } catch (error) {
        console.error('解析状态更新失败', error)
      }
    })

    // 心跳事件（保持连接）
    eventSource.addEventListener('heartbeat', (event) => {
      console.log('心跳:', event.data)
    })

    // 连接完成事件
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
   * 获取活动总库存
   *
   * 库存数据来源：
   * - 后端从 Redis 读取（预热时写入）
   * - 不是从数据库读取，保证高并发下的性能
   *
   * @param activityId 活动ID
   * @returns 库存数量（直接是数字，不是对象）
   *
   * @example
   * ```typescript
   * const res = await seckillApi.getStock(1)
   * if (res.code === 200) {
   *   console.log('当前库存:', res.data) // number 类型
   * }
   * ```
   */
  getStock(activityId: number) {
    return request.get<number>(`/seckill/activity/${activityId}/stock`)
  }
}

// ==================== 活动管理接口 ====================

/**
 * 商品数据结构
 *
 * 与后端 SeckillGoods 实体对应
 */
export interface Goods {
  /** 商品ID（自增） */
  id: number
  /** 关联的活动ID */
  activityId: number
  /** 商品名称 */
  name: string
  /** 商品原价 */
  originalPrice: number
  /** 秒杀价格 */
  seckillPrice: number
  /** 库存数量 */
  stock: number
  /** 已售数量 */
  sold: number
  /** 商品图片URL */
  imageUrl: string
}

/**
 * 活动详情数据结构（包含商品列表）
 *
 * 用于秒杀详情页，显示活动信息及关联的所有商品
 */
export interface ActivityDetail {
  /** 活动ID */
  id: number
  /** 活动名称 */
  name: string
  /** 活动描述 */
  description: string
  /** 开始时间 */
  startTime: string
  /** 结束时间 */
  endTime: string
  /** 活动状态（0=未开始, 1=进行中, 2=已结束） */
  status: number
  /** 每人限购数量 */
  perLimit: number
  /** 是否启用验证码 */
  enableCaptcha: boolean
  /** 是否启用IP限制 */
  enableIpLimit: boolean
  /** 签名密钥 */
  signKey: string
  /** 创建时间 */
  createTime: string
  /** 更新时间 */
  updateTime: string
  /** 关联的商品列表 */
  goodsList: Goods[]
}

/**
 * 活动数据结构
 *
 * 与后端 SeckillActivity 实体对应
 */
export interface Activity {
  /** 活动ID（自增） */
  id: number
  /** 活动名称 */
  name: string
  /** 活动描述 */
  description: string
  /** 开始时间（ISO 格式） */
  startTime: string
  /** 结束时间（ISO 格式） */
  endTime: string
  /** 活动状态（0=未开始, 1=进行中, 2=已结束） */
  status: number
  /** 每人限购数量 */
  perLimit: number
  /** 总库存（预留字段） */
  totalStock: number
}

/**
 * 预约记录数据结构
 *
 * 与后端 SeckillReservation 实体对应
 */
export interface Reservation {
  /** 预约ID */
  id: number
  /** 用户ID */
  userId: number
  /** 活动ID */
  activityId: number
  /** 预约时间 */
  reserveTime: string
  /** 状态（0=预约中, 1=已提醒, 2=已过期） */
  status: number
  /** 是否已发送提醒 */
  notified: boolean
  /** 提醒时间 */
  notifyTime: string | null
}

/**
 * 活动 API 对象
 *
 * 提供活动的 CRUD、发布、预热等功能
 */
export const activityApi = {
  /**
   * 获取活动分页列表
   *
   * @param page 页码（从1开始）
   * @param size 每页数量
   * @returns 分页结果 { records: Activity[], total: number }
   *
   * @example
   * ```typescript
   * const res = await activityApi.list(1, 20)
   * console.log(res.data.records) // Activity[]
   * console.log(res.data.total)  // 总数
   * ```
   */
  list(page: number = 1, size: number = 20) {
    return request.get<{ records: Activity[]; total: number }>(`/seckill/activity`, { params: { page, size } })
  },

  /**
   * 获取活动详情
   *
   * 数据来源：
   * - 优先从 Redis 读取（已预热的活动）
   * - 缓存不存在时从数据库读取并回填
   *
   * @param id 活动ID
   * @returns 活动详情
   *
   * @example
   * ```typescript
   * const res = await activityApi.get(1)
   * if (res.code === 200) {
   *   console.log(res.data.name)  // 活动名称
   *   console.log(res.data.status) // 活动状态
   * }
   * ```
   */
  get(id: number) {
    return request.get<Activity>(`/seckill/activity/${id}`)
  },

  /**
   * 获取活动详情（包含商品列表）
   *
   * 用于秒杀详情页，返回活动信息及关联的所有商品
   * 用户可以选择要抢购的商品
   *
   * @param id 活动ID
   * @returns 活动详情（含商品列表）
   *
   * @example
   * ```typescript
   * const res = await activityApi.getDetail(1)
   * if (res.code === 200) {
   *   console.log(res.data.name)  // 活动名称
   *   console.log(res.data.goodsList) // 商品列表
   * }
   * ```
   */
  getDetail(id: number) {
    return request.get<ActivityDetail>(`/seckill/activity/${id}/detail`)
  },

  /**
   * 创建活动
   *
   * @param data 活动参数（部分字段可选）
   * @returns 创建后的活动（含自动生成的ID）
   *
   * @example
   * ```typescript
   * const res = await activityApi.create({
   *   name: '限时秒杀',
   *   description: '全场5折',
   *   startTime: '2024-01-15T10:00:00',
   *   endTime: '2024-01-15T12:00:00',
   *   perLimit: 1
   * })
   * ```
   */
  create(data: Partial<Activity>) {
    return request.post<Activity>('/seckill/activity', data)
  },

  /**
   * 更新活动
   *
   * @param id 活动ID
   * @param data 更新后的活动数据
   * @returns 更新后的活动
   */
  update(id: number, data: Partial<Activity>) {
    return request.put<Activity>(`/seckill/activity/${id}`, data)
  },

  /**
   * 删除活动
   *
   * @param id 活动ID
   * @returns 删除结果（true=成功）
   */
  delete(id: number) {
    return request.delete<boolean>(`/seckill/activity/${id}`)
  },

  /**
   * 发布活动
   *
   * 发布流程：
   * 1. 检查活动是否已关联商品
   * 2. 更新活动状态为"进行中"
   * 3. 触发预热，将数据和库存写入 Redis
   *
   * @param id 活动ID
   * @returns 发布结果（true=成功）
   *
   * @example
   * ```typescript
   * const res = await activityApi.publish(1)
   * if (res.code === 200) {
   *   console.log('发布成功，已预热到 Redis')
   * }
   * ```
   */
  publish(id: number) {
    return request.post<boolean>(`/seckill/activity/${id}/publish`)
  },

  /**
   * 开启活动（手动开始）
   *
   * @param id 活动ID
   */
  start(id: number) {
    return request.post<boolean>(`/activity/${id}/start`)
  },

  /**
   * 结束活动（手动结束）
   *
   * @param id 活动ID
   */
  end(id: number) {
    return request.post<boolean>(`/activity/${id}/end`)
  },

  /**
   * 预约活动
   *
   * 用户预约后，活动开始前会收到通知
   * 预约信息存储在 Redis Set 中
   *
   * @param activityId 活动ID
   */
  reserve(activityId: number) {
    return request.post<void>(`/reservation/${activityId}`)
  },

  /**
   * 获取我的预约列表
   *
   * @returns 预约列表
   */
  getMyReservations() {
    return request.get<Reservation[]>('/reservation/my')
  },

  /**
   * 预热活动数据（手动触发）
   *
   * 通常不需要手动调用，发布时会自动预热
   * 用于管理后台手动刷新缓存
   *
   * @param activityId 活动ID
   */
  preheat(activityId: number) {
    return request.post<any>(`/seckill/preheat/${activityId}`)
  }
}
