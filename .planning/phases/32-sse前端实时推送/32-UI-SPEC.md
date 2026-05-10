---
phase: 32
slug: sse-realtime-push-frontend-pages
status: draft
shadcn_initialized: false
preset: not-applicable-vue-element-plus
created: 2026-05-10
---

# Phase 32 — SSE 实时推送 + 前端基础页面

> Visual and interaction contract for the hardware monitoring frontend (CPU, memory, disk, system info) with SSE real-time data push.

---

## Overview

This phase adds a new hardware monitoring page (`/monitor/hardware`) under the existing `/monitor` route. The page receives real-time hardware metrics via SSE and displays them through four visual panels: CPU usage donut chart, memory usage donut chart, disk partition progress bars, and system information cards.

**Scope boundary:** This phase does NOT include:
- Disk IO / network throughput line charts (Phase 33)
- Trend line charts with sliding window (Phase 33)
- Summer beach theme color customization for charts (Phase 33)
- Security hardening / ADMIN role checks (Phase 34)
- Loading skeletons, empty states, error boundary handling (Phase 34)
- SSE disconnection banner notification (Phase 34)

---

## Design System

| Property | Value |
|----------|-------|
| Tool | Custom (Vue 3 + Element Plus) |
| Preset | Not applicable — project uses Element Plus theming via CSS variables |
| Component library | Element Plus 2.9.x |
| Chart library | ECharts 6.0.0 + vue-echarts 8.0.1 |
| Icon library | @element-plus/icons-vue (bundled with Element Plus) |
| Font | System font stack: `-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif` |
| Theme | Summer Beach (enterprise-theme.css) — ocean blue primary (#3b82f6), amber accent (#d97706) |

**Source:** CONTEXT.md D-05 (left nav layout), D-06 (ECharts donut), existing enterprise-theme.css verified in codebase.

---

## Layout Architecture

### Page Layout (D-05 — Locked)

```
+-- HardwareMonitorPage.vue -------------------------------+
|  +-- Left Nav (180px) -------+  +-- Right Content ------+ |
|  |                            |  |                       | |
|  |  [CPU]                     |  |  Active section        | |
|  |  [Memory]                  |  |  component renders     | |
|  |  [Disk]                    |  |  here                  | |
|  |  [System Info]             |  |                       | |
|  |                            |  |                       | |
|  +----------------------------+  +-----------------------+ |
+-----------------------------------------------------------+
```

| Property | Specification |
|----------|---------------|
| Container | `el-container` inside a single `el-card` or page-level wrapper |
| Left nav width | 180px fixed, no collapse for this sub-navigation |
| Left nav background | Transparent or `--ocean-pale: #dbeafe` with glass effect |
| Divider | `1px solid var(--ocean-light: #93c5fd)` or `el-divider direction="vertical"` |
| Right content padding | 24px matching `el-card__body` standard |
| Page outer padding | 24px (matches `layout-main` standard and `MonitorDashboard.vue`) |

### Left Navigation Specification

| Property | Specification |
|----------|---------------|
| Component | `el-menu` with `default-active` bound to active section |
| Menu items | CPU, 内存, 磁盘, 系统信息 (4 items, no sub-menus) |
| Menu item height | 44px (slightly tighter than main sidebar's 48px) |
| Item padding | `padding-left: 16px` |
| Active indicator | Amber background gradient (`rgba(253, 230, 138, 0.6)`) + ocean-deep text |
| Icon | Each item uses an appropriate Element Plus icon (e.g., `Cpu`, `DataBoard`, `Disk`, `InfoFilled`) |
| Behavior | Click changes the active section in right content pane — this is a SPA tab switch, not route navigation |

### Responsive Behavior

| Breakpoint | Behavior |
|------------|----------|
| >= 1200px | Full layout: left nav + right content side by side |
| 768px - 1199px | Left nav collapses to icon-only (64px) if space constrained |
| < 768px | Left nav becomes a horizontal top tab bar (`el-tabs`); content below |

---

## Component Specifications

### CpuGauge.vue (HWM-13)

**Purpose:** Display CPU usage as a donut chart with processor info card.

#### States

| State | Condition | Visual |
|-------|-----------|--------|
| Loading | `metrics.value === null` (initial, before first SSE event) | ECharts donut shows gray placeholder with "---" center text; info card shows skeleton lines |
| Connected / Data | `connected === true` and `metrics.value.cpu !== null` | Donut fills with usage %; info card shows processor details |
| Disconnected | `connected === false` and previous data exists | Donut freezes on last value; a small amber dot indicator appears top-right of card; info card retains last data |
| No CPU data | `metrics.value.cpu === null` (server returned null) | Donut at 0%; info card shows "暂无数据" |

#### Layout

```
+-- el-card (glass-card) ----------------------------------+
|  [donut chart 180x180]  |  [info items]                 |
|                          |  型号: Intel(R) ...           |
|    75%                   |  物理核心: 8                  |
|                          |  逻辑核心: 16                 |
|                          |  频率: 3.60 GHz               |
|                          |  架构: 64-bit                 |
+----------------------------------------------------------+
```

| Property | Specification |
|----------|---------------|
| Card | `el-card` with default glass style (existing `enterprise-theme.css`) |
| Layout | `el-row` with `el-col :span="8"` for chart, `el-col :span="16"` for info |
| Donut size | 180px x 180px (chart container) |
| Info field label | `--el-text-color-secondary: #64748b` at 13px |
| Info field value | `--el-text-color-primary: #1e293b` at 14px weight 600 |

#### ECharts Donut Configuration

| Property | Value |
|----------|-------|
| Type | `pie` with `radius: ['55%', '75%']` |
| Usage color | `--el-color-primary: #3b82f6` (gradient from #3b82f6 to #1e3a8a) |
| Remaining color | `--el-fill-color: #f0f9ff` or `#E4E7ED` |
| Center label | `{usagePercent}%` at 28px bold, ocean-deep color |
| Sub-label | "CPU" at 13px secondary text |
| Animation | Default ECharts grow animation on first data; subsequent updates animate smoothly with `animationDurationUpdate: 500` |
| Emphasis | No hover emphasis (static display, no interaction needed) |

---

### MemoryGauge.vue (HWM-14)

**Purpose:** Display memory usage as a donut chart with total/used/available breakdown.

#### States

Same state matrix as CpuGauge.vue, with "暂无内存数据" for null data.

#### Layout

```
+-- el-card (glass-card) ----------------------------------+
|  [donut chart 180x180]  |  [info items]                 |
|                          |  总量: 32.0 GB                |
|    45%                   |  已用: 14.4 GB               |
|                          |  可用: 17.6 GB               |
|                          |                              |
+----------------------------------------------------------+
```

#### ECharts Donut Configuration

| Property | Value |
|----------|-------|
| Type | `pie` with `radius: ['55%', '75%']` |
| Usage color | `--ocean-medium: #3b82f6` |
| Remaining color | `--el-fill-color: #f0f9ff` |
| Center label | `{usagePercent}%` at 28px bold, ocean-deep color |
| Sub-label | "内存" at 13px secondary text |

---

### DiskPartitions.vue (HWM-15)

**Purpose:** Show each disk partition as a progress bar with capacity details.

#### States

| State | Condition | Visual |
|-------|-----------|--------|
| Loading | No SSE data yet | 3 skeleton progress bars (gray shimmer) |
| Connected / Data | `disks` array has entries | Full progress bar list |
| No disk data | `disks === null` or empty array | "暂无磁盘信息" centered empty state |
| High usage threshold | usage >= 90% | Progress bar status changes to `exception` (red gradient) |
| Warning threshold | usage >= 80% | Progress bar status changes to `warning` (amber gradient) |

#### Layout

```
+-- el-card (glass-card) ----------------------------------+
|  #header: 磁盘分区                                        |
|                                                          |
|  +-- disk-card-1 ---------------------------------------+|
|  |  C: (SSD)              [====70%========              ]||
|  |  已用: 120.5 GB  可用: 51.2 GB  总量: 171.7 GB      ||
|  +------------------------------------------------------+|
|  +-- disk-card-2 ---------------------------------------+|
|  |  D: (HDD)              [=====85%=======              ]||
|  |  已用: 500.2 GB  可用: 88.0 GB  总量: 588.2 GB      ||
|  +------------------------------------------------------+|
+----------------------------------------------------------+
```

| Property | Specification |
|----------|---------------|
| Component | `el-progress` with `:text-inside="true"` and `:stroke-width="22"` |
| Percentage calc | `usedSpace / totalSpace * 100` |
| Progress bar border-radius | `--el-border-radius-round: 24px` |
| Progress color | Normal: ocean blue gradient; Warning (>=80%): amber gradient; Exception (>=90%): red gradient |
| Disk header | Disk label (e.g., "C:") in bold 14px + disk model in secondary 12px |
| Details row | Three spans in `14px` secondary text, separated by ` | ` |
| Spacing between disks | `margin-bottom: 16px` |

**Source:** CONTEXT.md uses Claude's discretion for disk progress bar style. El-Progress is the standard choice given existing Element Plus integration.

---

### SystemInfo.vue (HWM-17)

**Purpose:** Display static system information (OS, uptime, process count).

#### States

| State | Condition | Visual |
|-------|-----------|--------|
| Loading | No SSE data yet | Gray skeleton lines inside card |
| Connected / Data | `system` data available | Full system info display |
| No system data | `system === null` | "暂无系统信息" empty state |

#### Layout

```
+-- el-card (glass-card) ----------------------------------+
|  #header: 系统信息                                        |
|                                                          |
|  操作系统版本    Windows 11 Pro 23H2 (22631.2861)        |
|  运行时间       运行中 12天 5小时 30分钟                 |
|  进程数         245 个进程                               |
|  Java 版本      JDK 26.0.1 (已确认)                      |
|  系统架构        64-bit                                  |
+----------------------------------------------------------+
```

| Property | Specification |
|----------|---------------|
| Component | Custom structure: label-value pairs in a CSS grid |
| Label column | Fixed 100px width, `--el-text-color-secondary: #64748b`, 14px |
| Value column | `--el-text-color-primary: #1e293b`, 14px weight 500 |
| Row spacing | `12px` between rows |
| Divider | Optional `el-divider` between system info and Java info groups |
| Card header | "系统信息" in ocean-deep font-weight 600 |

**Source:** CONTEXT.md Claude's discretion for system info card layout. Custom grid is preferred over `el-descriptions` for tighter control.

---

### HardwareMonitorPage.vue (HWM-12, HWM-19)

**Purpose:** Shell page that manages SSE connection lifecycle, coordinates data flow between `useHardwareMetrics()` and child components.

#### Behavior

| Action | Behavior |
|--------|----------|
| Mount | Calls `connect()` on `useHardwareMetrics()` — establishes SSE connection to `/api/monitor/hardware/subscribe` |
| Data received | SSE event `metrics` is parsed; reactive `metrics` ref updates; child components reactively re-render |
| Connection lost | `onerror` fires; `connected` becomes false; exponential backoff starts (1s -> 2s -> 4s -> 8s -> 16s -> max 30s) |
| Unmount | Calls `disconnect()` — closes EventSource, clears reconnect timer |
| Manual reconnect | Page-level retry button appears when disconnected for > 30s (Phase 34 will add banner notification) |

#### Data Flow

```
useHardwareMetrics()
  |
  |-- EventSource -> SSE streaming -> JSON.parse -> reactive metrics.value
  |
  |-- connected.value (boolean)  --> props to children
  |-- metrics.value.cpu          --> CpuGauge.vue :cpuData prop
  |-- metrics.value.memory       --> MemoryGauge.vue :memoryData prop
  |-- metrics.value.disks        --> DiskPartitions.vue :disks prop
  |-- metrics.value.system       --> SystemInfo.vue :systemData prop
```

**Source:** CONTEXT.md D-04 (data update mode: SSE data directly updates reactive state), D-07 (useHardwareMetrics), D-08 (exponential backoff).

---

## Interaction Design

### SSE Connection Indicator

| State | Visual Indicator | Location |
|-------|-----------------|----------|
| Connected (green) | Small green dot (8px, `--el-color-success`) in the header area | Top-right of the hardware monitoring page, or integrated in card header |
| Disconnected/reconnecting (amber) | Small amber dot (8px, `--el-color-warning`) + optional text "重连中..." | Same location |
| Disconnected > 30s (red) | Small red dot (8px, `--el-color-danger`) + retry button | Same location |

### Navigation Interaction

| Action | Behavior |
|--------|----------|
| Click "CPU" in left nav | Right content switches to CPU panel with slide transition |
| Click "内存" in left nav | Right content switches to memory panel |
| Click "磁盘" in left nav | Right content switches to disk partitions panel |
| Click "系统信息" in left nav | Right content switches to system info panel |
| Default active | "CPU" — loads as first visible section |
| Active highlight | Selected nav item gets amber background + bold text |

### Data Update Animation

| Element | Update Behavior |
|---------|----------------|
| Donut chart | `animationDurationUpdate: 500` — smooth transition between values |
| Progress bar | `el-progress` has 0.6s CSS transition (already in enterprise-theme.css) |
| Info card values | No animation — numbers snap to new values (reactive binding) |
| Timestamp | Last updated time shown in footer of main card: `更新于 HH:mm:ss` |

---

## Visual Design

### Color Usage

The existing Summer Beach theme (enterprise-theme.css) covers all needed color tokens. This phase does NOT require new color variables.

| Token | Value | Usage in This Phase |
|-------|-------|---------------------|
| `--ocean-deep` | #1e3a8a | Card headers, chart center label, section titles |
| `--ocean-medium` / `--el-color-primary` | #3b82f6 | Donut usage segment, progress bar fill (normal), active nav item icon |
| `--ocean-light` | #93c5fd | Dividers, border colors, progress bar track |
| `--ocean-pale` | #dbeafe | Left nav background, card backgrounds |
| `--sand` | #fef3c7 | Active nav item background (with transparency) |
| `--sand-dark` | #fde68a | Hover state for nav items |
| `--sun` / `--el-color-warning` | #d97706 / #f59e0b | Progress bar at >= 80%, SSE reconnecting indicator dot |
| `--el-color-danger` | #ef4444 | Progress bar at >= 90%, SSE disconnected indicator dot |
| `--el-color-success` | #10b981 | SSE connected indicator dot |
| `--el-text-color-secondary` | #64748b | Info field labels, detail text |
| `--el-text-color-primary` | #1e293b | Info field values, main content text |

### Accent (10%) Reserved For

- Active navigation item indicator (amber background highlight)
- Warning-level thresholds on disk progress bars (>= 80% usage)
- SSE reconnecting state dot (amber)
- NOT used for: primary buttons, chart data, card headers (those use ocean blue)

### Typography

| Role | Size | Weight | Line Height | Source |
|------|------|--------|-------------|--------|
| Body | 14px | 400 | 1.5 | CSS variable `--el-font-size-base` |
| Small / Label | 13px | 400 | 1.4 | CSS variable `--el-font-size-small` |
| Card header title | 16px | 600 | 1.4 | CSS variable `--el-font-size-extra-large` |
| Chart center percent | 28px | 700 | 1.2 | Custom (ECharts label) |
| Info field value | 14px | 600 | 1.5 | Custom emphasis within body |
| Section nav item | 14px | 500 | 1.4 | Element Plus menu default |

### Spacing Scale

| Token | Value | Usage in This Phase |
|-------|-------|---------------------|
| xs | 4px | Icon-to-text gap in nav items |
| sm | 8px | Dot indicator to label gap |
| md | 16px | `margin-bottom` between disk cards; chart to info text gap |
| lg | 24px | `padding` inside cards; page outer padding |
| xl | 32px | Gap between left nav and right content |
| 2xl | 48px | Section-level break (not used in this phase) |
| 3xl | 64px | Page-level spacing (not used in this phase) |

Exceptions: none — all spacing follows the 8-point grid as defined by existing Element Plus conventions.

---

## Data Flow Contract

### SSE Event Protocol

| Event Name | Direction | Payload | Frequency |
|------------|-----------|---------|-----------|
| `connected` | Server -> Client | `{"status":"connected"}` | Once on connection |
| `metrics` | Server -> Client | `HardwareMetricsDTO` JSON | Every 2s |
| `error` | Server -> Client | `{"message":"..."}` | On server error |

### HardwareMetricsDTO Structure (TypeScript)

Defined in `app-vue/src/api/modules/hardware.ts`. Full type definitions are provided in 32-RESEARCH.md. Key reactive binding:

```typescript
interface HardwareMetricsDTO {
  timestamp: number        // Unix ms
  cpu: CpuMetricsDTO | null
  memory: MemoryMetricsDTO | null
  disks: DiskMetricsDTO[] | null
  network: NetworkMetricsDTO[] | null   // Ignored in this phase (Phase 33)
  system: SystemInfoDTO | null
}
```

### useHardwareMetrics() Composable API

| Return | Type | Description |
|--------|------|-------------|
| `connect` | `() => void` | Establish SSE connection |
| `disconnect` | `() => void` | Close SSE, clear timers |
| `reconnect` | `() => void` | Reset retry delay to 1s and reconnect |
| `connected` | `Ref<boolean>` | Reactive connection state |
| `metrics` | `Ref<HardwareMetricsDTO \| null>` | Reactive metrics data |
| `error` | `Ref<string \| null>` | Reactive error message |

**Source:** CONTEXT.md D-07 (composable signature), D-08 (exponential backoff), 32-RESEARCH.md Pattern 3.

### Component Props Interface

| Child Component | Props | Prop Type |
|-----------------|-------|-----------|
| `CpuGauge` | `cpuData`, `connected` | `CpuMetricsDTO \| null`, `boolean` |
| `MemoryGauge` | `memoryData`, `connected` | `MemoryMetricsDTO \| null`, `boolean` |
| `DiskPartitions` | `disks`, `connected` | `DiskMetricsDTO[] \| null`, `boolean` |
| `SystemInfo` | `systemData` | `SystemInfoDTO \| null` |

---

## Route & Menu Registration

| Property | Value |
|----------|-------|
| Route path | `/monitor/hardware` (nested child of `/monitor`) |
| Route name | `HardwareMonitor` |
| Component | Lazy import: `() => import('@/views/monitor/hardware/HardwareMonitorPage.vue')` |
| Parent | `/monitor` (existing `MonitorDashboard` route) |
| Meta | `{ title: '硬件监控', requiresAuth: true }` |
| Menu registration | Add as child menu item under "监控大盘" in backend menu API (requires database record or menu API call) |

**Source:** CONTEXT.md Claude's discretion — resolved as nested child route under `/monitor`.

---

## Copywriting Contract

| Element | Copy | Notes |
|---------|------|-------|
| Page title | `硬件监控` | Route meta title, card header |
| CPU section title | `CPU` | Left nav label |
| Memory section title | `内存` | Left nav label |
| Disk section title | `磁盘` | Left nav label |
| System section title | `系统信息` | Left nav label |
| Chart center label | `CPU` / `内存` | Sub-label below percentage in donut center |
| No data (CPU) | `暂无 CPU 数据` | Empty state when cpu is null |
| No data (memory) | `暂无内存数据` | Empty state when memory is null |
| No data (disk) | `暂无磁盘信息` | Empty state when disks is null/empty |
| No data (system) | `暂无系统信息` | Empty state when system is null |
| Connection status | `已连接` | Tooltip for green dot |
| Reconnecting | `重连中...` | Tooltip for amber dot |
| Disconnected | `连接已断开` | Tooltip for red dot |
| Retry button | `重新连接` | Manual reconnect button |
| Disk details format | `已用: {value}` / `可用: {value}` / `总量: {value}` | Separated by vertical bars |
| System info labels | `操作系统版本` / `运行时间` / `进程数` / `系统架构` | Label column |
| Timestamp footer | `更新于 HH:mm:ss` | Card footer showing last update time |

### Destructive Actions

None in this phase. No destructive actions exist — hardware monitoring is read-only.

### Primary CTA

There is no single primary CTA button in this phase. The "main action" is the real-time data visualization itself. The implicit CTA is: "navigate to /monitor/hardware to view hardware metrics."

---

## Registry Safety

Not applicable — this is a Vue 3 + Element Plus project without shadcn. No third-party component registries are used.

---

## Accessibility Considerations

| Requirement | Implementation |
|-------------|----------------|
| Color contrast | All text colors meet WCAG AA: body text #1e293b on #f0f9ff background (ratio > 7:1) |
| Motion preference | Chart animations respect `prefers-reduced-motion: reduce` via existing enterprise-theme.css media query |
| Screen reader | Donut chart center text is rendered as SVG text (readable); consider adding `aria-label` to chart containers |
| Focus navigation | Left nav `el-menu` supports keyboard navigation by default |
| SSE status | Connection dot has `title` attribute with descriptive text; no visual-only dependency |

---

## File Inventory

### New Files

| File | Type | Purpose |
|------|------|---------|
| `app-vue/src/views/monitor/hardware/HardwareMonitorPage.vue` | Vue component | Shell page: left nav + right content + SSE composable orchestration |
| `app-vue/src/views/monitor/hardware/CpuGauge.vue` | Vue component | CPU donut chart + info card |
| `app-vue/src/views/monitor/hardware/MemoryGauge.vue` | Vue component | Memory donut chart + info card |
| `app-vue/src/views/monitor/hardware/DiskPartitions.vue` | Vue component | Disk partition progress bar list |
| `app-vue/src/views/monitor/hardware/SystemInfo.vue` | Vue component | System info display card |
| `app-vue/src/composables/useHardwareMetrics.ts` | TypeScript composable | SSE connection management with exponential backoff |
| `app-vue/src/api/modules/hardware.ts` | TypeScript API module | HardwareMetricsDTO type definitions |

### Modified Files

| File | Change |
|------|--------|
| `app-vue/src/router/modules/routes.ts` | Add `/monitor/hardware` route as child of `/monitor` |

### Backend Files (created/updated in Phase 31-03, referenced here)

| File | Purpose |
|------|---------|
| `springboot/.../controller/monitor/hardware/HardwareMonitorController.java` | + `/subscribe`, `/unsubscribe` SSE endpoints |
| `springboot/.../sse/monitor/HardwareSseService.java` | SSE broadcast service (new in backend) |
| `springboot/.../service/monitor/hardware/impl/HardwareMetricsServiceImpl.java` | `collect()` -> broadcast hook |

---

## Checker Sign-Off

- [ ] Dimension 1 Copywriting: PASS
- [ ] Dimension 2 Visuals: PASS
- [ ] Dimension 3 Color: PASS
- [ ] Dimension 4 Typography: PASS
- [ ] Dimension 5 Spacing: PASS
- [ ] Dimension 6 Registry Safety: PASS (N/A — Vue + Element Plus, no shadcn)

**Approval:** pending
