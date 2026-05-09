# 调研摘要：Windows 硬件负载监控

2**调研日期:** 2026-05-09
**置信度:** HIGH

---

## 技术栈

| 组件 | 选型 | 版本 | 说明 |
|------|------|------|------|
| 系统信息库 | OSHI | 7.1.0+ | 必须 `oshi-core-ffm`（FFM API），JDK 26 禁用 JNA |
| 图表库 | ECharts + vue-echarts | 6.x / 8.x | 项目已有，复用 |
| 实时推送 | SSE | — | 复用 SeckillSseService 模式 |
| 历史存储 | MySQL 分区表 | — | 初期够用，P2 评估 InfluxDB |

## 特征集（MVP）

| 优先级 | 指标 | 采集方式 | 展示方式 |
|--------|------|----------|----------|
| P0 | CPU 使用率（总体） | OSHI CentralProcessor | 仪表盘环形图 + 趋势线 |
| P0 | 内存使用率 | OSHI GlobalMemory | 仪表盘环形图 + 趋势线 |
| P0 | 磁盘容量使用率 | OSHI HWDiskStore + OSFileStore | 进度条列表 |
| P1 | CPU 核数/型号/频率 | OSHI CentralProcessor | 信息卡片 |
| P1 | 系统运行时间/进程数 | OSHI OperatingSystem | 信息卡片 |
| P1 | 实时 SSE 推送 | SSE | 自动刷新 |
| P1 | 趋势图（近 1h 环形缓冲区） | 内存环形缓冲区（360 点） | ECharts 折线图 |
| P2 | 磁盘 IOPS / 读写速度 | OSHI HWDiskStore | 折线图 |
| P2 | 网络吞吐量 | OSHI NetworkIF | 折线图 |
| P2 | 历史持久化 | MySQL 分区表 | 时间范围选择 |
| P2 | CPU 温度 | OSHI + jLibreHardwareMonitor | 仪表盘 |
| P3 | GPU 指标 | 需额外依赖 | 后续评估 |

**排除项（v1）**：CPU 温度、GPU 指标、进程 Top N、阈值告警

## 关键架构决策

1. **OSHI 7.x FFM**（非 6.x JNA）— JDK 26 JEP 472 禁止 JNA
2. **monitor/hardware/** 新包 — 不与 RocketMQ 监控耦合
3. **Dedicated collector thread** — `@Scheduled` 写入 volatile cache，避免线程安全
4. **SSE 扩展** — 缩短超时 + 心跳 + 定期清理过期 emitter
5. **ECharts 滑动窗口** — 300 点上限 + markRaw + dispose

## 主要风险

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| OSHI 7.x FFM JDK 26 兼容 | 高 | Phase 1 写单元测试验证 |
| Windows 管理员权限 | 中 | 文档说明权限要求 |
| CPU 采样 < 1s 返回 0 | 中 | 强制 2s 采集间隔 |
| SSE 连接泄漏 | 中 | 心跳 + 定期 sweep |
| ECharts 内存泄漏 | 中 | markRaw + 滑动窗口 |

## 构建顺序

1. **Phase 1**: 后端 OSHI 集成 + 定时采集 + REST API（CPU/内存/磁盘）
2. **Phase 2**: SSE 推送 + 前端页面 + 仪表盘组件
3. **Phase 3**: 完整指标（磁盘 IO/网络）+ 趋势图
4. **Phase 4**: 打磨（加载态/重连/安全加固/历史数据）

---

*由 Architecture/Features/Pitfalls 调研合成*
