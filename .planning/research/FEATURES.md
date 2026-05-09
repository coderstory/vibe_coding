# 特征全景：Windows 硬件负载监控

**领域:** 服务器硬件监控 —— Vue 3 + Spring Boot 管理后台
**研究日期:** 2026-05-09
**置信度:** HIGH

## 表属性

用户在管理后台中期望看到的硬件监控基本功能。缺失这些会让监控页面感觉"不完整"。

| 特征 | 为什么是预期 | 复杂度 | 说明 |
|------|-------------|--------|------|
| CPU 使用率（总体百分比） | 最基础的监控指标，任何监控系统必备 | 低 | OSHI `getSystemCpuLoadBetweenTicks()` 需两次采样计算 |
| CPU 核数/型号/频率展示 | 用户想确认服务器 CPU 配置信息 | 低 | OSHI `CentralProcessor` 直接提供 |
| 内存总量/已用/可用 | 标准内存监控三要素 | 低 | OSHI `GlobalMemory.getTotal()` / `getAvailable()` |
| 内存使用率百分比 | 直观反映内存压力状态 | 低 | 计算 (total - available) / total |
| 磁盘列表与分区信息 | 显示各磁盘/分区的挂载点和容量 | 低 | OSHI `HWDiskStore` + `OSFileStore` |
| 磁盘使用率（已用/总量） | 用户需要知道磁盘是否快满了 | 低 | 差分 `getTotalSpace()` / `getUsableSpace()` |
| 网络接口列表 | 显示各网卡状态和数据 | 低 | OSHI `NetworkIF` |
| 实时数据刷新 | 用户期望页面自动更新，无需手动刷新 | 中 | WebSocket 推送或 SSE |
| 系统运行时间和基本信息 | 一眼确认服务器运行状态 | 低 | OSHI `OperatingSystem` |
| 当前进程数 | 反映系统负载的基本指标 | 低 | OSHI `OperatingSystem.getProcessCount()` |
| 数值格式化（GB/TB 单位换算） | 原始字节数不可读，需自动换算 | 低 | 用 1024 进制自动选择 KB/MB/GB/TB |
| 图表可视化 | 趋势比数字更能体现状态变化 | 中 | ECharts 折线图展示近 1h/6h/24h 趋势 |

### 实时数据结构标准模式

业界通行的实时监控数据推送有两种模式：

**模式 A: WebSocket（双向，推荐）**
```
前端建立 WebSocket 连接 → 后端每 N 秒推送一次全量指标包
前端接收后直接更新图表（不经过 HTTP 请求/响应开销）
```
- 优点：延迟低、服务端可主动推送、前端代码简单
- 适用：实时页面常驻的场景（监控页持续打开）
- 已有参考：`ServerStatus-` 项目使用 Spring Boot + Netty WebSocket

**模式 B: SSE（单向，轻量）**
```
前端建立 EventSource 连接 → 后端每 N 秒推送一次指标事件
仅服务端→客户端，前端不能发送数据
```
- 优点：原生浏览器支持、自动重连、比 WebSocket 轻量
- 适用：纯展示场景（不需要前端发消息给后端）
- 已有参考：当前项目秒杀系统已使用 SSE 推送队列状态

### 数据采集频率标准

| 指标类型 | 推荐采集间隔 | 理由 |
|---------|-------------|------|
| CPU 使用率 | 3-5 秒 | OSHI 需要两次采样计算负载，间隔太短不准确（`getCpuInfo(long waitingTime)` 需要等待时间） |
| 内存使用率 | 3-5 秒 | 变化不剧烈，秒级刷新足够 |
| 磁盘使用率 | 10-30 秒 | 使用率变化慢，长间隔更合理 |
| 磁盘 IOPS/读写速度 | 5-10 秒 | 需两次差分，间隔太短差值过小不准确 |
| 网络吞吐量 | 3-5 秒 | 与磁盘 IO 类似需差分 |
| 网络连接数 | 5-10 秒 | 变化相对较慢 |
| 进程列表 | 10-30 秒 | 进程列表数据量大，不宜高频采集 |

## 区分项

提升监控页面价值的特征。非必须，但会显著提升用户体验。

| 特征 | 价值 | 复杂度 | 说明 |
|------|------|--------|------|
| CPU 温度监控 | 预警过热风险，预防硬件故障 | 中 | OSHI 6.7.0+ 通过 jLibreHardwareMonitor 支持 Windows 温度读取，但硬件兼容性参差不齐 |
| CPU 每个核心使用率 | 定位单核瓶颈场景（如单线程应用） | 中 | OSHI `getProcessorCpuLoadBetweenTicks()` 返回每个逻辑核心的负载数组 |
| 磁盘读写速度（MB/s） | 判断磁盘 IO 瓶颈 | 中 | 需两次 `HWDiskStore` 采样后差分计算 |
| 磁盘 IOPS | 数据库等 IO 密集型应用的性能指标 | 中 | 与读写速度同源，使用 `getReads()`/`getWrites()` 差分 |
| 磁盘 IO 延迟 | 判断磁盘健康状态 | 中 | `getTransferTime()` 与 IOPS 结合计算平均延迟 |
| 网络吞吐量（上行/下行 Mbps） | 排查网络带宽瓶颈 | 中 | `NetworkIF.getBytesSent()`/`getBytesRecv()` 差分计算 |
| 网络连接状态统计（ESTABLISHED/TIME_WAIT 等） | 判断网络连接健康度（如 TIME_WAIT 过多） | 高 | OSHI 不直接提供，需通过 `netstat` 命令解析或 JNI |
| 进程 Top N（按 CPU/内存排序） | 快速定位资源消耗大户 | 中 | OSHI `OperatingSystem.getProcesses()` 支持按 CPU/内存排序 |
| 进程详情（PID/线程数/打开文件数） | 深入排查问题进程 | 中 | OSHI `OSProcess` 提供进程详细信息 |
| GPU 监控 | 机器学习/渲染场景必备 | 高 | OSHI 不直接支持 GPU，需 NVML（仅 NVIDIA）或 WMI |
| 历史数据趋势（带时间范围选择） | 排查"半小时前发生了什么" | 高 | 需要存储层（InfluxDB 或 MySQL 时序表），增量最大 |
| 阈值告警（CPU > 90% 时告警） | 被动发现问题而非主动巡检 | 中 | 后端定时检查缓存中的指标值 |
| 暗色主题适配 | 运维人员长时间查看时减少眼疲劳 | 低 | CSS 变量配合 `prefers-color-scheme` |
| 单台 vs 多服务器切换 | 从单机扩展到多机监控 | 高 | 架构变更，需引入 agent 概念 |
| 导出数据 CSV | 事故分析时需要导出历史数据 | 中 | 后端提供日期范围导出接口 |
| 大屏模式（全屏自适应） | 运维大屏展示 | 低 | CSS `fullscreen` API + 大字号布局 |
| JSON 指标 API 端点 | 被其他系统集成消费 | 低 | `@GetMapping("/api/monitor/metrics")` |

### 进程列表性能说明

OSHI 获取进程列表时需要遍历操作系统进程表，Windows 上通过 PDH 性能计数器读取。在进程数多的系统（>500 进程）上，每次调用 `getProcesses()` 可能消耗 50-200ms。因此：
- 进程列表不应像 CPU 那样 3 秒刷新一次
- 建议 30 秒以上 + 用户手动刷新按钮
- 按 CPU/内存排序的 Top N 可通过 `limit` 参数控制（如只取前 20）

## 反模式

监控系统中常见但有害的做法。

| 反模式 | 为什么有害 | 应该怎么做 |
|--------|-----------|-----------|
| 每次请求都直接调用 OSHI | JNA 边界调用开销大，高频请求会导致 CPU 负载上升（观测量干扰被观测量） | 使用 `@Scheduled` 定时采集 + 内存缓存，API 只读缓存 |
| 采集频率过高（<1 秒） | OSHI 读取 PDH 计数器有开销，1 秒内 CPU 负载值变化很小无意义；高频采集本身会推高 CPU | 合理频率：CPU/内存 3-5 秒，磁盘 IO/网络 5-10 秒 |
| 把原始指标写入 MySQL 的通用表 | MySQL 不适合高频时间序列写入（约 2000 点/秒上限），大量历史数据会导致表膨胀和查询缓慢 | 使用 InfluxDB/TimescaleDB；或 MySQL 分区表 + 定期清理归档 |
| 存储全量历史数据不做清理 | 时间序列数据增长速度极快（单机 10 秒采样，一天约 8640 行/指标），一个月后查询缓慢 | 设保留策略：原始数据保留 7 天，聚合数据保留 3 月，日统计保留 1 年 |
| 展示 50+ 个图表让用户自己看 | 信息过载，用户不知道什么指标重要（Netdata 研究：97% 告警无需立即处理） | 聚焦 3-10 个核心指标，参考 Google 四大黄金信号（USE/RED 方法） |
| 告警只设阈值不做关联分析 | CPU > 90% 告警频繁，运维逐渐无视（狼来了效应） | 阈值 + 持续时间组合（CPU > 90% 持续 5 分钟）；SLO 导向告警 |
| CPU 温度显示 0°C 不处理 | 误导用户以为传感器坏了或温度异常，实际是 OSHI 在无权限环境下返回 0 | 检测到 0/NaN 时显示"暂不支持"或"传感器不可用" |
| 在前端用定时器轮询 HTTP API 更新 | 每次都是完整的 HTTP 请求-响应周期，延迟高、服务端压力大、客户端网络开销大 | 使用 WebSocket 或 SSE，服务端主动推送增量数据 |
| 监控本机把自己也监控进去 | 监控系统的 CPU/内存/IO 消耗会叠加到被监控数据上 | 至少将监控进程自身从进程列表中过滤；或取系统总负载减去监控进程 |
| 不在容器环境中做特殊处理 | Docker 内 `/proc` 和 `/sys` 被限制，OSH 可能读到宿主机而非容器的资源限制 | 检测容器环境 `/.dockerenv`，使用 cgroup 数据源 |

## 特征依赖关系

```
OSHI 库集成（SystemInfo 单例）
    └──requires──> Spring Boot @Configuration

定时采集服务（@Scheduled + 缓存）
    └──requires──> OSHI 库集成
    └──requires──> Spring Boot @EnableScheduling

实时推送（WebSocket / SSE）
    └──requires──> 定时采集服务
    └──requires──> Spring WebSocket 支持 / SSE

核心指标 API（/api/monitor/metrics）
    └──requires──> 定时采集服务（从缓存读，不直接调 OSHI）

历史数据存储
    └──requires──> 定时采集服务（写入持久化）
    └──requires──> InfluxDB 或 MySQL 时序表

历史趋势查询 API
    └──requires──> 历史数据存储

阈值告警
    └──requires──> 定时采集服务（检查缓存中的值）

CPU 温度监控
    └──requires──> OSHI 6.7.0+ + jLibreHardwareMonitor 依赖
    └──conflicts──> 部分硬件/VM 环境不支持（返回 0）

磁盘 IOPS/读写速度
    └──requires──> 两次 HWDiskStore 采样差分计算
    └──requires──> 定时器间隔 5-10 秒（间隔太短差分不准确）

网络吞吐量
    └──requires──> 两次 NetworkIF 采样差分计算

进程 Top N
    └──requires──> OSHI getProcesses(limit, sort) 调用

图表可视化
    └──requires──> ECharts 依赖集成
    └──enhances──> 历史数据存储（趋势图需要历史数据）

历史数据 ──enhances──> 趋势图表（图表的完整度依赖历史数据长度）
实时数据 ──enhances──> 仪表盘/Gauge（仪表盘只需要当前值无需历史）
阈值告警 ──enhances──> 通知渠道（邮件/钉钉/企业微信，需要额外集成）
历史数据 ──conflicts──> 存储成本（数据保留越长存储越高，需权衡）
高频采集 ──conflicts──> 系统性能（采集频率与监控开销是 trade-off）
```

## MVP 定义（初始版本）

### 首次发布必须（P0）

核心监控能力，实现后页面即可投入基本使用：

- [ ] **OSHI 核心集成** — `SystemInfo` 单例作为 Spring Bean 注入
  - `OshiConfig` 配置类创建 `SystemInfo`、`HardwareAbstractionLayer`、`OperatingSystem`
  - 依赖：`oshi-core` 6.7.0+（Maven/Gradle）
  - **复杂度**: 低
- [ ] **定时采集服务** — `@Scheduled` 每 3 秒采集一次全量指标
  - 缓存到 `ConcurrentHashMap`，API 从缓存读
  - CPU（总体使用率）、内存（总/已用/可用/百分比）、系统信息
  - **复杂度**: 中（需注意 CPU 采样需要等待 1 秒做差分）
- [ ] **核心指标 REST API** — `/api/monitor/metrics` 返回实时快照
  - 返回 JSON: `{cpu, memory, disk, network, system, timestamp}`
  - 不直接调 OSHI，从缓存读取
  - **复杂度**: 低
- [ ] **监控页面路由和布局** — Vue Router + 监控页面组件
  - 组件路径: `views/monitor/MonitorPage.vue`
  - 路由: `/monitor` → 监控页面
  - **复杂度**: 低
- [ ] **CPU + 内存仪表盘** — 两个 ECharts Gauge 图表
  - 显示实时 CPU 和内存使用率百分比
  - 颜色区间: 绿色(<60%) / 黄色(60-85%) / 红色(>85%)
  - **复杂度**: 中（ECharts Gauge 配置）

### 第二优先级（P1）

- [ ] **磁盘监控区域** — 每个磁盘的容量条 + 信息卡片
  - 显示: 盘符/型号、总容量、已用、可用、使用率
  - 使用率 > 90% 时标红
  - **复杂度**: 中
- [ ] **系统信息卡片** — OS 名称、版本、运行时间、进程数
  - 从 OSHI `OperatingSystem` 获取
  - **复杂度**: 低
- [ ] **实时刷新机制** — 前端通过 SSE 或轮询定期更新
  - 推荐 SSE（与现有秒杀系统的 SSE 模式一致）
  - 参考: 当前项目 `SseController.java` 的实现模式
  - **复杂度**: 中（SSE 服务端推送 + 前端 EventSource 管理）
- [ ] **折线趋势图** — 近 1 小时的 CPU 和内存趋势
  - 前端维护环形缓冲区（max 360 个点，10 秒一个点 = 1 小时）
  - 无需后端存储，纯内存
  - **复杂度**: 中（ECharts Line + 前端数据累积）

### 延后处理（P2）

- [ ] **磁盘 IOPS + 读写速度** — 每个磁盘的差分计算
  - 需后端维护上次采样的磁盘状态
  - **复杂度**: 中（差分计算需要额外状态管理）
- [ ] **网络吞吐量** — 上传/下载速度实时图表
  - 与磁盘 IOPS 相同：差分 + 前端图表
  - **复杂度**: 中
- [ ] **CPU 温度（Windows 特定）** — 显示 CPU 温度
  - 需要 OSHI 6.7.0+ + jLibreHardwareMonitor 依赖
  - 部分硬件不支持，需优雅降级
  - **复杂度**: 中（硬件兼容性问题）
- [ ] **进程 Top N** — 按 CPU/内存排序的进程列表
  - 30 秒刷新一次 + 手动刷新按钮
  - **复杂度**: 中（OSHI `getProcesses()` 带 limit 和排序）
- [ ] **历史数据持久化** — 存储指标到数据库
  - 建议: 存在当前项目的 MySQL 中 + 定时清理
  - 或: 引入 InfluxDB（范围较大，需单独评估）
  - **复杂度**: 高
- [ ] **CPU 每个核心使用率** — 每个核心的负载折线
  - 多线图展示每个逻辑核心
  - **复杂度**: 中（需 `getProcessorCpuLoadBetweenTicks()` 数组）
- [ ] **阈值告警（页面内通知）** — 超过阈值时页面提示
  - 后端检查 + SSE 推送告警事件
  - **复杂度**: 中
- [ ] **导出历史数据 CSV** — 按日期范围导出
  - 需要历史数据存储完成后才能做
  - **复杂度**: 中
- [ ] **大屏模式** — 全屏自适应
  - CSS `fullscreen` API + 大字号布局
  - **复杂度**: 低

## 优先级矩阵

| 特征 | 价值（用户感知） | 实施成本 | 优先级 |
|------|----------------|---------|--------|
| OSHI 核心集成 | N/A（基础依赖） | 低 | P0 |
| 定时采集服务 | 高（一切数据的基础） | 中 | P0 |
| CPU 仪表盘 | 高（核心指标） | 中 | P0 |
| 内存仪表盘 | 高（核心指标） | 中 | P0 |
| 磁盘容量展示 | 高（核心指标） | 中 | P1 |
| 系统信息卡片 | 中（确认基本信息） | 低 | P1 |
| 实时刷新（SSE） | 高（自动更新） | 中 | P1 |
| 折线趋势图（内存） | 高（历史趋势） | 中 | P1 |
| 磁盘 IOPS/速度 | 中（IO 排查场景） | 中 | P2 |
| 网络吞吐量 | 中（网络排查场景） | 中 | P2 |
| CPU 温度 | 低（硬件兼容性差） | 中 | P2 |
| 进程 Top N | 中（问题排查） | 中 | P2 |
| 历史数据持久化 | 高（回溯分析） | 高 | P2 |
| 单核 CPU 使用率 | 中（性能调优场景） | 中 | P2 |
| 阈值告警 | 中（被动通知） | 中 | P2 |
| 大屏模式 | 低（非核心场景） | 低 | P2 |
| GPU 监控 | 低（当前项目无 ML 场景） | 高 | 延后 |

**优先级说明:**
- P0: 初始发布必须，产出可用页面
- P1: 可用页面变成"好用"的页面
- P2: 锦上添花，可单独开里程碑

## 与现有项目的集成点

| 已有系统/组件 | 复用方式 | 适配说明 |
|-------------|---------|---------|
| `SseController.java`（秒杀系统） | 复用 SSE 推送模式 | 新建 `MonitorSseController.java` 独立端点 `/api/monitor/sse`，避免与秒杀系统的 SSE 端点冲突 |
| ECharts（项目中已有） | 复用 ECharts 依赖 | 项目已有 ECharts，直接使用；需要验证版本兼容 |
| Spring Boot `@Scheduled` | 定时采集 | 新增 `MonitorScheduler.java` 使用 `@Scheduled(fixedRate = 3000)` |
| Vue Router | 新增路由 | `routes/modules/monitor.ts` + 侧边栏菜单 |
| Axios API 模块 | 新建 API 文件 | `api/modules/monitor.ts` |
| Element Plus el-progress | 磁盘容量进度条 | 现成组件，可直接用 |
| `shared/config/` 配置拆分模式 | 新增 `monitor.yaml` | 遵循已有配置拆分模式 |

## 来源

- [OSHI GitHub](https://github.com/oshi/oshi) — 跨平台硬件信息库，纯 Java（JNA）
- [OSHI 官方文档 - Sensors](https://www.oshi.ooo/oshi-core-java11/xref/oshi/hardware/Sensors.html) — CPU 温度 API
- [OSHI 性能优化指南](https://github.com/coolsky-lin/oshi/blob/master/src/site/markdown/Performance.md) — 缓存策略和频率建议
- [OSHI Windows 实现文档](https://deepwiki.com/oshi/oshi/4.1-windows-implementation) — PDH vs WMI 优先级链
- [Hutool OshiUtil](https://doc.hutool.cn/pages/OshiUtil/) — 便捷封装层
- [SpringBoot + OSHI 实现服务器资源监控 (掘金)](https://juejin.cn/post/7243748398566801464) — 集成实践
- [避开 SpringBoot+OSHI 监控开发中的 5 个常见问题](https://blog.csdn.net/m2n3b4v5c6/article/details/154560726) — 常见陷阱
- [Ward - Server Dashboard (GitHub)](https://github.com/Rudolf-Barbu/Ward) — 使用 OSHI + Spring Boot 的监控仪表盘参考项目
- [ServerStatus- (GitHub)](https://github.com/yyft6919/ServerStatus-) — Vue 3 + Spring Boot + WebSocket 监控系统参考
- [Beszel (GitHub)](https://github.com/henrygd/beszel) — 轻量级服务器监控 Hub+Agent 架构
- [Netdata: 'Monitor Everything' is an Anti-Pattern](https://www.netdata.cloud/resources/research/monitor-everything-anti-pattern/) — 监控反模式研究
- [Google SRE 四大黄金信号](https://sre.google/sre-book/monitoring-distributed-systems/) — USE/RED 监控方法论
- [InfluxDB vs MySQL 时序数据性能对比](https://www.influxdata.com/comparison/influxdb-vs-mysql/) — 存储选型依据
- [Vue3 使用 ECharts 仪表盘](https://developer.aliyun.com/article/1600543) — Gauge 图表配置

---
*特征研究用于: Windows 硬件负载监控功能规划*
*研究日期: 2026-05-09*
