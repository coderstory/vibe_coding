# Domain Pitfalls: Windows Hardware Load Monitoring

**Domain:** Spring Boot admin system real-time hardware monitoring on Windows
**Researched:** 2026-05-09
**Target system:** Spring Boot 4.1.0-RC1 + JDK 26 + Vue 3 + Windows host
**Project:** Adding hardware load monitoring page to existing admin system

---

## Critical Pitfalls

Mistakes that completely block the feature or cause production incidents.

### Pitfall 1: Using OSHI 6.x (`oshi-core`) on JDK 26 -- JNA Will Be Blocked

**What goes wrong:** The application fails at runtime with `IllegalCallerException` or JNI restriction errors. OSHI cannot collect any hardware metrics.

**Why it happens:** JDK 24+ (JEP 472) introduced restricted native access with warnings. JDK 26 is expected to make these restrictions **errors by default** (denying JNI calls without explicit opt-in). OSHI 6.x (`oshi-core`) depends on JNA, which relies on JNI under the hood. The project already uses JDK 26 (`java.toolchain.languageVersion.set(JavaLanguageVersion.of(26))`), with `--enable-preview` enabled for the compiler.

**Consequences:**
- OSHI cannot initialize on JDK 26 -- all hardware metrics return zeros or throw exceptions
- Workaround `--enable-native-access=ALL-UNNAMED` may not work in JDK 26 (depends on final JEP 472 implementation)
- If caught late in the project, requires a full OSHI version upgrade and API migration
- Affects ALL data collection: CPU, memory, disk, network

**Prevention:**
- Use **OSHI 7.0+** with `oshi-core-ffm` module (FFM API, no JNA dependency)
- `oshi-core-ffm` requires JDK 25+ -- compatible with the project's JDK 26
- Entry point becomes `oshi.ffm.SystemInfo` instead of `oshi.SystemInfo` (hardware/OS API classes remain the same, import changes only)
- Do NOT add `oshi-core` (JNA version) -- add `oshi-core-ffm` to `libs.versions.toml`:
  ```toml
  [versions]
  oshi = "7.1.0"

  [libraries]
  oshi-core-ffm = { module = "com.github.oshi:oshi-core-ffm", version.ref = "oshi" }
  ```
- Add in `build.gradle.kts`: `implementation(libs.oshi.core.ffm)`

**Detection:** Write a unit test that instantiates `SystemInfo` and calls `getHardware()`. Run on JDK 26 without `--enable-native-access`. If it throws, the wrong OSHI module is used.

---

### Pitfall 2: CPU Ticks Return All Zeros for Non-Admin Process Users

**What goes wrong:** `processor.getSystemCpuLoadTicks()` returns all zeros. The monitoring dashboard shows 0% CPU usage permanently. No error is logged.

**Why it happens:** Since OSHI 5.3.5, processor group awareness (>64 cores) changed the data source from the `"Processor"` performance counter group to the `"ProcessorInformation"` group. The new counters require permissions that **default Windows service users do not have**. The Spring Boot process runs under a non-admin account (e.g., `NETWORK SERVICE`, `LOCAL SERVICE`, or a custom service account).

**Consequences:**
- CPU metrics silently return 0 -- no error logs are produced (hardest debugging scenario)
- Developers may waste hours debugging thinking the collection logic is wrong
- Production monitoring is blind to CPU load spikes
- The bug is only discovered in production, because dev machines run under admin

**Prevention:**
- Add the Windows user running Spring Boot to the **"Performance Monitor Users"** local group
  ```
  Computer Management -> System Tools -> Local Users and Groups -> Groups ->
  Performance Monitor Users -> Add -> [user running the service]
  ```
- OR set OSHI config **before** creating `SystemInfo` (OSHI 6.6.3+):
  ```java
  GlobalConfig.set(GlobalConfig.OSHI_OS_WINDOWS_LEGACY_SYSTEM_COUNTERS, true);
  ```
- Document this as a deployment prerequisite in the monitoring feature's README

**Detection:** Write an integration test that calls `processor.getSystemCpuLoadTicks()` and asserts non-zero values after 2 seconds. Log raw tick values at startup: `log.info("CPU ticks: idle={}, user={}, system={}", ticks[0], ticks[1], ticks[2])`.

---

### Pitfall 3: SSE Connection Resource Leak from Unclosed Monitoring Emitters

**What goes wrong:** Accumulated `SseEmitter` objects grow unbounded in memory, eventually causing `OutOfMemoryError`. The server stops responding to any requests.

**Why it happens:** The project has an existing `SeckillSseService` with a `ConcurrentHashMap<String, SseEmitter>` emitter registry for seckill notifications. When extending this pattern for hardware monitoring:
- The monitoring page opens a persistent SSE connection
- When the user navigates away or closes the browser tab, `SseEmitter.onCompletion()` / `onError()` are **not reliably triggered** (known Spring Servlet API limitation)
- The `ConcurrentHashMap` entry leaks
- Spring + Jetty has a known issue (#32629) where `HttpConnection` objects are not cleaned up after SSE client disconnect, leading to heap memory exhaustion
- Messages accumulate in `earlySendAttempts` if the emitter initialization never completes (Spring Issue #33340)

**Consequences:**
- Memory grows linearly with each disconnected monitoring session
- If the monitoring page auto-reconnects (which most SSE clients do), each reconnect adds a new emitter without removing the old one
- After 1-2 hours of active use with tab switching, server runs out of heap memory
- Server restart is required to recover

**Prevention:**
- Do NOT reuse the seckill SSE pattern (5-minute timeout) for monitoring. Set a **30-60 second timeout** on monitoring SSE emitters.
- Implement **heartbeat pings** every 10 seconds. If `emitter.send()` throws `IOException`, remove the emitter from the registry.
- Catch `IOException` on every `send()` call:
  ```java
  try {
      emitter.send(event);
  } catch (IOException e) {
      emitters.remove(queueId);
      emitter.completeWithError(e);
  }
  ```
- Add a **periodic sweep** `@Scheduled` task that checks for stale emitters (registered but timed out).
- Frontend must call `/api/monitor/unsubscribe` in `onUnmounted()`.
- Limit max concurrent SSE connections with a configurable cap:
  ```java
  if (emitters.size() >= maxConnections) {
      throw new BusinessException("Too many monitoring connections");
  }
  ```
- Register the usual `onCompletion`/`onTimeout`/`onError` callbacks that always call `emitter.complete()` + remove from map.

**Detection:** Expose an endpoint `GET /api/monitor/sse/status` returning `{ activeConnections: N }`. If N grows without bound during normal user navigation, there is a leak. Also expose the metric via Micrometer if available.

---

### Pitfall 4: `SystemInfo` Singleton Thread Safety -- Data Races and Intermittent NPEs

**What goes wrong:** Intermittent `NullPointerException`, stale metric values, or unexpected CPU spikes in the collection thread. Behavior is non-deterministic and hard to reproduce.

**Why it happens:** OSHI's `SystemInfo` is **not officially thread-safe** (confirmed in GitHub issues #797 and #660). Lazy-initialized fields like `SystemInfo.hardware`, `CentralProcessor`, and `GlobalMemory` are initialized without synchronization. In a Spring Boot context:
- A `@Scheduled` task collects metrics every 1 second
- A REST API handler calls `hal.getMemory()` on the request thread
- An SSE push thread reads the same instance concurrently
- Result: data races, NPEs from observing `null` during init, repeated expensive WMI calls from cache expiry races

**Consequences:**
- Intermittent failures that only reproduce under load
- The monitoring dashboard shows random zero values
- CPU load on the server increases due to repeated failed cache reads re-triggering WMI calls
- Hard to debug because stepping through with a debugger changes the timing

**Prevention -- use the "dedicated collector thread" pattern:**
- Do NOT share a `SystemInfo` instance across threads
- Create a single background thread that collects all metrics into a thread-safe cache
- All other threads (SSE, REST) read from the cache only, never touching OSHI
- Pattern:
  ```java
  @Component
  public class HardwareMetricsCollector {
      private final SystemInfo systemInfo = new SystemInfo();
      private volatile MonitorSnapshot snapshot = MonitorSnapshot.empty();

      @PostConstruct
      public void start() {
          // Warm up: trigger COM init, collect first snapshot
          collect();
      }

      @Scheduled(fixedRate = 1000)
      public void collect() {
          HardwareAbstractionLayer hal = systemInfo.getHardware();
          // ... collect and store in snapshot
          this.snapshot = new MonitorSnapshot(...);
      }

      public MonitorSnapshot getSnapshot() { return snapshot; }
  }
  ```
- Use `volatile` or `AtomicReference` for the snapshot reference (single writer, multiple readers)

**Detection:** Write a stress test that calls `/api/monitor/hardware` from 10 concurrent threads for 10,000 iterations. Log all NPEs or zero values. If none appear, the caching pattern is working.

---

### Pitfall 5: CPU Sampling Interval Too Short -- 0% Load or Wildly Inaccurate Values

**What goes wrong:** The CPU load chart shows 0% most of the time, or values jump erratically and never match Windows Task Manager.

**Why it happens:** Three independent issues combine:
1. **Tick granularity:** On Windows, CPU tick counters increment only every 1/64 second (~15.6ms) per logical processor. With 8 logical processors and 3 tick counters, the cumulative error can reach 375ms.
2. **OSHI memoizer cache:** CPU tick data is cached for **300ms** by default (`OSHI_UTIL_MEMOIZER_EXPIRATION`). If the polling interval is <= 300ms, `getSystemCpuLoadBetweenTicks()` sees zero delta and returns 0%.
3. **Cumulative vs interval trap:** The first call to `getSystemCpuLoadBetweenTicks()` returns -1.0 (no prior delta to compare against).

**Consequences:**
- CPU monitoring is useless -- users see a flat line at 0%
- Developers increase polling frequency to "fix it," making the problem worse
- Every comparison to Windows Task Manager is apples-to-oranges, causing endless confusion

**Prevention:**
- Set polling interval to **at least 1 second** for system CPU load
- Configure memoizer expiration to be shorter than the polling interval:
  ```java
  GlobalConfig.set(GlobalConfig.OSHI_UTIL_MEMOIZER_EXPIRATION, 500); // must be < polling interval
  ```
- Discard the first `getSystemCpuLoadBetweenTicks()` result (returns -1.0 or 0)
- Do NOT zero-out the `prevTicks` array -- reuse the previous snapshot:
  ```java
  // CORRECT
  prevTicks = processor.getSystemCpuLoadTicks();
  // ... wait 1 second ...
  ticks = processor.getSystemCpuLoadTicks();
  cpuLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks);
  prevTicks = ticks; // reuse, do NOT create new long[][]
  ```
- For matching Windows Task Manager display, divide by `processor.getLogicalProcessorCount()`:
  ```java
  double taskManagerLoad = cpuLoad / processor.getLogicalProcessorCount() * 100;
  ```

**Detection:** Log `(cpuLoad, tickDelta, interval)` on each collection. If `tickDelta` is 0 but `interval` > 300ms, the memoizer expiration is too long. If `cpuLoad` is consistently 0, check tick source permissions (Pitfall 2).

---

### Pitfall 6: Windows Disk/Network Queries Block the Event Loop

**What goes wrong:** The SSE push to the monitoring dashboard pauses for 500ms-3000ms. Heartbeats stop arriving. The frontend assumes disconnection and reconnects, creating duplicate connections and compounding the issue.

**Why it happens:** OSHI's `hal.getDiskStores()` and `hal.getNetworkIFs()` trigger WMI queries on Windows. WMI:
- Requires COM initialization on first call: **10-50 seconds**
- Per-query overhead: **3-3000ms** depending on the counter
- Network `updateAttributes()` triggers a fresh WMI call each time

If these calls execute on the SSE push thread or the request thread, the entire thread blocks. With a thread-per-connection model (default Tomcat), all threads can be consumed.

**Consequences:**
- SSE push pauses mid-stream, heartbeat stops
- Clients detect silence, close connection, reconnect
- Duplicate emitters accumulate (see Pitfall 3)
- Server enters a death spiral: thread exhaustion -> connection drops -> reconnection -> more threads
- The Spring Boot scheduling thread pool is drained, delaying all other `@Scheduled` tasks

**Prevention:**
- **Strictly separate data collection from data delivery:**
  - Single background collector thread: calls OSHI APIs every 1-2s, stores result in `volatile` DTO
  - SSE push thread: reads from cached DTO only, never touches OSHI
  - REST endpoints: read from cached DTO only
- Collect disk and network data at a **lower frequency** (every 5-10 seconds) since these metrics change slowly
- For the first collection (COM init), trigger at application startup via `@PostConstruct`, not on first user request
- Use `NetworkIF.updateAttributes()` sparingly -- it triggers a WMI refresh. Cache network stats between collections.

**Detection:** Profile `hal.getDiskStores()` and `hal.getNetworkIFs()` execution time with `System.nanoTime()` during development on the target Windows machine. Log execution times:
```java
long start = System.nanoTime();
List<HWDiskStore> disks = hal.getDiskStores();
long elapsed = (System.nanoTime() - start) / 1_000_000;
log.debug("Disk collection took {}ms", elapsed);
```
If any collection exceeds 100ms, background caching is mandatory.

---

### Pitfall 7: ECharts Real-Time Chart Causes Browser Memory Exhaustion

**What goes wrong:** The browser tab consumes 1-4GB of memory within 1-2 hours. The monitoring page becomes unresponsive. The browser eventually crashes.

**Why it happens:** In the Vue SPA with real-time chart updates:
1. **ECharts setOption leak:** Each `chart.setOption()` call with data creates new graphic objects without releasing old ones. The `symbol: "circle"` default creates `Shape` objects per data point that accumulate (known ECharts issue #15480).
2. **Vue reactivity overhead:** If the ECharts instance is stored in a Vue `ref`, Vue's reactivity system tracks every mutation. ECharts instances are large objects with circular references.
3. **No dispose on unmount:** If the user navigates away from the monitoring page, the chart is left in memory. A detached DOM node holds references to all its data.
4. **Unbounded data arrays:** Data points accumulate indefinitely in the time series array. 1-second updates for 1 hour = 3600 data points. With 4 metrics (CPU, memory, disk, network), that is 14,400 objects.

**Consequences:**
- Browser tab crashes after prolonged monitoring
- User loses all monitoring context
- Bad product perception: "the monitoring page crashes my browser"

**Prevention:**
- Always call `chart.dispose()` in `onUnmounted()`:
  ```typescript
  onUnmounted(() => {
      chart?.dispose()
      window.removeEventListener('resize', resizeHandler)
  })
  ```
- Use `markRaw()` to exclude the chart instance from Vue reactivity:
  ```typescript
  import { markRaw } from 'vue'
  const chart = markRaw(echarts.init(domRef.value))
  ```
- Use `chart.appendData()` for incremental updates (many data points) instead of `chart.setOption()` (full re-render):
  ```typescript
  // For large datasets, appendData is ~47x faster than setOption
  chart.appendData({ seriesIndex: 0, data: [newPoint] })
  ```
- Set `symbol: 'none'` for line series with >100 data points to avoid shape object accumulation
- Implement a **sliding window** on the frontend -- only keep the last N data points (e.g., 300 = 5 min at 1s):
  ```typescript
  const MAX_POINTS = 300
  timeSeriesData.push(newPoint)
  if (timeSeriesData.length > MAX_POINTS) {
      timeSeriesData.shift()
  }
  ```
- Buffer and throttle updates: batch data points and update at most once per 500ms, even if the SSE pushes every 200ms

---

### Pitfall 8: Security -- Exposing Sensitive System Metrics Without Proper Access Control

**What goes wrong:** The `/api/monitor/hardware` endpoint reveals CPU model, memory config, disk serial numbers, network interfaces, and process list to users who should not have access.

**Why it happens:** The existing security whitelist in `security.yaml` is:
```yaml
whitelist: /api/auth/**,/api/knowledge/files/**,/api/seckill/subscribe/**,/api/seckill/unsubscribe/**
```
If the hardware monitoring endpoint is added to the controller layer but:
- Not explicitly secured with role checking (it falls under `anyRequest().authenticated()` in `SecurityConfig`)
- OR accidentally added to the whitelist (easy to happen during testing)
- OR a low-privilege user can access it (only authenticated, not authorized)

Hardware metrics expose:
- CPU model and architecture (system fingerprinting)
- Disk serial numbers and layout (reconnaissance for infrastructure attacks)
- Network interface configurations (exposes internal IP structure)
- Running processes (information leakage about other users' applications)

**Consequences:**
- Information disclosure vulnerability
- A regular user can see hardware details meant for administrators
- Attackers can use hardware fingerprinting for targeted exploits

**Prevention:**
- Do NOT add `/api/monitor/hardware` to the security whitelist under any circumstances
- Add `@PreAuthorize("hasRole('ADMIN')")` to the monitoring controller:
  ```java
  @RestController
  @RequestMapping("/api/monitor")
  @PreAuthorize("hasRole('ADMIN')")
  public class MonitorHardwareController { ... }
  ```
- OR add a role-based rule in `SecurityConfig`:
  ```java
  .authorizeHttpRequests(auth -> auth
      .requestMatchers("/api/monitor/hardware/**").hasRole("ADMIN")
      .requestMatchers(whitelist).permitAll()
      .anyRequest().authenticated()
  )
  ```
- Filter sensitive fields from the DTO (disk serial numbers, process command lines with arguments)
- Consider binding the SSE monitoring endpoint to `127.0.0.1` only in production:
  ```yaml
  management:
    server:
      port: 8081
      address: 127.0.0.1
  ```

**Detection:** Test three scenarios in CI or during QA:
1. No JWT token -> 401
2. Non-admin JWT token -> 403
3. Admin JWT token -> 200 + valid data

---

### Pitfall 9: OSHI CPU% Does Not Match Windows Task Manager -- User Confusion

**What goes wrong:** Users compare values in the monitoring dashboard with Windows Task Manager. They never match. Users file bugs claiming the monitoring is inaccurate.

**Why it happens:** OSHI and Windows Task Manager measure fundamentally different things:

| Source | Metric | Scale | Range |
|--------|--------|-------|-------|
| OSHI system CPU | % Processor Time (time not idle) | Per logical processor | 0-100% per proc |
| Windows Task Manager | % Processor Utility (work done / nominal perf) | System-wide | 0-100% (can exceed with turbo) |
| OSHI process CPU | Unix-style, raw tick delta | Not scaled | 0% to N*100% (multithreaded) |
| Task Manager process CPU | Processor Utility, scaled | Scaled to logical CPUs | 0-100% |

Additionally, OSHI measures over the sampling interval (e.g., 1 second), while Task Manager shows a smoothed average.

**Consequences:**
- Users lose trust in the monitoring feature
- Developers waste time trying to "fix" the calculation to match Task Manager
- Confusing support tickets and unnecessary debugging

**Prevention:**
- **Scale OSHI output to match Task Manager convention:**
  ```java
  // System CPU: OSHI returns per-processor, scale to system-wide
  double systemCpuPercent = processor.getSystemCpuLoadBetweenTicks(prevTicks, ticks)
      / processor.getLogicalProcessorCount() * 100;
  ```
- Add a tooltip or UI label: "CPU usage is calculated from processor time counters at ~1s intervals. Values may differ slightly from Windows Task Manager."
- Document in the monitoring help section why values differ
- Do NOT try to perfectly match Task Manager -- it uses a different counter source (% Processor Utility vs % Processor Time)

**Detection:** Run the monitoring page side-by-side with Windows Task Manager. Note differences. If OSHI shows >100% for system CPU, the logical-processor scaling is missing. If OSHI shows exactly 0%, it is Pitfall 5 (interval too short) or Pitfall 2 (permissions).

---

## Moderate Pitfalls

### Pitfall 10: CPU Temperature Returns Fixed 27.85 Degrees Celsius

**What goes wrong:** The "CPU Temperature" gauge on the dashboard always shows 27.85C, even under heavy CPU load. Users think the feature is fake.

**Why it happens:** Most Windows consumer hardware does not expose CPU temperature via standard WMI. OSHI's `getSensors().getCpuTemperature()` returns a default value of 27.85C when no sensor data is available. Only Open Hardware Monitor or LibreHardwareMonitor (separate installation) can provide real temperature data.

**Prevention:**
- Remove CPU temperature from the dashboard by default
- Check `sensors.getCpuTemperature().isPresent()` before displaying the gauge
- If temperature is 27.85C or 0 on Windows, show "N/A -- Sensor not available" with a tooltip explaining why
- Optional: document how to install LibreHardwareMonitor for real temperature data

---

### Pitfall 11: WMI COM Initialization 10-50 Second Delay on First Request

**What goes wrong:** The first time any user opens the monitoring dashboard, the page takes 10-50 seconds to show data. The loading spinner times out.

**Why it happens:** OSHI's first WMI query requires COM initialization (Windows Component Object Model), which triggers RPC setup. Subsequent calls are fast (1-5ms), but the first call is extremely slow.

**Prevention:**
- **Eager-init at application startup:** Call `systemInfo.getHardware()` in a `@PostConstruct` or `CommandLineRunner` to trigger COM init during application boot:
  ```java
  @Component
  public class HardwareInitializer {
      private final HardwareMetricsCollector collector;
      @PostConstruct
      public void init() { collector.collect(); } // triggers COM init
  }
  ```
- Use the dedicated collector thread pattern (Pitfall 4 prevention). By the time a user opens the dashboard, the cache already has warm data.
- Show "Initializing hardware sensors..." status in the UI during the first few seconds, then transition to live data.

---

### Pitfall 12: Thread Exhaustion from Multiple Monitoring SSE Connections

**What goes wrong:** Server starts rejecting requests after a few browser tabs open the monitoring dashboard. Tomcat thread pool is drained.

**Why it happens:** SSE connections hold a Tomcat container thread per connection (in the blocking Servlet model, which Spring Boot 4.1 uses with `spring-boot-starter-webmvc`). If each SSE connection's `send()` is called from a thread pool, and all share the same pool, connections multiply: 5 users x 2 browser tabs = 10 threads.

**Consequences:**
- All other API endpoints become slow or unavailable
- New users cannot even load the login page
- Server restart required to recover

**Prevention:**
- Use the **cached DTO pattern**: one background thread collects data, SSE threads only push from cache (no OSHI calls on SSE threads)
- Set a maximum concurrent SSE connection limit configured via `application.yaml`:
  ```yaml
  monitor:
    sse:
      max-connections: 20
  ```
- Spring Boot 4.1 uses virtual threads by default if available (JDK 21+), which helps with thread-per-connection cost, but WMI blocking calls should still not run on SSE threads
- Monitor active connection count and alert if approaching the limit

---

### Pitfall 13: Storing High-Frequency Monitoring Data in MySQL

**What goes wrong:** Adding a `monitor_metrics` table in Flyway and writing every 1-second metric collection to it. After a few hours, the database is overwhelmed with writes. The admin system becomes slow.

**Why it happens:** 1-second collection x 10 metrics = 864,000 rows/day. MySQL is not optimized for high-frequency time-series writes. The admin system's MySQL database is shared with business tables (users, roles, menus, orders).

**Consequences:**
- MySQL CPU usage spikes from constant inserts
- Business queries (user management, order lookup) become slow
- Writes block due to InnoDB redo log pressure
- Flyway migration versioning becomes painful if the monitoring schema evolves

**Prevention:**
- **Do NOT persist raw monitoring data to MySQL.** The `admin_system` database is for business data.
- Use an in-memory ring buffer for short-term history (last N data points):
  ```java
  // Guava or custom
  import com.google.common.collect.EvictingQueue;
  private final EvictingQueue<MonitorSnapshot> history = EvictingQueue.create(3600); // 1 hour at 1s
  ```
- For long-term history, ship metrics to a dedicated time-series database (InfluxDB, Prometheus) or log aggregation system
- If forced to use MySQL, batch writes every 30-60 seconds and add a TTL retention job
- The Flyway migration should only create configuration tables, not metric storage tables

---

### Pitfall 14: Network Interface Display Names Are Unrecognizable

**What goes wrong:** The monitoring dashboard shows "Network: eth5, eth7, Local Area Connection* 11" instead of names the user can identify like "Ethernet" or "Wi-Fi."

**Why it happens:** OSHI reports Windows network adapter names that include index numbers (`eth0`, `eth1`, ...) or GUIDs that change between systems. The display name is the physical adapter model name, not the Windows network name.

**Prevention:**
- Use `NetworkIF.getDisplayName()` for UI display (e.g., "Realtek PCIe GbE Family Controller")
- Use `getIPv4addr()` as a secondary identifier
- Filter out loopback interfaces (`isLoopback() == true`)
- Filter out virtual adapters (Hyper-V, VPN, Docker) by checking display name for keywords: "Hyper-V", "Virtual", "VPN", "Docker", "Bluetooth"
- Let users rename interfaces via configuration if needed

---

## Minor Pitfalls

### Pitfall 15: Process List Collection is Expensive on Windows

`os.getProcesses(0, null)` can take 100-500ms on Windows with many processes. If collected on every SSE push, this blocks the thread.

**Prevention:** Only collect the process list when the user explicitly opens the "Processes" tab. Never include process list in the periodic 1-second snapshot. If periodic process collection is needed, sample at 30-second intervals.

### Pitfall 16: First `getSystemCpuLoadBetweenTicks()` Returns -1

The very first call produces -1 (no prior delta). The frontend should handle this gracefully instead of showing "NaN%" or a broken gauge.

**Prevention:** Initialize `prevTicks` on the first call, skip the first computation, then start computing from the second interval. The first data point will be missing -- that is expected behavior. Document it.

### Pitfall 17: OSHI `getSensors()` Returns Empty on Most Windows Hardware

CPU temperature, fan speed, and voltage sensors are rarely accessible via standard APIs on Windows consumer hardware. The sensor list will be empty.

**Prevention:** Always check `sensors.getCpuTemperature().isPresent()` before showing temperature. Do not create empty sensor cards. Hide the sensors section entirely if no data is available.

### Pitfall 18: Gradle Dependency Resolution -- `oshi-core-ffm` Requires Careful Versioning

The project uses `libs.versions.toml` for dependency management. Adding `oshi-core-ffm` requires attention: it is not in the Spring Boot BOM, so its version must be explicitly managed.

**Prevention:** Add to `libs.versions.toml` following the existing pattern for non-BOM dependencies (like JJWT):
```toml
[versions]
oshi = "7.1.0"

[libraries]
oshi-core-ffm = { module = "com.github.oshi:oshi-core-ffm", version.ref = "oshi" }
```
Run `./gradlew.bat build --refresh-dependencies` after adding.

### Pitfall 19: Frontend Router Guard Blocks Monitoring Page Access

If the monitoring route is added under `role: ['ADMIN']` but the frontend router's `beforeEach` guard does not handle role-based redirection properly, admin users may be stuck in a redirect loop or see a blank page.

**Prevention:** Follow the existing routing pattern in `router/modules/` -- ensure the monitoring route uses the same role guard pattern as the existing pages. Test with both admin and non-admin roles.

### Pitfall 20: Monitoring Page Refresh During SSE Connection

If the user refreshes the browser on the monitoring page, the SSE connection drops and a new one is created. The old emitter may not be cleaned up if the frontend does not call `unsubscribe` before page unload.

**Prevention:** Use `window.addEventListener('beforeunload', () => sse.close())` in addition to `onUnmounted()`. The SSE close triggers the server-side `onCompletion` callback in some (but not all) cases.

---

## Phase-Specific Warnings

| Phase Topic | Likely Pitfall | Mitigation |
|-------------|---------------|------------|
| OSHI dependency integration (Phase 1) | Using `oshi-core` instead of `oshi-core-ffm` on JDK 26 | Verify FFM module is used. CI assertion: classpath must not contain `oshi-core` (JNA). |
| SSE infrastructure extension (Phase 1) | SSE emitter leak from monitoring page navigation | Extend existing `SeckillSseService` with heartbeat + timeout sweep for monitoring. Use 30s timeout, not 5min. |
| Data collection scheduling (Phase 2) | `@Scheduled` blocking on WMI call | Dedicated collector thread + volatile DTO cache. Never call OSHI in request threads. |
| CPU load calculation (Phase 2) | Tick interval < 300ms causing 0% load | Set polling >= 1s. Configure memoizer expiration to 500ms. Discard first result. |
| Disk/network metrics (Phase 2) | Blocking event loop with WMI disk queries | Collect disk/network at lower frequency (5-10s). COM init at startup. |
| Frontend chart component (Phase 3) | ECharts memory leak from improper disposal | `markRaw()`, `dispose()`, `symbol: 'none'`, sliding window 300 points max. |
| Security hardening (Phase 4) | Hardware API exposed to non-admin users | `@PreAuthorize("hasRole('ADMIN')")` on monitoring controller. Not in whitelist. |
| Historical data storage (Phase 4) | High-frequency writes to MySQL database | In-memory ring buffer for history. Do not persist raw metrics to admin_system. |

---

## Pitfall-to-Phase Mapping

| Pitfall | Prevention Phase | Verification |
|---------|------------------|--------------|
| 1: JNA blocked on JDK 26 | Phase 1 (dependency selection) | Unit test: `SystemInfo()` on JDK 26 without `--enable-native-access` |
| 2: CPU ticks all zeros | Phase 2 (deployment docs + config) | Integration test: assert tick values > 0 |
| 3: SSE emitter leak | Phase 1 (SSE pattern extension) | Monitor active connection count over time |
| 4: SystemInfo thread safety | Phase 2 (dedicated collector pattern) | Stress test: 10 concurrent threads, 10K iterations |
| 5: CPU sampling too short | Phase 2 (interval config) | Assert `getSystemCpuLoadBetweenTicks` returns valid values |
| 6: Disk query blocks thread | Phase 2 (background caching) | Profile collection times, assert < 10ms in SSE path |
| 7: ECharts memory leak | Phase 3 (chart lifecycle) | Heap snapshot before/after 10min monitoring |
| 8: Security exposure | Phase 4 (role-based auth) | 401/403 test for unauthorized users |
| 9: CPU% mismatch with TM | Phase 2 (UI labeling) | Side-by-side comparison with Task Manager |
| 10: CPU temperature fake | Phase 2 (sensor availability check) | Assert `isPresent()` before displaying |
| 11: WMI init delay | Phase 2 (eager init) | First user request should see cached data |
| 12: Thread exhaustion | Phase 1 (capped connections) | Load test: 50 concurrent SSE connections |
| 13: MySQL from metrics | Phase 4 (storage decision) | Code review: no `JdbcTemplate` in monitoring service |
| 14: Network names | Phase 3 (display formatting) | Visual inspection of dashboard |
| 15: Process list cost | Phase 2 (separate endpoint) | Profile `os.getProcesses()` execution time |
| 16: First CPU call -1 | Phase 2 (skip first result) | Frontend should handle NaN/negative gracefully |
| 17: Empty sensors | Phase 2 (availability check) | Test on target Windows hardware |
| 18: Gradle dependency | Phase 1 (libs.versions.toml) | `./gradlew.bat dependencies` confirms FFM module |
| 19: Router guard | Phase 3 (route config) | Test navigation with admin and non-admin roles |

---

## Sources

- OSHI GitHub: https://github.com/oshi/oshi
- OSHI Issue #3123 -- FFM support and 7.0 module layout: https://github.com/oshi/oshi/issues/3123
- OSHI Issue #797 -- Thread safety: https://github.com/oshi/oshi/issues/797
- OSHI Issue #660 -- Are SystemInfo instances thread-safe: https://github.com/oshi/oshi/issues/660
- OSHI Discussion #2698 -- JEP 472 JNI restrictions: https://github.com/oshi/oshi/discussions/2698
- OSHI Issue #2694 -- CPU ticks zero on Windows: https://github.com/oshi/oshi/issues/2694
- OSHI Issue #2572 -- OSHI vs Task Manager CPU comparison: https://github.com/oshi/oshi/issues/2572
- OSHI Issue #646 -- IDLE tick calculation bug (historical): https://github.com/oshi/oshi/issues/646
- OSHI Issue #759 -- PDH/WMI dual-path thread safety: https://github.com/oshi/oshi/issues/759
- OSHI Issue #613 -- Slow WMI queries: https://github.com/oshi/oshi/issues/613
- OSHI Issue #1595 -- WMI query refactoring: https://github.com/oshi/oshi/issues/1595
- OSHI Performance documentation: https://raw.githubusercontent.com/oshi/oshi/master/src/site/markdown/Performance.md
- Spring Framework Issue #33340 -- SSE memory leak: https://github.com/spring-projects/spring-framework/issues/33340
- Spring Framework Issue #32629 -- Jetty SSE connection leak: https://github.com/spring-projects/spring-framework/issues/32629
- ECharts Issue #15480 -- Symbol shape memory leak: https://github.com/apache/echarts/issues/15480
- ECharts large dataset optimization (SSE streaming): https://developer.baidu.com/article/detail.html?id=6512319
- Spring Boot Actuator security pitfalls: https://www.invicti.com/web-application-vulnerabilities/spring-boot-actuator
- Datadog process monitoring 100x efficiency improvement: https://www.datadoghq.com/ja/blog/engineering/scaling-process-pipeline-efficiency/
- CSDN -- SpringBoot+OSHI 5 common problems: https://blog.csdn.net/m2n3b4v5c6/article/details/154560726
- OSHI Windows implementation DeepWiki: https://deepwiki.com/oshi/oshi/4.1-windows-implementation
- OSHI Windows CPU permission analysis: https://blog.gitcode.com/a6fbe73f77cde90e8a218fb7f7907377.html
