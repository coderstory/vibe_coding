import { ref, onUnmounted } from 'vue'
import type { TrendDataPoint } from '@/api/modules/hardware'
import { fetchTrend } from '@/api/modules/hardware'
import type { ApiResponse } from '@/api/types'

export interface UseHardwareTrendOptions {
  /** 是否立即加载（默认 true） */
  immediate?: boolean
  /** 轮询间隔（ms），0 表示不轮询 */
  pollIntervalMs?: number
  /** 趋势点数范围（1-360） */
  range?: number
  /** fetch 函数，用于测试注入 mock */
  fetchFn?: (metric: string, range: number) => Promise<{ data: ApiResponse<TrendDataPoint[]> }>
}

/**
 * 硬件指标趋势数据拉取 composable。
 * 封装趋势 API 调用，支持 loading/error 状态、自动轮询刷新。
 * 组件卸载时自动清理。
 *
 * @param metric 指标名称
 * @param options 可选配置
 * @returns { data, loading, error, refresh, cleanup }
 */
export function useHardwareTrend(
  metric: string,
  options: UseHardwareTrendOptions = {}
) {
  const {
    immediate = true,
    pollIntervalMs = 0,
    range = 360,
    fetchFn = fetchTrend
  } = options

  const data = ref<TrendDataPoint[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)
  let pollTimer: ReturnType<typeof setTimeout> | null = null

  async function refresh() {
    loading.value = true
    error.value = null
    try {
      const res = await fetchFn(metric, range)
      data.value = res.data.data || []
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '请求失败'
    } finally {
      loading.value = false
    }
  }

  function startPoll() {
    if (pollIntervalMs <= 0) return
    pollTimer = setTimeout(async () => {
      await refresh()
      startPoll()
    }, pollIntervalMs)
  }

  function cleanup() {
    if (pollTimer) {
      clearTimeout(pollTimer)
      pollTimer = null
    }
  }

  if (immediate) {
    refresh().then(() => {
      if (pollIntervalMs > 0) startPoll()
    })
  } else if (pollIntervalMs > 0) {
    startPoll()
  }

  onUnmounted(cleanup)

  return { data, loading, error, refresh, cleanup }
}
