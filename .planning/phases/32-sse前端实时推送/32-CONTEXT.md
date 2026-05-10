# Phase 32: SSE 实时推送 + 前端基础页面 - Context

**Gathered:** 2026-05-10
**Status:** Ready for planning

<domain>
## Phase Boundary

后端通过 SSE 端点实时推送硬件指标（每 2s 一次），前端展示 CPU 使用率环形图、内存使用率环形图、磁盘分区容量进度条列表、系统基本信息卡片。SSE 推送服务和前端 composable 设计为可测试的接口抽象。

**不涉及：** 磁盘 IO/网络吞吐量折线图（Phase 33）、近 1 小时趋势（Phase 33）、安全加固（Phase 34）。

</domain>

<decisions>
## Implementation Decisions

### SSE 后端设计

- **D-01（广播模式）：** SSE 采用广播模式。所有客户端接收相同硬件指标数据，新建一个 SSE 端点管理全局连接池。硬件监控天然广播场景（所有客户端看到相同数据），无需像 SeckillSseService 那样按 queueId 独立订阅。
- **D-02（控制器位置）：** SSE 端点加到现有 `HardwareMonitorController`，新增 `/subscribe` / `/unsubscribe` 端点。不新建 Controller，保持 `controller/monitor/hardware/` 包内硬件相关端点统一。
- **D-03（心跳策略）：** 使用 `SseEmitter` 自带超时机制（60s）+ 浏览器 `EventSource` 自动重连。不额外发送心跳事件。每 2s 的数据推送本身自带心跳语义，无需单独保活。
- **D-04（数据更新模式）：** SSE 推送直接更新 Vue 组件 reactive 数据。数据到达即 UI 更新，不经过中间缓存层。

### 前端设计

- **D-05（页面布局）：** 左侧窄导航菜单 + 右侧主内容区。左侧列出指标类别（CPU/内存/磁盘/系统信息），右侧对应展示。
- **D-06（环形图方案）：** 使用 ECharts 环形图（vue-echarts）。项目已有 vue-echarts + echarts 依赖，无需新增包。样式可自定义匹配海滩风主题配色。
- **D-07（SSE Composable）：** 业务专用 `useHardwareMetrics()` composable，内部封装 SSE 连接创建、数据解析、重连逻辑。返回 `{ data, connected, error, reconnect }`。支持 mock `EventSource` 注入以满足 TDD-05 可测试要求。
- **D-08（重连策略）：** 指数退避：1s → 2s → 4s → 8s → 16s → max 30s。避免断线风暴时频繁请求。

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

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### 阶段定义
- `.planning/ROADMAP.md` §Phase 32 — 阶段目标、成功标准、需求列表
- `.planning/REQUIREMENTS.md` §v1.8 — HWM-10~15, HWM-17, HWM-19, TDD-02, TDD-05

### 现有架构
- `.planning/research/ARCHITECTURE.md` — SSE 架构设计、SSE vs WebSocket 对比、反模式（§SSE Modes）
- `.planning/codebase/ARCHITECTURE.md` — 完整架构描述
- `.planning/codebase/CONVENTIONS.md` — 代码规范

### 后端 SSE 模式参考
- `springboot/src/main/java/cn/coderstory/springboot/sse/seckill/SeckillSseService.java` — 现有 SSE 服务实现（SseEmitter + ConcurrentHashMap + 回调模式）
- `springboot/src/main/java/cn/coderstory/springboot/controller/seckill/SeckillSseController.java` — SSE Controller 模式（produces = TEXT_EVENT_STREAM_VALUE）

### Phase 31 硬件监控代码
- `springboot/src/main/java/cn/coderstory/springboot/controller/monitor/hardware/HardwareMonitorController.java` — 硬件控制器（新增 SSE 端点）
- `springboot/src/main/java/cn/coderstory/springboot/service/monitor/hardware/HardwareMetricsService.java` — 硬件采集服务接口（SSE 数据源）
- `springboot/src/main/java/cn/coderstory/springboot/dto/monitor/hardware/HardwareMetricsDTO.java` — SSE 推送的数据结构

### 前端模式参考
- `app-vue/src/views/monitor/MonitorDashboard.vue` — 现有监控页面（Element Plus 卡片布局）
- `app-vue/src/views/rocketmq/QpsChart.vue` — ECharts 集成模式（vue-echarts + use + CanvasRenderer）
- `app-vue/src/router/modules/routes.ts` — 前端路由配置（/monitor 路由已有）
- `app-vue/src/composables/useAnimationToggle.ts` — 现有 composable 模式参考
- `app-vue/src/api/modules/monitor.ts` — 监控 API 模块（需扩展 SSE）

### 技术标准
- `CLAUDE.md` §项目架构、§代码风格（前后端规范）、§构建命令
- `docs/comment-standards.md` — L0-L3 注释层级标准

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- **SeckillSseService** — SSE 连接管理模式可复用（SseEmitter、ConcurrentHashMap、onCompletion/onTimeout/onError 回调模式），但 Phase 32 需要广播模式而非每连接独立订阅
- **QpsChart.vue** — vue-echarts 集成模式可直接复用（`use([CanvasRenderer, LineChart, ...])` + `VChart` 组件 + reactive chartOption）
- **MonitorDashboard.vue** — Element Plus 卡片 + el-row/el-col 布局模式
- **router/modules/routes.ts** — 路由注册模式（lazy import + meta 元数据）

### Established Patterns
- Controller → Service 分层，`@RequiredArgsConstructor` + `private final` 注入
- `ApiResponse<T>` 统一响应封装（SSE 推送不经过 ApiResponse，直接推 DTO）
- Vue Composition API + `<script setup lang="ts">`
- `@/` 路径别名
- kebab-case CSS class + `<style scoped>`

### Integration Points
- `HardwareMonitorController` — 新增 `/subscribe` `/unsubscribe` 端点
- `HardwareMetricsService` — SSE 服务通过该接口获取最新指标快照
- `router/modules/routes.ts` — 新增硬件监控路由
- `api/modules/monitor.ts` — 新增 SSE 连接方法或类型定义
- `views/monitor/hardware/` — 新建页面目录
- `config/business.yaml` — 如需要配置 SSE 超时等参数

</code_context>

<specifics>
## Specific Ideas

- SSE 事件名约定：`metrics`（硬件指标数据）、`connected`（连接建立）、`error`（错误信息）
- 广播 SSE 服务名：`HardwareSseService`，内部 `Set<SseEmitter>` 或 `CopyOnWriteArrayList<SseEmitter>`
- 页面路径：`/monitor/hardware`，嵌套在已有 `/monitor` 下或独立路由
- 磁盘分区使用 Element Plus `El-Progress` 进度条组件展示容量百分比
- 系统信息使用 `El-descriptions` 或 `El-card` 展示

</specifics>

<deferred>
## Deferred Ideas

- 磁盘 IO / 网络吞吐量实时折线图 → Phase 33
- 近 1 小时趋势折线图（ECharts 滑动窗口，300 点上限） → Phase 33
- 夏日海滩风主题适配 → Phase 33
- 安全加固（ADMIN 角色校验） → Phase 34
- 页面加载态/空状态/异常状态处理 → Phase 34
- SSE 断线重连时 UI 状态提示 → Phase 34

</deferred>

---

*Phase: 32-SSE 前端实时推送*
*Context gathered: 2026-05-10 via discuss-phase*
