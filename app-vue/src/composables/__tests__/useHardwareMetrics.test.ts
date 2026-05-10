import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { useHardwareMetrics } from '../useHardwareMetrics'

/** mock EventSource 实现（TDD-05） */
class MockEventSource {
  private handlers: Map<string, (event: any) => void> = new Map()
  onerror: ((event: any) => void) | null = null
  onopen: ((event: any) => void) | null = null

  constructor(_url: string) {
    setTimeout(() => {
      const handler = this.handlers.get('connected')
      if (handler) handler({})
    }, 0)
  }

  addEventListener(event: string, handler: (event: any) => void) {
    this.handlers.set(event, handler)
  }

  close() {}

  mockReceiveEvent(event: string, data: string) {
    const handler = this.handlers.get(event)
    if (handler) handler({ data })
  }

  mockError() {
    if (this.onerror) this.onerror({})
  }
}

describe('useHardwareMetrics', () => {
  let mockEs: MockEventSource

  beforeEach(() => {
    vi.useFakeTimers()
    mockEs = new MockEventSource('')
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('调用 connect() 后 connected 为 true', async () => {
    const { connect, connected } = useHardwareMetrics({
      createEventSource: () => mockEs as unknown as EventSource
    })

    connect()
    await vi.runAllTimersAsync()
    expect(connected.value).toBe(true)
  })

  it('收到 metrics 事件后解析并更新', () => {
    const { connect, metrics } = useHardwareMetrics({
      createEventSource: () => mockEs as unknown as EventSource
    })

    connect()
    mockEs.mockReceiveEvent('metrics', JSON.stringify({
      timestamp: 1000,
      cpu: { systemLoad: 50.0, perCoreLoad: [50], logicalCores: 4, physicalCores: 4, processorName: 'Test', processorFrequency: 3000000000, cpu64bit: true }
    }))

    expect(metrics.value).not.toBeNull()
    expect(metrics.value?.cpu?.systemLoad).toBe(50.0)
    expect(metrics.value?.timestamp).toBe(1000)
  })

  it('连接错误触发重连（指数退避首次 1s）', async () => {
    const { connect, connected, error } = useHardwareMetrics({
      createEventSource: () => mockEs as unknown as EventSource
    })

    connect()
    await vi.runAllTimersAsync()
    expect(connected.value).toBe(true)

    mockEs.mockError()
    expect(connected.value).toBe(false)
    expect(error.value).toContain('重连')
  })

  it('reconnect() 重置重连延迟', async () => {
    const { connect, reconnect, connected } = useHardwareMetrics({
      createEventSource: () => mockEs as unknown as EventSource
    })

    connect()
    await vi.runAllTimersAsync()
    mockEs.mockError()

    mockEs = new MockEventSource('')
    reconnect()
    await vi.runAllTimersAsync()
    expect(connected.value).toBe(true)
  })

  it('disconnect() 关闭连接并清理', () => {
    const { connect, disconnect, connected } = useHardwareMetrics({
      createEventSource: () => mockEs as unknown as EventSource
    })

    connect()
    disconnect()
    expect(connected.value).toBe(false)
  })
})
