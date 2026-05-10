/**
 * 硬件监控 TypeScript 类型定义。
 * 与后端 DTO 结构完全对齐，用于 SSE 推送数据解析和组件 Props 类型约束。
 */

/** 通用格式化值结构 */
export interface MetricValue<T> {
  value: T
  format: string
  unit: string
}

export interface CpuMetricsDTO {
  systemLoad: number
  perCoreLoad: number[]
  logicalCores: number
  physicalCores: number
  processorName: string
  processorFrequency: number
  cpu64bit: boolean
}

export interface MemoryMetricsDTO {
  total: MetricValue<number>
  available: MetricValue<number>
  used: MetricValue<number>
  usagePercent: MetricValue<number>
}

export interface DiskMetricsDTO {
  name: string
  model: string
  size: MetricValue<number>
  diskType: string
  /** 挂载点 */
  mount: string
  totalSpace: MetricValue<number>
  usedSpace: MetricValue<number>
  usableSpace: MetricValue<number>
  readBytesPerSec: MetricValue<number>
  writeBytesPerSec: MetricValue<number>
  readsPerSec: MetricValue<number>
  writesPerSec: MetricValue<number>
}

export interface NetworkMetricsDTO {
  displayName: string
  macAddress: string
  speed: number
  bytesSentPerSec: MetricValue<number>
  bytesRecvPerSec: MetricValue<number>
  packetsSentPerSec: MetricValue<number>
  packetsRecvPerSec: MetricValue<number>
}

export interface SystemInfoDTO {
  osFamily: string
  osVersion: string
  osVersionInfo: string
  systemUptime: number
  systemUptimeFormatted: string
  processCount: number
}

export interface HardwareMetricsDTO {
  timestamp: number
  cpu: CpuMetricsDTO | null
  memory: MemoryMetricsDTO | null
  disks: DiskMetricsDTO[] | null
  network: NetworkMetricsDTO[] | null
  system: SystemInfoDTO | null
}

/** 趋势数据点 */
export interface TrendDataPoint {
  timestamp: number
  value: number
  metric: string
}

/** 类型守卫：检查原始数据是否为有效的 HardwareMetricsDTO */
export function isValidHardwareMetrics(data: unknown): data is HardwareMetricsDTO {
  return (
    typeof data === 'object' &&
    data !== null &&
    'timestamp' in data &&
    typeof (data as HardwareMetricsDTO).timestamp === 'number'
  )
}

import request from '../request'
import type { ApiResponse } from '../types'

/**
 * 获取指定指标的趋势数据。
 *
 * @param metric 指标名称（cpu/memory/disk_read/disk_write/net_sent/net_recv）
 * @param range 趋势点数（1-360，默认 360）
 * @returns 趋势数据点列表
 */
export function fetchTrend(metric: string, range = 360) {
  return request.get<ApiResponse<TrendDataPoint[]>>('/monitor/hardware/trend', {
    params: { metric, range }
  })
}
