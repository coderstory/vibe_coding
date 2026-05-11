import { onUnmounted, ref } from 'vue'
import type { HardwareMetricsDTO } from '@/api/modules/hardware'

export interface UseHardwareMetricsOptions {
  /** EventSource 工厂函数，用于测试时注入 mock */
  createEventSource?: (url: string) => EventSource
  /** SSE 服务端点 URL */
  url?: string
}

/**
 * SSE 连接管理 composable。
 * 封装 EventSource 生命周期，支持指数退避重连（1s → 30s max）。
 * 组件卸载时自动断开连接。
 *
 * @param options 可选配置（createEventSource 工厂 / url）
 * @returns { connect, disconnect, reconnect, connected, metrics, error }
 */
export function useHardwareMetrics(options: UseHardwareMetricsOptions = {}) {
  const {
    createEventSource = (url: string) => new EventSource(url),
    url = `/api/monitor/hardware/subscribe?token=${localStorage.getItem('token') || ''}`
  } = options

  const connected = ref(false)
  const metrics = ref<HardwareMetricsDTO | null>(null)
  const error = ref<string | null>(null)
  let eventSource: EventSource | null = null
  let reconnectTimer: ReturnType<typeof setTimeout> | null = null
  let retryDelay = 1000
  const MAX_RETRY_DELAY = 30000

  function connect() {
    disconnect()

    eventSource = createEventSource(url)

    eventSource.addEventListener('connected', () => {
      connected.value = true
      error.value = null
      retryDelay = 1000 // 连接成功重置退避延迟
    })

    eventSource.addEventListener('metrics', (event: Event) => {
      try {
        const messageEvent = event as MessageEvent
        metrics.value = JSON.parse(messageEvent.data)
        error.value = null
      } catch {
        error.value = '解析监控数据失败'
      }
    })

    eventSource.onerror = () => {
      connected.value = false
      eventSource?.close()
      scheduleReconnect()
    }
  }

  /** 调度重连（指数退避：1s → 2s → 4s → 8s → 16s → max 30s） */
  function scheduleReconnect() {
    error.value = `连接断开，${retryDelay / 1000} 秒后重连...`
    reconnectTimer = setTimeout(() => {
      retryDelay = Math.min(retryDelay * 2, MAX_RETRY_DELAY)
      connect()
    }, retryDelay)
  }

  function disconnect() {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    eventSource?.close()
    eventSource = null
    connected.value = false
  }

  /** 手动重连（重置退避延迟为 1s） */
  function reconnect() {
    retryDelay = 1000
    connect()
  }

  onUnmounted(disconnect)

  return { connect, disconnect, reconnect, connected, metrics, error }
}
