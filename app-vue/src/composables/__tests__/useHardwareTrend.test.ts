import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { useHardwareTrend } from '../useHardwareTrend'
import type { ApiResponse } from '@/api/types'
import type { TrendDataPoint } from '@/api/modules/hardware'

function mockResponse(data: TrendDataPoint[]): { data: ApiResponse<TrendDataPoint[]> } {
  return { data: { code: 200, data, message: 'ok' } }
}

describe('useHardwareTrend', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('初始化后 loading 为 false，data 为空', () => {
    const fetchFn = vi.fn(() => Promise.resolve(mockResponse([])))
    const { loading, data } = useHardwareTrend('cpu', { fetchFn, immediate: false })

    expect(loading.value).toBe(false)
    expect(data.value).toEqual([])
  })

  it('调用 refresh() 后获取数据并更新', async () => {
    const mockData: TrendDataPoint[] = [
      { timestamp: 1000, value: 50.0, metric: 'cpu' },
      { timestamp: 2000, value: 55.0, metric: 'cpu' }
    ]
    const fetchFn = vi.fn(() => Promise.resolve(mockResponse(mockData)))
    const { loading, data, refresh } = useHardwareTrend('cpu', { fetchFn, immediate: false })

    const promise = refresh()
    expect(loading.value).toBe(true)

    await promise
    expect(loading.value).toBe(false)
    expect(data.value).toEqual(mockData)
  })

  it('请求失败时 error 有值', async () => {
    const fetchFn = vi.fn(() => Promise.reject(new Error('网络错误')))
    const { loading, error, refresh } = useHardwareTrend('cpu', { fetchFn, immediate: false })

    await refresh()
    expect(loading.value).toBe(false)
    expect(error.value).toBe('网络错误')
  })

  it('immediate=true 自动触发首次加载', async () => {
    const mockData: TrendDataPoint[] = [{ timestamp: 1000, value: 50.0, metric: 'cpu' }]
    const fetchFn = vi.fn(() => Promise.resolve(mockResponse(mockData)))
    const { loading, data } = useHardwareTrend('cpu', { fetchFn, immediate: true })

    expect(loading.value).toBe(true)
    await vi.runAllTimersAsync()
    expect(fetchFn).toHaveBeenCalledTimes(1)
    expect(data.value).toEqual(mockData)
  })

  it('轮询间隔自动刷新数据', async () => {
    const mockData: TrendDataPoint[] = [{ timestamp: 1000, value: 50.0, metric: 'cpu' }]
    const fetchFn = vi.fn(() => Promise.resolve(mockResponse(mockData)))
    useHardwareTrend('cpu', { fetchFn, immediate: false, pollIntervalMs: 5000 })

    expect(fetchFn).toHaveBeenCalledTimes(0)
    await vi.advanceTimersByTimeAsync(5000)
    expect(fetchFn).toHaveBeenCalledTimes(1)
    await vi.advanceTimersByTimeAsync(5000)
    expect(fetchFn).toHaveBeenCalledTimes(2)
  })

  it('组件卸载时清理轮询定时器', async () => {
    const fetchFn = vi.fn(() => Promise.resolve(mockResponse([])))
    const { cleanup } = useHardwareTrend('cpu', { fetchFn, immediate: false, pollIntervalMs: 5000 })

    cleanup()
    await vi.advanceTimersByTimeAsync(10000)
    expect(fetchFn).toHaveBeenCalledTimes(0)
  })
})
