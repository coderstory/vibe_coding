# Architecture Patterns: Windows Hardware Load Monitoring

**Project:** Vue + Spring Boot Admin System
**Domain:** Windows server hardware resource monitoring (CPU, memory, disk, network)
**Researched:** 2026-05-09
**Mode:** Ecosystem + Feasibility

## Recommended Architecture

### Overview

The Windows hardware monitoring feature follows the existing domain-package pattern and SSE real-time push pattern already established in the seckill system. A new `monitor/hardware` sub-domain is added on the backend. A new `views/monitor/hardware/` page set goes on the frontend.

```
Browser (Vue 3)
    |
    |-- GET /api/monitor/hardware/metrics          (polling fallback)
    |-- GET /api/monitor/hardware/subscribe        (SSE real-time stream)
    |
    v
Spring Boot Backend
    |
    |-- controller/monitor/hardware/HardwareMonitorController
    |       |
    |       |-- service/monitor/hardware/HardwareMetricsService
    |       |       |-- OSHI SystemInfo (singleton, JNA-based)
    |       |       |-- @Scheduled sampling loop (2s interval)
    |       |       |-- in-memory ring buffer for trend history
    |       |
    |       |-- sse/monitor/HardwareSseService
    |               |-- ConcurrentHashMap<String, SseEmitter>
    |               |-- Broadcast to all connected clients
    |
    |-- OSHI 6.13.x (or 7.1.x) Library
            |-- CentralProcessor  (CPU load, ticks, frequency, per-core)
            |-- GlobalMemory      (physical/virtual memory, swap)
            |-- HWDiskStore       (disk read/write bytes, queue length)
            |-- NetworkIF         (network throughput, packets, errors)
```

### Why SSE over WebSocket

| Criteria | SSE | WebSocket | Verdict |
|----------|-----|-----------|---------|
| Direction | Server -> Client only | Full-duplex | SSE sufficient (monitoring is server push only) |
| Existing pattern | Already used in seckill (`SeckillSseService`) | Not used anywhere in project | SSE reuses proven pattern |
| Auto-reconnect | Native browser support | Manual implementation | SSE wins |
| Protocol complexity | HTTP (standard, no upgrade) | WS (separate protocol, upgrade handshake) | SSE simpler to proxy/firewall |
| Binary data | Text only | Binary supported | Not needed for JSON metrics |
| Concurrent connections | Browser limit ~6 per domain | No browser limit | Not a concern at our scale (<10 users) |

**Conclusion:** SSE is the right choice. The existing `SeckillSseService` is a direct template for `HardwareSseService`.

### Component Boundaries

| Component | Responsibility | Communicates With |
|-----------|---------------|-------------------|
| `HardwareMonitorController` | REST + SSE endpoints for hardware metrics | `HardwareMetricsService`, `HardwareSseService` |
| `HardwareMetricsService` | OSHI data collection, metrics aggregation, history buffer | OSHI `SystemInfo` singleton |
| `HardwareSseService` | SSE connection management, broadcast push to all clients | `HardwareMetricsService` (gets metrics), `SseEmitter` (sends to clients) |
| `MonitoringScheduler` | `@Scheduled(fixedRate=2000)` sampling loop | `HardwareMetricsService`, `HardwareSseService` |
| `HardwareMonitorPage.vue` | Main monitoring page with layout | Child gauge/chart components |
| `CpuGauge.vue` | CPU usage gauge + per-core breakdown | ECharts gauge + bar chart |
| `MemoryGauge.vue` | Memory usage gauge (physical + virtual) | ECharts gauge |
| `DiskGauge.vue` | Disk I/O read/write rates | ECharts gauge + line chart |
| `NetworkChart.vue` | Network throughput (in/out) | ECharts line chart |
| `MetricTrendChart.vue` | Time-series chart for historical trends | ECharts line chart (reuses QpsChart pattern) |

### Data Flow

#### Polling Mode (fallback — initial page load)

```
Frontend                          Backend
   |                                 |
   |-- GET /api/monitor/hardware     |
   |   /metrics                      |
   |                               HardwareMetricsService
   |                                  |-- OSHI snapshot (reads cached metrics)
   |<-- { cpu, mem, disk, network }  |
```

#### SSE Streaming Mode (primary — real-time updates)

```
Frontend                          Backend                         OSHI
   |                                 |                              |
   |-- GET /api/monitor/hardware     |                              |
   |   /subscribe                    |                              |
   |   (SSE connection)              | HardwareSseService           |
   |                                 |   |-- create SseEmitter      |
   |                                 |   |-- store in ConcurrentHashMap
   |                                 |                              |
   |                                 | MonitoringScheduler          |
   |                                 |   |-- @Scheduled(2000ms)     |
   |                                 |   |-- snapMetrics():         |
   |                                 |       call OSHI              |
   |                                 |       update cache          |
   |                                 |       push to history buf   |
   |                                 |   |-- broadcast():           |
   |<-- event: "metric-update"       |       forEach emitter:      |
   |    data: { cpu, mem, disk,      |         emitter.send(event) |
   |           network, timestamp }  |                              |
   |                                 |                              |
```

#### SSE Reconnection (browser built-in)

```
Browser                                   Backend
   |                                         |
   |-- EventSource connects ----------------->| (creates new SseEmitter)
   |                                         |
   |<-- event: "metric-update" (immediate) --|
   |                                         |
   (network drops)                           |
   |                                         |
   |-- Browser auto-reconnects (3s default)  |
   |   (sends Last-Event-ID header)          |
   |                                         | (creates new SseEmitter)
   |<-- event: "metric-update" --------------|
   |                                         |
```

**Key point:** SSE auto-reconnection happens at the browser level without any frontend code. The `Last-Event-ID` header can be used to resume from the last received event, but for monitoring (where stale data is acceptable), simply reconnecting and receiving the latest snapshot is sufficient.

### Directory Structure Changes

#### Backend (new files)

```
springboot/src/main/java/cn/coderstory/springboot/
+-- controller/monitor/hardware/
|   +-- HardwareMonitorController.java
+-- service/monitor/hardware/
|   +-- HardwareMetricsService.java
+-- sse/monitor/
|   +-- HardwareSseService.java
+-- dto/monitor/hardware/
|   +-- HardwareMetricsDTO.java
|   +-- CpuMetricsDTO.java
|   +-- MemoryMetricsDTO.java
|   +-- DiskMetricsDTO.java
|   +-- NetworkMetricsDTO.java
```

#### Frontend (new files)

```
app-vue/src/
+-- views/monitor/hardware/
|   +-- HardwareMonitorPage.vue       # Main dashboard layout
|   +-- CpuGauge.vue                  # CPU utilization gauge
|   +-- MemoryGauge.vue               # Memory utilization gauge
|   +-- DiskGauge.vue                 # Disk I/O gauge
|   +-- NetworkChart.vue              # Network throughput chart
+-- api/modules/
|   +-- hardware.ts                   # Hardware monitoring API module
+-- composables/
|   +-- useHardwareSse.ts             # SSE connection composable
```

#### Modified files

```
springboot/gradle/libs.versions.toml              # + oshi-version, oshi-core library entry
springboot/build.gradle.kts                       # + implementation(libs.oshi.core)
springboot/src/main/resources/application.yaml     # + monitor.hardware config section
app-vue/src/router/modules/routes.ts               # + /monitor/hardware route entry
```

## Patterns to Follow

### Pattern 1: Domain Package Isolation

**What:** Hardware monitoring gets its own sub-package under `monitor/`, separate from existing seckill metrics monitoring.

**Where:** `controller/monitor/hardware/`, `service/monitor/hardware/`, `dto/monitor/hardware/`

**Why:** The existing `MonitorController` and `MonitorService` handle Redis-based seckill metrics (concurrent count, QPS keys). Hardware monitoring is a separate concern (OS-level CPU/memory/disk via OSHI). The project's existing domain structure (e.g., `seckill`, `rocketmq`, `order`) confirms this pattern.

**Example:**
```java
package cn.coderstory.springboot.controller.monitor.hardware;

@RestController
@RequestMapping("/api/monitor/hardware")
@RequiredArgsConstructor
public class HardwareMonitorController {
    private final HardwareMetricsService metricsService;
    private final HardwareSseService sseService;

    @GetMapping("/metrics")
    public ApiResponse<HardwareMetricsDTO> getMetrics() {
        return ApiResponse.success(metricsService.getCurrentMetrics());
    }

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        return sseService.createEmitter();
    }
}
```

### Pattern 2: SSE Broadcast (Reuse Seckill Pattern)

**What:** The seckill system already has `SeckillSseService` with `ConcurrentHashMap<String, SseEmitter>`. The hardware monitoring SSE follows the same implementation pattern but broadcasts to ALL connected clients instead of per-queue.

**Key differences from SeckillSseService:**

| Aspect | SeckillSseService | HardwareSseService |
|--------|-------------------|-------------------|
| Connection key | `queueId` (per-request) | `UUID` (per-client) |
| Message routing | `sendToQueue(queueId, event, data)` | `broadcast(event, data)` to all |
| Timeout | 300s (bounded) | `0L` (never expire — monitoring stays open) |
| Heartbeat | Per-queue heartbeats | Broadcast heartbeat to all |
| Lifecycle | Ephemeral (seconds) | Persistent (hours/days) |

**Example:**
```java
@Service
@Slf4j
public class HardwareSseService {
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter() {
        String clientId = UUID.randomUUID().toString();
        SseEmitter emitter = new SseEmitter(0L); // Never timeout

        emitter.onCompletion(() -> emitters.remove(clientId));
        emitter.onTimeout(() -> emitters.remove(clientId));
        emitter.onError(e -> {
            log.warn("Hardware SSE client {} error: {}", clientId, e.getMessage());
            emitters.remove(clientId);
        });

        emitters.put(clientId, emitter);

        // Send connected event immediately so client isn't pending
        try {
            emitter.send(SseEmitter.event().name("connected").data("Hardware monitoring connected"));
        } catch (IOException e) {
            emitter.complete();
        }

        log.debug("Hardware SSE connected: {} (total: {})", clientId, emitters.size());
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
                log.debug("Hardware SSE broadcast failed for {}: {}", id, e.getMessage());
                emitters.remove(id);
            }
        });
    }

    public int getActiveConnectionCount() {
        return emitters.size();
    }
}
```

### Pattern 3: OSHI Singleton + @Scheduled Sampling

**What:** `SystemInfo` is instantiated exactly once per application lifetime. A `@Scheduled(fixedRate=2000)` method runs the sampling loop and updates a cached `HardwareMetricsDTO`.

**Why:**
1. OSHI documentation explicitly warns against creating `SystemInfo` per-request — JNA class loading and native library discovery is expensive (100-500ms).
2. CPU load requires two tick samples with a delay between them — periodic sampling is the natural fit.
3. Separate data collection from data serving (single writer, multiple readers pattern).

**Example:**
```java
@Service
@Slf4j
public class HardwareMetricsService {

    // OSHI singleton — instantiated once
    private final SystemInfo systemInfo = new SystemInfo();
    private final HardwareAbstractionLayer hal = systemInfo.getHardware();
    private final CentralProcessor cpu = hal.getProcessor();
    private final GlobalMemory memory = hal.getMemory();

    // Thread-safe cached metrics — volatile for visibility
    private volatile HardwareMetricsDTO currentMetrics;

    // CPU tick history array — needed for delta calculation
    private long[] previousCpuTicks;

    // Network history — needed for throughput calculation
    private final Map<String, NetworkMetricsSnapshot> previousNetworkStats = new ConcurrentHashMap<>();

    // In-memory ring buffer for trend history (last 60 samples = 2 minutes at 2s interval)
    private final RingBuffer<HardwareMetricsDTO> historyBuffer = new RingBuffer<>(60);

    @PostConstruct
    public void init() {
        // Warm up: first call to getSystemCpuLoadTicks() may return -1
        this.previousCpuTicks = cpu.getSystemCpuLoadTicks();
    }

    @Scheduled(fixedRateString = "${monitor.hardware.sampling-interval:2000}")
    public void sample() {
        HardwareMetricsDTO metrics = new HardwareMetricsDTO();
        metrics.setTimestamp(System.currentTimeMillis());
        metrics.setCpu(sampleCpu());
        metrics.setMemory(sampleMemory());
        metrics.setDisks(sampleDisks());
        metrics.setNetwork(sampleNetwork());

        this.currentMetrics = metrics;
        historyBuffer.add(metrics);
    }

    private CpuMetricsDTO sampleCpu() {
        long[] currentTicks = cpu.getSystemCpuLoadTicks();
        double systemLoad = cpu.getSystemCpuLoadBetweenTicks(previousCpuTicks) * 100;
        double[] perCoreLoad = cpu.getProcessorCpuLoadBetweenTicks(previousCpuTicks);

        this.previousCpuTicks = currentTicks;

        CpuMetricsDTO dto = new CpuMetricsDTO();
        dto.setSystemLoad(Math.round(systemLoad * 10.0) / 10.0);
        dto.setPerCoreLoad(ArrayUtils.toList(perCoreLoad));
        dto.setPhysicalCores(cpu.getPhysicalProcessorCount());
        dto.setLogicalCores(cpu.getLogicalProcessorCount());
        return dto;
    }

    // ... similar for memory, disks, network

    public HardwareMetricsDTO getCurrentMetrics() {
        return currentMetrics;
    }

    public List<HardwareMetricsDTO> getHistory() {
        return historyBuffer.snapshot();
    }
}
```

### Pattern 4: Frontend SSE Composable

**What:** A `useHardwareSse` composable encapsulates SSE connection lifecycle, event parsing, and cleanup.

**Why:** Follows the project's Composition API conventions. Keeps SSE logic reusable. Auto-cleans on component unmount via `onUnmounted`.

**Example:**
```typescript
// composables/useHardwareSse.ts
import { onUnmounted, ref } from 'vue'
import type { HardwareMetricsDTO } from '@/api/modules/hardware'

export function useHardwareSse() {
  const connected = ref(false)
  const metrics = ref<HardwareMetricsDTO | null>(null)
  const error = ref<string | null>(null)
  let eventSource: EventSource | null = null

  function connect(url: string = '/api/monitor/hardware/subscribe') {
    if (eventSource) disconnect()

    eventSource = new EventSource(url)

    eventSource.addEventListener('metric-update', (event: MessageEvent) => {
      try {
        metrics.value = JSON.parse(event.data)
        error.value = null
      } catch (e) {
        error.value = '解析监控数据失败'
      }
    })

    eventSource.addEventListener('connected', () => {
      connected.value = true
      error.value = null
    })

    eventSource.onerror = () => {
      connected.value = false
      error.value = 'SSE 连接断开，正在重连...'
      // EventSource auto-reconnects — no manual code needed
    }
  }

  function disconnect() {
    eventSource?.close()
    eventSource = null
    connected.value = false
    error.value = null
  }

  onUnmounted(disconnect)

  return { connect, disconnect, connected, metrics, error }
}
```

### Pattern 5: ECharts Gauge + Line Chart (Reuse QpsChart Pattern)

**What:** The project already uses ECharts 6.x + vue-echarts 8.x in the RocketMQ dashboard (`QpsChart.vue`). Hardware monitoring adds gauge charts for current utilization and line charts for trends.

**Example (CpuGauge.vue):**
```vue
<script lang="ts">
/**
 * CPU 利用率仪表盘组件。以仪表盘形式展示当前 CPU 使用率，附带每个逻辑核心的负载柱状图。
 */
</script>

<script lang="ts" setup>
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { GaugeChart, BarChart } from 'echarts/charts'
import { computed } from 'vue'

use([CanvasRenderer, GaugeChart, BarChart])

const props = defineProps<{
  /** CPU 整体利用率 0-100 */
  systemLoad: number
  /** 每个逻辑核心的利用率数组 */
  perCoreLoad: number[]
}>()

const gaugeOption = computed(() => ({
  series: [{
    type: 'gauge',
    startAngle: 90,
    endAngle: -270,
    max: 100,
    pointer: { show: true },
    progress: {
      show: true,
      width: 8
    },
    axisLine: {
      lineStyle: { width: 8 }
    },
    axisLabel: { show: false },
    detail: {
      formatter: '{value}%',
      fontSize: 24
    },
    data: [{ value: props.systemLoad }]
  }]
}))
</script>

<template>
  <v-chart :option="gaugeOption" autoresize style="height: 250px" />
</template>
```

### Pattern 6: In-Memory Ring Buffer for Trend History

**What:** A fixed-size circular buffer storing the last N metric samples for trend display.

**Why:** No database needed for short-term history (2 minutes at 2s intervals = 60 samples). The ring buffer is lock-free for the single writer, or uses `ReentrantReadWriteLock` for the snapshot method.

**Example:**
```java
@Component
public class RingBuffer<T> {
    private final T[] buffer;
    private final int capacity;
    private int head = 0;
    private int count = 0;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @SuppressWarnings("unchecked")
    public RingBuffer(@Value("${monitor.hardware.history-size:60}") int capacity) {
        this.capacity = capacity;
        this.buffer = (T[]) new Object[capacity];
    }

    public void add(T item) {
        lock.writeLock().lock();
        try {
            buffer[head] = item;
            head = (head + 1) % capacity;
            if (count < capacity) count++;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<T> snapshot() {
        lock.readLock().lock();
        try {
            List<T> result = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                int idx = (head - count + i + capacity) % capacity;
                result.add(buffer[idx]);
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }
}
```

## Anti-Patterns to Avoid

### Anti-Pattern 1: Creating SystemInfo Per-Request

**What:** Instantiating `new SystemInfo()` on every REST API call or in every controller method.

**Why bad:** OSHI initialization performs JNA class loading, native library discovery, and Windows COM initialization. This takes 100-500ms per call and can cause resource leaks.

**Instead:** Create `SystemInfo` once as a `private final` field in `HardwareMetricsService`. This follows the "initialize once, reuse forever" pattern from OSHI's own documentation.

**Detection:** Look for `new SystemInfo()` outside of `@PostConstruct` or constructor.

### Anti-Pattern 2: Running wmic/typeperf via Runtime.exec()

**What:** Running `Runtime.getRuntime().exec("wmic cpu get loadpercentage")` or `typeperf` commands.

**Why bad:** Each shell exec spawns a new process (800ms+ per call at 2s intervals). Output parsing is fragile (locale-dependent formatting). Error handling is poor (what if wmic is not in PATH?). This puts unnecessary load on the WMI provider.

**Instead:** Use OSHI, which calls PDH (Performance Data Helper) API directly via JNA (~10ms per call) with proper error handling.

**Detection:** Search for `Runtime.exec`, `ProcessBuilder`, `wmic`, or `typeperf` in code.

### Anti-Pattern 3: Mixing Seckill Metrics and Hardware Metrics

**What:** Adding hardware monitoring methods to the existing `MonitorService` or the existing `/api/monitor/metrics` endpoint.

**Why bad:** The existing `MonitorService` fetches **Redis-based seckill metrics** (concurrent count, QPS keys). Hardware monitoring manages **OS-level metrics** (CPU, memory, disk) via OSHI. These are completely different:
- Different data sources (Redis vs native JNA)
- Different refresh rates (event-driven vs 2s fixed-rate)
- Different consumers (seckill operators vs infrastructure admins)

**Instead:** Create `HardwareMetricsService` under `service/monitor/hardware/` and expose via `/api/monitor/hardware/metrics`.

### Anti-Pattern 4: Per-User SSE Routing for Broadcast Data

**What:** Mapping SSE connections to specific users and only sending events addressed to that user.

**Why bad:** Hardware metrics are system-wide and identical for all viewers. Per-user routing adds needless complexity (user-to-emitter map, auth check on every broadcast).

**Instead:** Use a simple broadcast pattern — all SSE connections receive identical `metric-update` events. Authenticate at connection time (the JWT filter already handles this).

### Anti-Pattern 5: Polling as Primary Data Channel

**What:** Frontend polls `GET /api/monitor/hardware/metrics` every 2 seconds instead of using SSE.

**Why bad:** Each poll is a full HTTP request/response with headers, authentication filter chain, serialization, and deserialization. At 2s intervals with 10 clients, that is 5 requests/second just for no-op overhead.

**Instead:** Use SSE for real-time streaming. Keep the REST endpoint only for:
1. Initial page load (set initial state before SSE connects)
2. Clients that do not support SSE (rare, but possible in restricted browsers)
3. Diagnostic/debugging via curl

### Anti-Pattern 6: Storing History in Database

**What:** Writing every metric sample (every 2 seconds) to a MySQL table via MyBatis Plus.

**Why bad:** At 2s intervals, that is 43,200 rows/day per metric. MySQL is not optimized for time-series writes at this frequency. The existing schema would need a new migration, and the database is already serving the admin system's transactional data.

**Instead:** Use an in-memory ring buffer for short-term history (2 minutes). If long-term history is needed later, add a dedicated time-series store (InfluxDB, Prometheus, or a separate metrics table with downsampling).

## Scalability Considerations

| Concern | At 1-10 users | At 100 users | At 1000+ users |
|---------|--------------|--------------|----------------|
| **SSE connections** | Direct SseEmitter per connection (trivial) | Tomcat thread pool needs tuning (`server.tomcat.max-threads=200`) | Need WebFlux for reactive SSE or separate push server |
| **OSHI sampling overhead** | Single `@Scheduled` thread, ~10-50ms per call at 2s interval | Same — sampling cost is independent of user count. 0.5-2.5% CPU overhead | Consider dedicated monitoring agent (decoupled from admin server) |
| **History storage** | In-memory ring buffer (60 entries, ~100KB) | Same — buffer is fixed-size regardless of users | Export to InfluxDB or Prometheus for long-term queries |
| **SSE broadcast cost** | Iterating ~10 emitters, negligible | Iterating ~100 emitters, sub-millisecond | Need event bus (Redis pub/sub) for multi-instance fan-out |

### Recommendations by Scale

**Current scale (single instance, <10 concurrent users):**
- Direct `ConcurrentHashMap<String, SseEmitter>`
- In-memory ring buffer (60 samples = 2 minutes history)
- OSHI sampling in the same JVM
- No changes to infrastructure

**Future scale (2+ instances, load-balanced):**
- Redis pub/sub for SSE fan-out (so any instance can push to any client)
- Dedicated `@Async` event publisher to avoid blocking the sampling thread
- Refactor sampling to a separate thread pool

**Future scale (1000+ concurrent connections):**
- Separate monitoring agent service (decouples OSHI from admin server)
- WebFlux-based SSE for non-blocking I/O
- Prometheus + Grafana for long-term dashboards (the admin page becomes a thin viewer)

## Integration Points

### Backend

| Point | What Changes | Risk | Effort |
|-------|-------------|------|--------|
| `libs.versions.toml` | Add `oshi-core` version entry | Low | 1 line |
| `build.gradle.kts` | Add `implementation(libs.oshi.core)` | Low | 1 line |
| `application.yaml` (or `business.yaml`) | Add `monitor.hardware.sampling-interval` config | Low | 5 lines |
| `build.gradle.kts` | Add `@EnableScheduling` to config or main class | Low | 1 annotation |

### Frontend

| Point | What Changes | Risk | Effort |
|-------|-------------|------|--------|
| `routes.ts` | Add route entry under `/monitor/hardware` | Low | 8 lines |
| Sidebar/menu config | Add menu item linking to hardware page | Low | 5 lines |
| `package.json` | ECharts already installed, no change needed | None | 0 lines |

### No Changes Needed

| What | Why |
|------|-----|
| **SecurityConfig** | New endpoints use existing JWT auth filter; no new permit rules needed |
| **Database schema** | No new tables (metrics are transient, stored in-memory) |
| **Flyway migration** | No schema changes |
| **Existing MonitorController/MonitorService** | Left untouched — seckill metrics remain separate |
| **MonitorDashboard.vue** | Left untouched — or optionally add a link to the new hardware page |
| **CORS config** | Already configured for all `/api/**` paths |

## Suggested Implementation Order

### Phase 1: Backend Service + REST API (2-3 hours)

1. Add `oshi-core` dependency to `libs.versions.toml`:
   ```toml
   [versions]
   oshi = "6.13.0"

   [libraries]
   oshi-core = { module = "com.github.oshi:oshi-core", version.ref = "oshi" }
   ```
2. Create DTOs: `HardwareMetricsDTO`, `CpuMetricsDTO`, `MemoryMetricsDTO`, `DiskMetricsDTO`, `NetworkMetricsDTO`
3. Create `HardwareMetricsService` with OSHI singleton, `@Scheduled` sampling, and in-memory cache
4. Create `HardwareMonitorController` with `GET /api/monitor/hardware/metrics`
5. Add `@EnableScheduling` to application main class
6. Verify: `curl http://localhost:8080/api/monitor/hardware/metrics` returns JSON

### Phase 2: SSE Streaming (1-2 hours)

1. Create `HardwareSseService` modeled on existing `SeckillSseService`
2. Add SSE endpoint to `HardwareMonitorController`
3. Modify `HardwareMetricsService` (or a dedicated scheduler bean) to call `sseService.broadcast()` after each sample
4. Verify: `curl -N http://localhost:8080/api/monitor/hardware/subscribe` streams events

### Phase 3: Frontend Page (3-4 hours)

1. Create `useHardwareSse` composable
2. Create `api/modules/hardware.ts`
3. Create gauge components: `CpuGauge.vue`, `MemoryGauge.vue`, `DiskGauge.vue`
4. Create `NetworkChart.vue`
5. Create `HardwareMonitorPage.vue` with 2x2 grid layout (CPU, Memory, Disk, Network)
6. Add route in `routes.ts`
7. Add sidebar menu entry

### Phase 4: Polish (1-2 hours)

1. Add loading state while SSE connects
2. Add reconnection status indicator
3. Add trend history chart (past 2 minutes of data)
4. Add per-core CPU breakdown to CpuGauge
5. Handle SSE disconnection gracefully (show stale data indicator)

## Sources

- OSHI 7.1.0 GitHub Releases: https://github.com/oshi/oshi/releases (HIGH confidence, official releases page, accessed 2026-05-09)
- OSHI Official Site: https://www.oshi.ooo/ (HIGH confidence, official project site)
- OSHI Windows Implementation DeepWiki: https://deepwiki.com/oshi/oshi/4.1-windows-implementation (MEDIUM confidence, community analysis of OSHI internals)
- Maven Central OSHI versions (libraries.io): https://libraries.io/maven/com.github.oshi:oshi-core (MEDIUM confidence, package registry data)
- OSHI FFM module plans (Issue #3123): https://github.com/oshi/oshi/issues/3123 (MEDIUM confidence, GitHub issue with plans)
- Spring SSE best practices (dev.to): https://dev.to/sadiul_hakim/server-sent-event-133o (MEDIUM confidence, community article)
- vue-echarts: https://github.com/ecomfe/vue-echarts (HIGH confidence, official GitHub repo)
- Existing project code: `SeckillSseService.java`, `MonitorController.java`, `QpsChart.vue` (HIGH confidence, verified in codebase)
