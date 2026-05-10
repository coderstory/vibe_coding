# Phase 32: SSE 实时推送 + 前端基础页面 - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-05-10
**Phase:** 32-SSE 前端实时推送
**Areas discussed:** SSE 广播模型, SSE 控制器位置, 心跳策略, 数据更新模式, 页面布局, 环形图方案, SSE Composable API, 重连策略

---

## SSE 广播模型

| Option | Description | Selected |
|--------|-------------|----------|
| 广播模式 | 单个 SSE 端点，新连接加入全局广播列表。所有客户端接收相同数据 | ✓ |
| 单连接单订阅 | 每个客户端独立订阅/取消，类似 SeckillSseService | |
| 先广播，后续需要再拆分 | 初始广播实现，预留按客户端过滤的扩展点 | |

**User's choice:** 广播模式
**Notes:** 硬件监控天然广播场景，所有客户端看到相同数据。实现最简单。

---

## SSE 控制器位置

| Option | Description | Selected |
|--------|-------------|----------|
| 加到 HardwareMonitorController | 硬件相关端点统一放在 HardwareMonitorController 中 | ✓ |
| 新建 HardwareSseController | 职责分离，但 Phase 32 只有这一个 SSE 端点，单独 Controller 冗余 | |
| 抽取公共 SSE 抽象 | 硬件 SSE + seckill SSE 统一抽象基类 | |

**User's choice:** 加到 HardwareMonitorController
**Notes:** 保持 controller/monitor/hardware/ 包内硬件端点统一。

---

## 心跳策略

| Option | Description | Selected |
|--------|-------------|----------|
| SseEmitter 超时+浏览器重连 | SseEmitter 自带超时机制 + 浏览器 EventSource 自动重连。最简实现 | ✓ |
| 定时心跳事件 | 后端每 10s 广播一次 heartbeat 事件 | |
| 前端 ping | SseEmitter 设长时间超时 + 前端每 30s 发 ping | |
| 数据即心跳 | 每 2s 已有数据推送，数据本身起心跳作用 | |

**User's choice:** SseEmitter 超时+浏览器重连
**Notes:** 每 2s 数据推送自带心跳语义，无需额外保活机制。

---

## 数据更新模式

| Option | Description | Selected |
|--------|-------------|----------|
| SSE 直接推组件 | SSE 推送直接更新 Vue reactive 数据 | ✓ |
| SSE 写缓存，组件消费 | SSE 写入本地 ref/reactive 缓冲区，组件通过 watch/computed 消费 | |
| 事件总线模式 | SSE 推数据 + emit 事件，子组件按需订阅 | |

**User's choice:** SSE 直接推组件
**Notes:** 数据到达即 UI 更新，不经过中间缓存层。

---

## 页面布局

| Option | Description | Selected |
|--------|-------------|----------|
| 单页滚动 | 顶部系统信息卡片，往下 CPU/内存/磁盘依次排列 | |
| 分 Tab 切换 | Tab1=CPU, Tab2=内存, Tab3=磁盘 | |
| 左侧导航布局 | 左侧窄菜单列指标类别，右侧主内容区 | ✓ |

**User's choice:** 左侧导航布局
**Notes:** 用户倾向左侧导航布局，便于后续扩展更多指标页面。

---

## 环形图方案

| Option | Description | Selected |
|--------|-------------|----------|
| ECharts 环形图 | ECharts 饼图 + 定制环形样式，项目已有 vue-echarts 依赖 | ✓ |
| Element Plus 进度环 | El-Progress 环形进度条，更轻量但样式固定 | |
| 自定义 SVG | 纯 SVG 手动绘制环形，完全控制样式 | |
| ECharts 仪表盘 | ECharts gauge 图表 | |

**User's choice:** ECharts 环形图
**Notes:** 项目已有 vue-echarts + echarts 依赖，无需新增包。QpsChart.vue 已有使用模式。

---

## SSE Composable API

| Option | Description | Selected |
|--------|-------------|----------|
| useHardwareMetrics() | 业务专用，封装 SSE 连接 + 数据解析 + 重连 | ✓ |
| 通用 useEventSource(url) | 通用 SSE composable，调用方自行解析 | |
| 双层架构 | 底层 useEventSource() + 上层 useHardwareMetrics() | |

**User's choice:** useHardwareMetrics()
**Notes:** 业务专用 composable，支持 mock EventSource 注入以满足 TDD-05 可测试要求。

---

## 重连策略

| Option | Description | Selected |
|--------|-------------|----------|
| 指数退避 | 1s → 2s → 4s → 8s → 16s → max 30s | ✓ |
| 固定 3s | 断线后固定 3s 重试 | |
| 浏览器默认行为 | EventSource 默认自动重连，间隔不可控 | |

**User's choice:** 指数退避
**Notes:** 避免断线风暴时频繁请求，同时用户体验良好。

---

## Claude's Discretion

- SSE 端点具体路径（`/subscribe` vs `/stream`）
- SSE 事件名称约定
- `SseEmitter` 精确 timeout 值（默认 60s，可调整）
- Vue 组件命名和文件结构
- 磁盘进度条具体样式
- 系统信息卡片布局细节
- 监控页面路由路径
- `useHardwareMetrics()` 参数签名和返回值结构
- 左侧导航菜单具体实现

## Deferred Ideas

- 磁盘 IO / 网络吞吐量实时折线图 → Phase 33
- 近 1 小时趋势折线图（ECharts 滑动窗口）→ Phase 33
- 夏日海滩风主题适配 → Phase 33
- 安全加固（ADMIN 角色校验） → Phase 34
- 页面加载态/空状态/异常状态处理 → Phase 34
- SSE 断线重连时 UI 状态提示 → Phase 34
