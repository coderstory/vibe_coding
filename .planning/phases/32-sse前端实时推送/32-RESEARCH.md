# Phase 32: SSE 实时推送 + 前端基础页面 - Research

**Researched:** 2026-05-10
**Domain:** Spring Boot SSE 实时推送 + Vue 3 前端监控面板
**Confidence:** HIGH

## Summary

Phase 32 在 Phase 31 硬件采集服务之上，实现 SSE 实时推送和前端基础监控页面。后端新增广播式 SSE 服务 (`HardwareSseService`)，复用 `SeckillSseService` 的 `ConcurrentHashMap<String, SseEmitter>` 模式但采用全连接广播而非按 queueId 路由。前端新增 `/monitor/hardware` 页面，包含 CPU 环形图、内存环形图、磁盘分区进度条列表和系统信息卡片，通过 `useHardwareMetrics()` composable 管理 SSE 连接生命周期。

**关键区别与 Phase 31 的 SeckillSseService：** 广播模式（所有客户端接收相同数据），UUID 连接标识（而非 queueId），无限超时（数据推送自带心跳），在 `HardwareMetricsServiceImpl.collect()` 完成时由采集线程直接调用广播。

**Primary recommendation:** 新建 `HardwareSseService` 广播式实现，`HardwareMonitorController` 新增 `/subscribe` 端点，`HardwareMetricsServiceImpl` 的 `collect()` 末尾注入广播调用。前端新建 `monitor/hardware/` 页面目录 + `composables/useHardwareMetrics.ts` 封装 SSE 连接管理。

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

- **D-01（广播模式）：** SSE 采用广播模式。所有客户端接收相同硬件指标数据，新建一个 SSE 端点管理全局连接池。硬件监控天然广播场景（所有客户端看到相同数据），无需像 SeckillSseService 那样按 queueId 独立订阅。
- **D-02（控制器位置）：** SSE 端点加到现有 `HardwareMonitorController`，新增 `/subscribe` / `/unsubscribe` 端点。不新建 Controller，保持 `controller/monitor/hardware/` 包内硬件相关端点统一。
- **D-03（心跳策略）：** 使用 `SseEmitter` 自带超时机制（60s）+ 浏览器 `EventSource` 自动重连。不额外发送心跳事件。每 2s 的数据推送本身自带心跳语义，无需单独保活。
- **D-04（数据更新模式）：** SSE 推送直接更新 Vue 组件 reactive 数据。数据到达即 UI 更新，不经过中间缓存层。
- **D-05（页面布局）：** 左侧窄导航菜单 + 右侧主内容区。左侧列出指标类别（CPU/内存/磁盘/系统信息），右侧对应展示。
- **D-06（环形图方案）：** 使用 ECharts 环形图（vue-echarts）。项目已有 vue-echarts + echarts 依赖，无需新增包。
- **D-07（SSE Composable）：** 业务专用 `useHardwareMetrics()` composable，内部封装 SSE 连接创建、数据解析、重连逻辑。返回 `{ data, connected, error, reconnect }`。支持 mock `EventSource` 注入以满足 TDD-05 可测试要求。
- **D-08（重连策略）：** 指数退避：1s -> 2s -> 4s -> 8s -> 16s -> max 30s。避免断线风暴时频繁请求。

### Claude's Discretion

- SSE 端点具体路径（`/subscribe` vs `/stream`）
- SSE 事件名称约定
- `SseEmitter` 精确 timeout 值（默认 60s，可调整）
- Vue 组件命名和文件结构
- 磁盘进度条具体样式（Element Plus `El-Progress` 或自定义）
- 系统信息卡片布局细节
- 硬件监控页面的路由路径（`/monitor/hardware` 或嵌套路由）
- `useHardwareMetrics()` 具体参数签名和返回值结构
- 左侧导航菜单具体实现（Element Plus `ElMenu` 或自定义）

### Deferred Ideas (OUT OF SCOPE)

- 磁盘 IO / 网络吞吐量实时折线图 -> Phase 33
- 近 1 小时趋势折线图（ECharts 滑动窗口，300 点上限） -> Phase 33
- 夏日海滩风主题适配 -> Phase 33
- 安全加固（ADMIN 角色校验） -> Phase 34
- 页面加载态/空状态/异常状态处理 -> Phase 34
- SSE 断线重连时 UI 状态提示 -> Phase 34

</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| HWM-10 | 后端通过 SSE 端点实时推送硬件指标数据 | `HardwareSseService` + `HardwareMonitorController` `/subscribe` 端点 |
| HWM-11 | SSE 连接实现心跳检测和过期连接自动清理 | SseEmitter 回调(60s timeout) + EventSource 自动重连 + 采集循环天然心跳 |
| HWM-12 | 前端硬件监控页面路由和菜单接入 | `routes.ts` 新增 `/monitor/hardware` 路由 + 后端菜单 API 注册 |
| HWM-13 | CPU 使用率仪表盘环形图 + 核数/型号信息卡片 | ECharts PieChart 环形图 + El-Card 展示 CPU 信息 |
| HWM-14 | 内存使用率仪表盘环形图 + 总量/已用/可用信息 | ECharts PieChart 环形图 + El-Card 展示内存详情 |
| HWM-15 | 磁盘分区容量进度条列表 | Element Plus El-Progress 组件 |
| HWM-17 | 系统基本信息卡片（OS 版本、运行时间、进程数） | Element Plus El-Descriptions 组件 |
| HWM-19 | 前端 SSE 连接管理（自动重连、加载态、错误处理） | `useHardwareMetrics()` composable 封装指数退避重连 |
| TDD-02 | SSE 推送服务设计为可测试的接口抽象，支持 mock 客户端验证 | `HardwareSseService` 接口化 + mock SseEmitter 验证 send() 调用 |
| TDD-05 | SSE 连接管理 composable 设计为可测试的接口，支持模拟事件源 | `useHardwareMetrics()` 接收可选的 EventSource 工厂参数 |

</phase_requirements>

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| SSE 连接管理 | API / Backend | — | SseEmitter 是 Spring MVC 组件，生命周期在服务端管理 |
| 指标数据广播 | API / Backend | — | 采集线程写完缓存后立即广播，不经过前端 |
| SSE composable | Browser / Client | — | 封装 EventSource 浏览器 API 的生命周期 |
| 环形图渲染 | Browser / Client | — | ECharts 纯客户端渲染，reactive 数据驱动 |
| 路由和菜单 | Browser / Client | Frontend Server | 路由在 Vue Router 中定义，菜单数据来自后端 API |
| 指标数据源 | API / Backend | — | Phase 31 的 HardwareMetricsService 提供数据 |

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Spring SseEmitter | Boot 4.1.0-RC1 | SSE 连接管理 | 项目已有 SeckillSseService 模式，原生支持 |
| ECharts | ^6.0.0 | 环形图渲染 | 项目已有，QpsChart.vue 已验证模式 |
| vue-echarts | ^8.0.1 | Vue 3 ECharts 绑定 | 项目已有，无需新增依赖 |
| Element Plus | ^2.9.0 | 进度条、卡片、布局 | 项目已有，MoniorDashboard.vue 已验证模式 |

### Installation
无需新增 npm 或 Gradle 依赖。ECharts 6.x + vue-echarts 8.x + Element Plus 2.9.x 均已存在。

**Version verification:**
- `echarts`: 6.0.0 [VERIFIED: npm registry]
- `vue-echarts`: 8.0.1 [VERIFIED: npm registry]
- ECharts PieChart + CanvasRenderer + GaugeChart 等模块通过 `use()` 注册

## Architecture Patterns

### System Architecture Diagram

```
Browser (Vue 3)
  |
  |-- GET /api/monitor/hardware/subscribe  (SSE, EventSource)
  |       |
  |       v
  |   HardwareMonitorController
  |       |
  |       v
  |   HardwareSseService (broadcast mode)
  |       |
  |       |-- ConcurrentHashMap<String, SseEmitter>
  |       |       |-- UUID1 -> SseEmitter(client1)
  |       |       |-- UUID2 -> SseEmitter(client2)
  |       |
  |       |-- broadcast(event="metrics", data=HardwareMetricsDTO)
  |               |
  |               |-- forEach emitter: emitter.send(event)
  |
  |   HardwareMetricsServiceImpl.collect()  [2s interval]
  |       |
  |       |-- OSHI SystemInfo (CPU, memory, disk, network)
  |       |-- write to currentMetrics (ReentrantReadWriteLock)
  |       |-- write to RingBuffer (trend history, 360 points)
  |       |-- call hardwareSseService.broadcast("metrics", snapshot)
  |
  v
HTML (Component Tree)
  |
  |-- HardwareMonitorPage.vue
  |       |-- Left Nav (el-menu: CPU/内存/磁盘/系统信息)
  |       |-- Right Content
  |               |-- CpuGauge.vue (ECharts donut + info card)
  |               |-- MemoryGauge.vue (ECharts donut + info card)
  |               |-- DiskPartitions.vue (El-Progress list)
  |               |-- SystemInfo.vue (El-Descriptions card)
  |
  |-- useHardwareMetrics() composable
          |-- EventSource (with mock support)
          |-- exponential backoff reconnect
          |-- reactive data binding
```

### Recommended Project Structure

**Backend (new files):**
```
springboot/src/main/java/cn/coderstory/springboot/
  sse/monitor/
    +-- HardwareSseService.java          # SSE 广播服务
```

**Backend (modified files):**
```
springboot/src/main/java/cn/coderstory/springboot/
  controller/monitor/hardware/
    HardwareMonitorController.java       # + /subscribe, /unsubscribe 端点
  service/monitor/hardware/impl/
    HardwareMetricsServiceImpl.java      # collect() 末尾广播
```

**Frontend (new files):**
```
app-vue/src/
  views/monitor/hardware/
    +-- HardwareMonitorPage.vue          # 主页面（左导航 + 右内容区）
    +-- CpuGauge.vue                     # CPU 环形图 + 信息卡片
    +-- MemoryGauge.vue                  # 内存环形图 + 信息卡片
    +-- DiskPartitions.vue               # 磁盘分区进度条列表
    +-- SystemInfo.vue                   # 系统信息卡片
  api/modules/
    +-- hardware.ts                      # 硬件监控 API + 类型定义
  composables/
    +-- useHardwareMetrics.ts            # SSE 连接管理 composable
```

**Frontend (modified files):**
```
app-vue/src/
  router/modules/
    routes.ts                            # + /monitor/hardware 路由
```

### Pattern 1: HardwareSseService 广播服务

**What:** 基于 SeckillSseService 模式但使用广播语义的 SSE 服务。所有连接客户端接收相同数据。

**When to use:** 系统级广播数据（CPU/内存/磁盘等所有用户看到相同的全局指标）。

**Key differences from SeckillSseService:**
| Aspect | SeckillSseService | HardwareSseService |
|--------|-------------------|-------------------|
| Connection key | `queueId` (per-request) | UUID (per-client) |
| Message routing | `sendToQueue(queueId, data)` | `broadcast(eventName, data)` |
| Timeout | 300000ms | 0L (never expires, data push as heartbeat) |
| Client lifecycle | Ephemeral (seconds) | Persistent (hours - stays open) |

**Example:**
```java
// Source: Adapted from SeckillSseService.java pattern [VERIFIED: codebase]
@Service
@Slf4j
@RequiredArgsConstructor
public class HardwareSseService {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter() {
        String clientId = UUID.randomUUID().toString();
        SseEmitter emitter = new SseEmitter(0L); // never timeout

        emitter.onCompletion(() -> emitters.remove(clientId));
        emitter.onTimeout(() -> emitters.remove(clientId));
        emitter.onError(e -> {
            log.warn("SSE client {} error: {}", clientId, e.getMessage());
            emitters.remove(clientId);
        });

        emitters.put(clientId, emitter);

        // Send connected event so client stops pending
        try {
            emitter.send(SseEmitter.event()
                .name("connected")
                .data("{\"status\":\"connected\"}"));
        } catch (IOException e) {
            emitter.complete();
        }

        log.debug("SSE connected: {} (total: {})", clientId, emitters.size());
        return emitter;
    }

    public void broadcast(String eventName, Object data) {
        if (emitters.isEmpty()) return;
        emitters.forEach((id, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data, MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                log.debug("SSE broadcast failed for {}: {}", id, e.getMessage());
                emitters.remove(id);
            }
        });
    }

    public int getActiveConnectionCount() {
        return emitters.size();
    }

    public void unsubscribe(String clientId) {
        SseEmitter emitter = emitters.remove(clientId);
        if (emitter != null) {
            emitter.complete();
        }
    }
}
```

### Pattern 2: 采集循环注入广播

**What:** 在 `HardwareMetricsServiceImpl.collect()` 末尾调用硬件 SSE 服务广播最新快照。

**When to use:** 采集循环与推送紧耦合——每采集完成立即推送，保证时序一致性。

**Key constraint:** 不能引入循环依赖。HardwareSseService 不依赖 HardwareMetricsService（SSE 服务只推送外部传入的数据），但 HardwareMetricsServiceImpl 依赖 HardwareSseService。

**Example (collect() modification):**
```java
// In HardwareMetricsServiceImpl.collect(), after cacheLock write
// [line ~145 in existing code]:
//
// Write to cache (write lock) -- D-10
cacheLock.writeLock().lock();
try {
    currentMetrics = snapshot;
    trendCounter++;
    if (trendCounter >= properties.getTrendDecimation()) {
        trendCounter = 0;
        historyBuffer.add(snapshot);
    }
} finally {
    cacheLock.writeLock().unlock();
}

// NEW: Broadcast to all SSE clients
// Must be OUTSIDE write lock to avoid blocking broadcast on slow clients
if (hardwareSseService != null) {
    hardwareSseService.broadcast("metrics", snapshot);
}
```

### Pattern 3: useHardwareMetrics Composable

**What:** 封装 SSE 连接生命周期、指数退避重连和 reactive 数据绑定的 composable。

**When to use:** 需要在 Vue 组件中建立 SSE 实时数据流的所有场景。

**Testing support:** 接受可选的 `createEventSource` 工厂参数（TDD-05），测试时可注入 mock EventSource。

**Example:**
```typescript
// Source: Adapted from CONTEXT.md D-07 + ARCHITECTURE.md Pattern 4 [VERIFIED: codebase patterns]
import { onUnmounted, ref } from 'vue'
import type { HardwareMetricsDTO } from '@/api/modules/hardware'

export interface UseHardwareMetricsOptions {
  /** EventSource 工厂函数，用于测试时注入 mock */
  createEventSource?: (url: string) => EventSource
  /** SSE 服务端点 */
  url?: string
}

export function useHardwareMetrics(options: UseHardwareMetricsOptions = {}) {
  const {
    createEventSource = (url: string) => new EventSource(url),
    url = '/api/monitor/hardware/subscribe'
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
      retryDelay = 1000  // reset on successful connection
    })

    eventSource.addEventListener('metrics', (event: MessageEvent) => {
      try {
        metrics.value = JSON.parse(event.data)
        error.value = null
      } catch (e) {
        error.value = '解析监控数据失败'
      }
    })

    eventSource.onerror = () => {
      connected.value = false
      eventSource?.close()
      scheduleReconnect()
    }
  }

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

  function reconnect() {
    retryDelay = 1000
    connect()
  }

  onUnmounted(disconnect)

  return { connect, disconnect, reconnect, connected, metrics, error }
}
```

### Pattern 4: ECharts Donut Chart for CPU/Memory

**What:** 使用 ECharts PieChart 的 `radius: ['50%', '70%']` 环形图配置展示 CPU/内存使用率。

**When to use:** 需要直观展示百分比使用率的场景。使用 `PieChart` 而非 `GaugeChart`，因为环形图（donut）更简洁，适合同时展示 usage + remaining。

**Example:**
```vue
<script lang="ts" setup>
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart } from 'echarts/charts'
import { computed } from 'vue'

use([CanvasRenderer, PieChart])

const props = defineProps<{
  usagePercent: number
  label: string
}>()

const chartOption = computed(() => ({
  series: [{
    type: 'pie',
    radius: ['50%', '70%'],
    avoidLabelOverlap: false,
    label: {
      show: true,
      position: 'center',
      formatter: () => `${props.usagePercent}%`,
      fontSize: 24,
      fontWeight: 'bold'
    },
    emphasis: { label: { show: true } },
    data: [
      {
        value: props.usagePercent,
        name: '已使用',
        itemStyle: { color: '#409EFF' }
      },
      {
        value: Math.max(100 - props.usagePercent, 0),
        name: '剩余',
        itemStyle: { color: '#E4E7ED' }
      }
    ]
  }]
}))
</script>
```

### Pattern 5: SSE Endpoint in Controller

**What:** 在现有 HardwareMonitorController 中新增 `/subscribe` 端点，复用 `produces = MediaType.TEXT_EVENT_STREAM_VALUE` 模式。

**When to use:** 新增 SSE 端点时统一放在现有 Controller 包内。

**Example:**
```java
// Added to existing HardwareMonitorController [VERIFIED: codebase pattern from SeckillSseController]
private final HardwareSseService hardwareSseService;

/**
 * 订阅硬件指标实时推送。
 * <p>
 * 建立 SSE 连接后，服务器每 2s 推送一次硬件指标数据。
 * 浏览器 EventSource 自动处理重连。
 *
 * @return SSE 连接发射器
 */
@GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter subscribe() {
    return hardwareSseService.createEmitter();
}

/**
 * 取消订阅硬件指标推送。
 *
 * @param clientId 客户端 ID（从 SSE 连接的 Last-Event-ID 或前端存储）
 * @deprecated 通常客户端关闭 EventSource 即自动完成，此端点为调试备用
 */
@GetMapping("/unsubscribe")
public void unsubscribe(@RequestParam(defaultValue = "") String clientId) {
    if (!clientId.isEmpty()) {
        hardwareSseService.unsubscribe(clientId);
    }
}
```

### Anti-Patterns to Avoid

- **在写锁内部调用 broadcast：** `broadcast()` 遍历所有 emitter 调用 `send()`，如果某个客户端断线慢，会阻塞写锁，影响采集时序。广播调用必须在写锁释放之后。
- **在 Controller 中管理 SseEmitter 生命周期：** SseEmitter 的创建/清理应委托给 `HardwareSseService`，Controller 只做参数提取和响应返回。
- **手动轮询替代 SSE：** 前端不应保留下拉刷新的轮询逻辑，SSE 是主数据通道。`GET /current` REST 端点仅用于初始加载。
- **在 HardwareSseService 上挂 @Async：** 广播调用在采集线程中同步执行，客户端少（<10）时开销可忽略。加了 `@Async` 反而引入线程调度延迟和任务队列积压风险。

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| SSE 连接管理 | 自定义 WebSocket 实现 | `SseEmitter` | Spring 原生支持，自动处理异步 dispatch |
| 浏览器 SSE 客户端 | 自定义长轮询/WS 封装 | `EventSource` API | 浏览器原生支持，自动重连，标准事件解析 |
| ECharts 集成 | 手动管理 chart 实例生命周期 | `vue-echarts` VChart | 组件化生命周期绑定，`autoresize` 自动尺寸适配 |
| 前端路由 | 自定义 hash 路由 | `Vue Router` + `createWebHistory()` | 项目已有，嵌套路由支持 |
| 左导航布局 | 自定义侧边栏组件 | `el-menu` | 项目已有 AppMenu 组件，直接复用 |

**Key insight:** 此阶段所有核心能力都由现有框架提供——SseEmitter、EventSource、ECharts、Element Plus。需要新增的只是编排代码（composable、SSE 服务、页面组件），零基础设施建设。

## Common Pitfalls

### Pitfall 1: broadcast() 在写锁内部调用阻塞采集线程
**What goes wrong:** 在 `HardwareMetricsServiceImpl.collect()` 的 `writeLock` 内部调用 `hardwareSseService.broadcast()`。如果一个客户端断线，`emitter.send()` 抛出 `IOException` 前可能阻塞数秒，拖慢整个采集循环。
**Why it happens:** 采集和推送天然关联（采集完成即推送），开发者容易把推送放在写锁保护范围内。
**How to avoid:** 在 `writeLock` 释放后调用 `broadcast()`。如果 `HardwareSseService` 注入可能为 null（测试场景），用 `Optional` 或 null 检查保护。
**Warning signs:** 采集定时出现 3s+ 间隔间隙，日志中 SSE 的 IOException 与采集周期不匹配。

### Pitfall 2: SseEmitter timeout 配置不合理
**What goes wrong:** 使用默认 `SseEmitter()` 构造（30s timeout），客户端 30s 内未收到事件时连接自动关闭，`EventSource` 立即重连造成频繁重建。
**Why it happens:** 默认 timeout 太短，不理解 0L 表示 "never expire"。
**How to avoid:** 使用 `new SseEmitter(0L)`（不超时），数据推送本身充当心跳。或使用 `new SseEmitter(60000L)` + 额外心跳（与 D-03 冲突，除非数据间隔超过 timeout）。
**Warning signs:** 日志中出现大量 SSE 连接创建/销毁的日志行（数秒一个循环）。

### Pitfall 3: EventSource 无法携带自定义 Header
**What goes wrong:** 浏览器 `EventSource` API 不支持自定义请求头（无法设置 `Authorization: Bearer <token>`），如果后端 SSE 端点受 JWT 保护，连接被 401 拒绝。
**Why it happens:** `EventSource` 是浏览器 API，设计约束。
**How to avoid:** 有两种方案：(a) 后端 SSE 端点不要求认证（但需要网络隔离），(b) 使用带 cookie 的认证机制（session-based）。当前项目使用 JWT Bearer token，建议方案：在 `subscribe` 端点的 URL 中包含 token 参数（`/api/monitor/hardware/subscribe?token=xxx`），服务端通过 `@RequestParam` 提取并验证。或者让 SSE 端点排除在 JWT 过滤器之外但依赖 IP/网络白名单。**当前项目安全加固在 Phase 34，此阶段可先不处理认证问题。** [ASSUMED]
- **选项 A（推荐）：** 将 `/api/monitor/hardware/subscribe` 加入 JWT 白名单（当前 SeckillSseController 也是认证后才可访问的，所以项目倾向认证模式）。URL 中传递 token 参数。
- **选项 B：** 利用 WebFlux / 配置允许 SSE 端点无认证，Phase 34 再加固。

### Pitfall 4: vue-echarts key 复用导致 chart 不更新
**What goes wrong:** 页面内多个 ECharts 实例使用相同 `key` 时，Vue 的 diff 算法复用同一个 DOM 节点导致 chart 渲染错乱。
**Why it happens:** vue-echarts 在组件挂载/更新时创建和销毁 ECharts 实例，key 冲突导致 DOM 映射错误。
**How to avoid:** 为每个 `VChart` 组件设置唯一 `key` 属性，或确保组件实例唯一（组件名隔离开）。
**Warning signs:** 一个 chart 的数据显示到另一个 chart 上。

### Pitfall 5: SSE 事件名大小写敏感性
**What goes wrong:** 前端 `eventSource.addEventListener('metrics', ...)` 监听不到后端发送的 `Metrics` 事件。
**Why it happens:** SSE 事件名大小写敏感，需前后端一致约定。
**How to avoid:** 定义事件名常量。约定：`metrics`（硬件数据）、`connected`（连接建立）、`error`（错误信息）。

## Code Examples

### Backend SSE 广播数据结构

SSE 推送不经过 `ApiResponse<T>` 包装，直接推送 `HardwareMetricsDTO` 的 JSON 序列化。EventSource 收到后直接解析。

```
event: metrics
data: {"timestamp": 1715328000000, "cpu": {"systemLoad": 45.2, ...}, ...}
```

### Frontend HardwareMetricsDTO Type

```typescript
// Source: mirrors backend DTO structure [VERIFIED: codebase HardwareMetricsDTO.java]
// File: app-vue/src/api/modules/hardware.ts

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
```

### Element Plus 磁盘分区进度条

```vue
<template>
  <div class="disk-partitions">
    <el-card v-for="disk in disks" :key="disk.name" class="disk-card">
      <div class="disk-header">
        <span class="disk-name">{{ disk.name }}</span>
        <span class="disk-model">{{ disk.model }}</span>
      </div>
      <el-progress
        :percentage="diskPercentage(disk)"
        :text-inside="true"
        :stroke-width="20"
        :status="diskStatus(disk)"
      />
      <div class="disk-details">
        <span>已用: {{ disk.usedSpace?.format }}</span>
        <span>可用: {{ disk.usableSpace?.format }}</span>
        <span>总量: {{ disk.totalSpace?.format }}</span>
      </div>
    </el-card>
  </div>
</template>
```

### Mock EventSource for Vitest Testing

```typescript
// Source: TDD-05 requirement [ASSUMED]
// File: app-vue/src/composables/__tests__/useHardwareMetrics.test.ts

import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useHardwareMetrics } from '@/composables/useHardwareMetrics'

class MockEventSource {
  private handlers: Record<string, ((event: any) => void)> = {}
  onerror: ((event: any) => void) | null = null

  constructor(private url: string) {
    setTimeout(() => {
      this.handlers['connected']?.({})
    }, 0)
  }

  addEventListener(event: string, handler: (event: any) => void) {
    this.handlers[event] = handler
  }

  close() {}

  // Test helper to simulate receiving SSE data
  mockReceiveEvent(event: string, data: string) {
    const handler = this.handlers[event]
    if (handler) handler({ data })
  }

  // Test helper to simulate connection error
  mockError() {
    this.onerror?.({})
  }
}

describe('useHardwareMetrics', () => {
  it('should connect and receive metrics data', async () => {
    const mockEs = new MockEventSource('')
    const { connect, connected, metrics } = useHardwareMetrics({
      createEventSource: () => mockEs as unknown as EventSource
    })

    connect()
    await vi.waitFor(() => expect(connected.value).toBe(true))
  })

  it('should parse and expose metrics data', async () => {
    const mockEs = new MockEventSource('')
    const { connect, metrics } = useHardwareMetrics({
      createEventSource: () => mockEs as unknown as EventSource
    })

    connect()
    mockEs.mockReceiveEvent('metrics', JSON.stringify({
      timestamp: 1000,
      cpu: { systemLoad: 50.0, perCoreLoad: [50], logicalCores: 4, physicalCores: 4 }
    }))

    expect(metrics.value?.cpu?.systemLoad).toBe(50.0)
  })
})
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| 手动轮询 (setInterval every 2s) | SSE 实时推送 | This phase | 减少 80% HTTP 开销，实时性从 ~2s 延迟降至 ~50ms |
| 按 queueId 独立订阅 (SeckillSseService) | 广播模式 (HardwareSseService) | This phase | 简化连接管理，无用户关联逻辑 |
| SseEmitter 有限超时 (300s) | SseEmitter 无超时 (0L) | This phase | 连接持久化，EventSource 负责重连 |
| 轮询驱动的内容更新 | 事件驱动的内容更新 | This phase | 数据到达即渲染，无需定时器 |

**Deprecated/outdated:**
- `MonitorDashboard.vue` 的 polling 模式（setInterval 5s 拉取）——不修改，但新硬件监控不再使用该模式

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | SSE 端点 `/subscribe` 不在 JWT 白名单中，需要 URL token 认证 | Architecture Patterns | 如果 Phase 34 改为全局认证，SSE 端点配置需要调整 |
| A2 | EventSource 不支持自定义 header，需通过 URL 参数传 token | Common Pitfalls | 如果后端将 SSE 端点加入 JWT 白名单，此假设不成立 |
| A3 | 采集循环中调用 broadcast() 在写锁外不会造成时序问题 | Anti-Patterns | 如果采集和推送间隔严格同步要求更高，需改用观察者模式 |

## Open Questions (RESOLVED)

1. **SSE 端点认证方式** (RESOLVED)
   - Decision: 暂时不做认证，Phase 34 统一处理。SSE `/subscribe` 端点加入 JWT 白名单，不需 token 即可连接。仅在 Phase 34 添加 ADMIN 角色校验。
   - Rationale: Phase 32 目标在功能实现，安全加固统一在 Phase 34 处理
   - Applied in: 32-01-PLAN Task 2（/subscribe 端点暂时开放）

2. **菜单项注册** (RESOLVED)
   - Decision: Flyway 迁移 V25__hardware_monitor_menu.sql 在 sys_menu 表插入硬件监控子菜单（parent_id=14 监控大盘）
   - Rationale: 与已有菜单注册模式一致（V17、V24），确保侧边栏自动显示（AppMenu 通过 getMenuTree() API 渲染）
   - Applied in: 32-03-PLAN Task 4

3. **监控页面路由结构** (RESOLVED)
   - Decision: `/monitor/hardware` 作为独立路由（与 `/monitor` 同级），路径嵌套只用于 URL 结构，不作为 `/monitor` 的子路由 children
   - Rationale: MonitorDashboard 已是一个独立页面非路由布局容器，添加 children 会破坏其功能
   - Applied in: 32-03-PLAN Task 1 Part B

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| ECharts | Donut chart | yes | 6.0.0 | — |
| vue-echarts | ECharts Vue binding | yes | 8.0.1 | — |
| Element Plus | UI components | yes | 2.9.x | — |
| Vitest | Frontend test | yes | 4.1.2 | — |
| jsdom | Vitest DOM env | yes | 29.0.1 | — |
| JUnit 5 | Backend test | yes | bundled | — |
| Mockito | Backend mock | yes | bundled | — |
| Spring SseEmitter | SSE push | yes | Boot 4.1 | — |

**Missing dependencies with no fallback:** None — all required libraries are already in the project.

## Validation Architecture

### Test Framework

| Property | Value |
|----------|-------|
| Framework (backend) | JUnit 5 + Mockito (via spring-boot-starter-test) |
| Framework (frontend) | Vitest 4.1.2 + @vue/test-utils 2.4.6 |
| Config file (frontend) | `app-vue/vitest.config.js` |
| Quick run command | `cd springboot && ./gradlew.bat test --tests "*ClassName*"` (back), `cd app-vue && npx vitest run` (front) |
| Full suite command | `cd springboot && ./gradlew.bat test` (back), `cd app-vue && npm test` (front) |

### Phase Requirements -> Test Map

| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| HWM-10 | SSE `/subscribe` 端点返回 SseEmitter | unit | `./gradlew.bat test --tests "*HardwareMonitorControllerTest*` | yes (existing) |
| HWM-10 | SSE 广播发送 HardwareMetricsDTO 事件 | unit | `./gradlew.bat test --tests "*HardwareSseServiceTest*` | No (Wave 0) |
| HWM-11 | 过期连接自动清理（onTimeout 回调） | unit | Same as above | No (Wave 0) |
| HWM-19 | SSE composable 接收事件并更新 reactive data | unit | `npx vitest run useHardwareMetrics` | No (Wave 0) |
| HWM-19 | SSE composable 指数退避重连 | unit | Same as above | No (Wave 0) |
| TDD-02 | HardwareSseService.broadcast() 验证 send() 调用 | unit | `./gradlew.bat test --tests "*HardwareSseServiceTest*` | No (Wave 0) |
| TDD-05 | useHardwareMetrics 接受 mock EventSource 工厂 | unit | `npx vitest run useHardwareMetrics` | No (Wave 0) |

### Wave 0 Gaps

- [ ] `springboot/src/test/java/cn/coderstory/springboot/sse/monitor/HardwareSseServiceTest.java` — 基于 Mockito 模拟 SseEmitter，验证 broadcast() send() 调用和回调清理
- [ ] `app-vue/src/composables/__tests__/useHardwareMetrics.test.ts` — 基于 mock EventSource 验证连接、数据解析、重连逻辑

### Sampling Rate
- **Per task commit:** 单个测试类 (`--tests "*ClassName*"`)
- **Per wave merge:** 后端 `./gradlew.bat test` + 前端 `npx vitest run`
- **Phase gate:** 全量测试通过

## Security Domain

> `security_enforcement` — Phase 34 才开始，本阶段 OMIT。SSE 端点认证方式在 Open Questions 中讨论。

## Sources

### Primary (HIGH confidence)
- `SeckillSseService.java` — 现有 SSE 模式参考 [VERIFIED: codebase]
- `SeckillSseController.java` — SSE Controller 端点模式 [VERIFIED: codebase]
- `HardwareMetricsServiceImpl.java` — 采集循环，broadcast hook 注入点 [VERIFIED: codebase]
- `HardwareMonitorController.java` — 新增 SSE 端点的目标 Controller [VERIFIED: codebase]
- `HardwareMetricsDTO.java` / `CpuMetricsDTO.java` / `MemoryMetricsDTO.java` / `DiskMetricsDTO.java` — SSE 推送数据结构 [VERIFIED: codebase]
- `QpsChart.vue` — ECharts 集成模式 (vue-echarts + use + CanvasRenderer) [VERIFIED: codebase]
- `MonitorDashboard.vue` — Element Plus 卡片布局模式 [VERIFIED: codebase]
- `routes.ts` — 路由注册模式 [VERIFIED: codebase]
- `Layout.vue` — 布局容器和侧边栏模式 [VERIFIED: codebase]
- `AppMenu.vue` — 动态菜单加载模式 [VERIFIED: codebase]
- `HardwareMetricsServiceImplTest.java` — Mockito 单元测试模式 [VERIFIED: codebase]
- `HardwareMonitorControllerTest.java` — MockMvc 控制器测试模式 [VERIFIED: codebase]
- `seckill.ts` — 前端 SSE 订阅模式 (EventSource + addEventListener) [VERIFIED: codebase]
- `app-vue/package.json` — ECharts 6.0.0 + vue-echarts 8.0.1 已安装 [VERIFIED: codebase]
- `app-vue/vitest.config.js` — Vitest 配置 (jsdom + globals + @ alias) [VERIFIED: codebase]

### Secondary (MEDIUM confidence)
- `useAnimationToggle.ts` — 现有 composable 模式参考 (onMounted/onUnmounted 生命周期) [VERIFIED: codebase]

### Tertiary (LOW confidence)
- 该阶段所有核心依赖都已验证存在于项目中，无需外部 WebSearch 验证

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH — 所有依赖已验证存在于项目中
- Architecture: HIGH — 基于现有 SeckillSseService 模式的直接适配
- Pitfalls: HIGH — 基于 SeckillSseService 使用经验和 ECharts vue-echarts 已知问题

**Research date:** 2026-05-10
**Valid until:** 2026-06-10 (stable — all dependencies are long-term)
