# Requirements: Ocean Breeze Admin - RocketMQ 管理

**Defined:** 2026-04-28
**Core Value:** 提供清晰、高效的企业级管理后台界面，通过夏日海滩风主题营造清爽专业的视觉体验。

## v1 Requirements

### MQ-TOPIC: Topic 管理

- [ ] **TOPIC-01**: 查看 Topic 列表（名称、队列数、状态、消息数量）
- [ ] **TOPIC-02**: 查看 Topic 详情（配置信息、路由信息）
- [ ] **TOPIC-03**: 创建 Topic（名称、队列数、权限设置）
- [ ] **TOPIC-04**: 删除 Topic（确认提示）

### MQ-CONS: Consumer Group 管理

- [ ] **CONS-01**: 查看 Consumer Group 列表（Group名、类型、状态）
- [ ] **CONS-02**: 查看 Consumer Group 详情（消费进度、订阅关系、位点信息）
- [ ] **CONS-03**: 重置消费位点（按时间戳/位点）

### MQ-MSG: 消息管理

- [ ] **MSG-01**: 按 Topic 和时间范围查询消息列表
- [ ] **MSG-02**: 查看消息详情（消息ID、内容、属性、Key、Tag）
- [ ] **MSG-03**: 消息轨迹追踪（生产→消费全链路）

### MQ-MON: 监控面板

- [ ] **MON-01**: 集群概览（Broker数量、Topic数量、Consumer数量、堆积总量）
- [ ] **MON-02**: Broker 状态监控（在线/离线、版本、运行时间）
- [ ] **MON-03**: 消息堆积监控（各 Topic 堆积量）
- [ ] **MON-04**: 实时 QPS 监控图表（生产/消费）
- [ ] **MON-05**: 延迟统计（发送延迟、消费延迟）

## v2 Requirements

暂未规划。

## Out of Scope

| Feature | Reason |
|---------|--------|
| 消息重发/补偿 | 需要业务场景确认，风险较高 |
| 死信队列管理 | 后期根据实际需求添加 |
| 多集群管理 | 单集群足够满足当前需求 |
| 消息内容修改 | 修改消息内容有风险，保留只读 |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| TOPIC-01 | Phase 1 | Pending |
| TOPIC-02 | Phase 1 | Pending |
| TOPIC-03 | Phase 1 | Pending |
| TOPIC-04 | Phase 1 | Pending |
| CONS-01 | Phase 2 | Pending |
| CONS-02 | Phase 2 | Pending |
| CONS-03 | Phase 2 | Pending |
| MSG-01 | Phase 3 | Pending |
| MSG-02 | Phase 3 | Pending |
| MSG-03 | Phase 3 | Pending |
| MON-01 | Phase 4 | Pending |
| MON-02 | Phase 4 | Pending |
| MON-03 | Phase 4 | Pending |
| MON-04 | Phase 4 | Pending |
| MON-05 | Phase 4 | Pending |

**Coverage:**
- v1 requirements: 15 total
- Mapped to phases: 15
- Unmapped: 0 ✓

---
*Requirements defined: 2026-04-28*
*Last updated: 2026-04-28 after initial definition*
