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
