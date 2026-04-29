/**
 * RocketMQ Topic 管理 API
 * 提供 Topic 的列表查询、详情查看、创建、删除等操作
 */
import request from './request'
import type { ApiResponse } from './types'

/**

 * Topic 视图对象
 */
export interface TopicVO {
  topicName: string
  queueCount: number
  status: 'ACTIVE' | 'SUSPEND' | 'UNKNOWN'
  messageCount: number
  createTime: string
  readQueueNums?: number
  perm?: string
}

/**
 * Topic 详情视图对象
 */
export interface TopicDetailVO extends TopicVO {
  routeInfo?: any
  subscriptions?: string[]
}

/**
 * 创建 Topic 参数
 */
export interface CreateTopicParams {
  topicName: string
  queueCount?: number
  perm?: string
}

/**
 * 获取 Topic 列表
 * @param keyword 关键字筛选（可选）
 */
export function getTopicList(keyword?: string) {
  return request.get<ApiResponse<{ records: TopicVO[]; total: number }>>('/rocketmq/topics', {
    params: { keyword }
  })
}

/**
 * 获取 Topic 详情
 * @param topicName Topic 名称
 */
export function getTopicDetail(topicName: string) {
  return request.get<ApiResponse<TopicDetailVO>>(`/rocketmq/topics/${encodeURIComponent(topicName)}`)
}

/**
 * 创建 Topic
 * @param data 创建参数
 */
export function createTopic(data: CreateTopicParams) {
  return request.post<ApiResponse<void>>('/rocketmq/topics', data)
}

/**
 * 删除 Topic
 * @param topicName Topic 名称
 */
export function deleteTopic(topicName: string) {
  return request.delete<ApiResponse<void>>(`/rocketmq/topics/${encodeURIComponent(topicName)}`)
}

// ==================== Consumer Group 管理 ====================

/**
 * Consumer Group 视图对象
 */
export interface ConsumerGroupVO {
  group: string
  groupType: 'BROADCASTING' | 'CLUSTERING' | 'UNKNOWN'
  status: 'OK' | 'REBALANCE_NOT_INIT' | 'OFFLINE' | 'UNKNOWN'
  consumerCount: number
  accumulatedDiff: number
}

/**
 * Consumer Group 详情视图对象
 */
export interface ConsumerGroupDetailVO extends ConsumerGroupVO {
  totalDiff: number
  offsetTable: Record<string, number>
  subscriptions?: any[]
}

/**
 * 重置位点参数
 */
export interface ResetOffsetParams {
  topic: string
  timestamp: number
}

/**
 * 获取 Consumer Group 列表
 * @param keyword 关键字筛选（可选）
 */
export function getConsumerGroupList(keyword?: string) {
  return request.get<ApiResponse<{ records: ConsumerGroupVO[]; total: number }>>('/rocketmq/consumer-groups', {
    params: { keyword }
  })
}

/**
 * 获取 Consumer Group 详情
 * @param group Group 名称
 */
export function getConsumerGroupDetail(group: string) {
  return request.get<ApiResponse<ConsumerGroupDetailVO>>(`/rocketmq/consumer-groups/${encodeURIComponent(group)}`)
}

/**
 * 重置消费位点
 * @param group Group 名称
 * @param params 重置参数
 */
export function resetConsumerOffset(group: string, params: ResetOffsetParams) {
  return request.post<ApiResponse<void>>(`/rocketmq/consumer-groups/${encodeURIComponent(group)}/reset-offset`, params)
}

// ==================== Message 管理 ====================

/**
 * Message 视图对象
 */
export interface MessageVO {
  msgId: string
  topic: string
  tags: string
  keys: string
  timestamp: number
  queueId: number
  queueOffset: number
  properties: Record<string, string>
}

/**
 * Message 详情视图对象
 */
export interface MessageDetailVO extends MessageVO {
  body: string
}

/**
 * Message Trace 视图对象
 */
export interface MessageTraceVO {
  traceType: string
  traceTime: number
  regionId: string
  groupName: string
  costTime: number
  traceStatus: 'SUCCESS' | 'FAILED' | 'PARTIAL_SUCCESS'
  customId: string
  clientHost: string
  serverHost: string
  storeHost: string
  requestCode: string
}

/**
 * 获取 Message 列表
 * @param topic Topic 名称
 * @param keyword 关键字筛选（可选）
 * @param startTime 开始时间戳
 * @param endTime 结束时间戳
 * @param maxMsg 最大消息数
 */
export function getMessageList(topic: string, startTime?: number, endTime?: number, maxMsg?: number, keyword?: string) {
  return request.get<ApiResponse<{ records: MessageVO[]; total: number }>>(`/rocketmq/messages/${encodeURIComponent(topic)}`, {
    params: { startTime, endTime, maxMsg, keyword }
  })
}

/**
 * 获取 Message 详情
 * @param topic Topic 名称
 * @param msgId Message ID
 */
export function getMessageDetail(topic: string, msgId: string) {
  return request.get<ApiResponse<MessageDetailVO>>(`/rocketmq/messages/${encodeURIComponent(topic)}/${encodeURIComponent(msgId)}`)
}

/**
 * 获取 Message 轨迹
 * @param topic Topic 名称
 * @param msgId Message ID
 */
export function getMessageTrace(topic: string, msgId: string) {
  return request.get<ApiResponse<MessageTraceVO[]>>(`/rocketmq/messages/${encodeURIComponent(topic)}/${encodeURIComponent(msgId)}/trace`)
}
