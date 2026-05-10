# Requirements: Vue + Spring Boot 管理后台

**Defined:** 2026-05-09
**Core Value:** 提供清晰、高效的企业级管理后台界面，通过夏日海滩风主题营造清爽专业的视觉体验。

## v1.8 Requirements

Windows 系统硬件负载监控页面，实时展示 CPU/内存/磁盘/网络指标。

### 后端服务

- [ ] **HWM-01**: 系统集成 OSHI 7.x FFM 库，在 JDK 26 上正常采集硬件指标
- [ ] **HWM-02**: 后端定时采集 CPU 使用率（2s 间隔），支持总体使用率和各核使用率
- [ ] **HWM-03**: 后端定时采集内存指标（总量、已用、可用
- 、使用率）
- [ ] **HWM-04**: 后端定时采集磁盘容量和分区信息
- [ ] **HWM-05**: 后端定时采集磁盘 IOPS 和读写速度
- [ ] **HWM-06**: 后端定时采集网络接口吞吐量（上下行速率）
- [x] **HWM-07**: 后端提供 REST API 返回当前硬件指标快照（31-03-PLAN）
- [x] **HWM-08**: 后端维护内存环形缓冲区（360 点 = 1h），支持趋势数据查询（31-01/31-03-PLAN）
- [x] **HWM-09**: 后端采集系统基本信息（OS 版本、运行时间、进程数）（31-03-PLAN）

### 实时推送

- [ ] **HWM-10**: 后端通过 SSE 端点实时推送硬件指标数据
- [ ] **HWM-11**: SSE 连接实现心跳检测和过期连接自动清理

### 前端页面

- [ ] **HWM-12**: 前端硬件监控页面路由和菜单接入
- [ ] **HWM-13**: CPU 使用率仪表盘环形图 + 核数/型号信息卡片
- [ ] **HWM-14**: 内存使用率仪表盘环形图 + 总量/已用/可用信息
- [ ] **HWM-15**: 磁盘分区容量进度条列表
- [ ] **HWM-16**: 磁盘 IO / 网络吞吐量实时折线图
- [ ] **HWM-17**: 系统基本信息卡片（OS 版本、运行时间、进程数）
- [ ] **HWM-18**: 近 1 小时趋势折线图（ECharts 滑动窗口）
- [ ] **HWM-19**: 前端 SSE 连接管理（自动重连、加载态、错误处理）
- [ ] **HWM-20**: 夏日海滩风主题适配（配色与现有主题一致）

### 安全与打磨

- [ ] **HWM-21**: 硬件监控 API 需要 ADMIN 角色权限
- [ ] **HWM-22**: ECharts 实例生命周期管理（dispose、markRaw、300 点上限）
- [ ] **HWM-23**: 页面加载态、空状态、异常状态处理
- [ ] **HWM-24**: SSE 断线重连时 UI 状态提示

### TDD 质量保障

- [ ] **TDD-01**: 后端采集服务设计为接口模式，OSHI 依赖可 mock，便于单元测试
- [ ] **TDD-02**: SSE 推送服务设计为可测试的接口抽象，支持 mock 客户端验证
- [ ] **TDD-03**: 后端核心逻辑（采集、计算、缓存）有单元测试覆盖，先写测试后实现
- [ ] **TDD-04**: 前端 Vue 组件逻辑（composable、图表数据处理）有单元测试覆盖
- [ ] **TDD-05**: SSE 连接管理 composable 设计为可测试的接口，支持模拟事件源

## 未来需求

- **HWM-F01**: CPU 温度监控（需 jLibreHardwareMonitor）
- **HWM-F02**: GPU 指标监控（需额外依赖）
- **HWM-F03**: 历史数据持久化（MySQL 分区表或 InfluxDB）
- **HWM-F04**: 阈值告警配置
- **HWM-F05**: 进程 Top N 列表

## 排除范围

| 功能 | 原因 |
|------|------|
| CPU 温度监控 | 需额外 jLibreHardwareMonitor DLL，稳定性待验证 |
| GPU 指标 | OSHI 核心模块不直接支持 |
| 阈值告警 | V1 聚焦展示，告警放入后续迭代 |
| 历史数据持久化 | 初期环形缓冲区足够，运行后评估再决定 |
| 跨平台（Linux） | 本里程碑定位为 Windows 专用 |
| 移动端适配 | 监控页为桌面端使用场景 |

## 追踪

| 需求 | 阶段 | 状态 |
|------|------|------|
| HWM-01 | Phase 31 | Pending |
| HWM-02 | Phase 31 | Pending |
| HWM-03 | Phase 31 | Pending |
| HWM-04 | Phase 31 | Pending |
| HWM-05 | Phase 31 | Pending |
| HWM-06 | Phase 31 | Pending |
| HWM-07 | Phase 31 | Complete |
| HWM-08 | Phase 31 | Complete |
| HWM-09 | Phase 31 | Complete |
| HWM-10 | Phase 32 | Pending |
| HWM-11 | Phase 32 | Pending |
| HWM-12 | Phase 32 | Pending |
| HWM-13 | Phase 32 | Pending |
| HWM-14 | Phase 32 | Pending |
| HWM-15 | Phase 32 | Pending |
| HWM-16 | Phase 33 | Pending |
| HWM-17 | Phase 32 | Pending |
| HWM-18 | Phase 33 | Pending |
| HWM-19 | Phase 32 | Pending |
| HWM-20 | Phase 33 | Pending |
| HWM-21 | Phase 34 | Pending |
| HWM-22 | Phase 33 | Pending |
| HWM-23 | Phase 34 | Pending |
| HWM-24 | Phase 34 | Pending |
| TDD-01 | Phase 31 | Pending |
| TDD-02 | Phase 32 | Pending |
| TDD-03 | Phase 31 | Pending |
| TDD-04 | Phase 33 | Pending |
| TDD-05 | Phase 32 | Pending |

**覆盖度：**
- v1.8 需求：29 条
- 已映射到阶段：29
- 未映射：0

---

*需求定义: 2026-05-09*
