# Ocean Breeze Admin - 路线图

> **创建日期:** 2026-04-03
> **更新日期:** 2026-04-28
> **当前里程碑:** v1.3 RocketMQ 管理功能
> **目标:** 在现有管理后台中集成 RocketMQ 管理和监控功能

---

## Phases

- [ ] **Phase 9: Topic 管理** - Topic 列表、详情、创建、删除
- [ ] **Phase 10: Consumer Group 管理** - Group 列表、详情、重置位点
- [x] **Phase 11: 消息管理** - 消息查询、详情、轨迹追踪
- [ ] **Phase 12: 监控面板** - 集群概览、Broker状态、堆积监控、QPS图表

---

## Phase Details

### Phase 9: Topic 管理

**Goal:** 实现 Topic 的查看、创建、删除功能

**Depends on:** 无

**Requirements:** TOPIC-01, TOPIC-02, TOPIC-03, TOPIC-04

**Success Criteria** (what must be TRUE):
1. 用户可以在表格中看到所有 Topic 的名称、队列数、状态、消息数量
2. 用户可以点击 Topic 查看详细信息（配置、路由）
3. 用户可以创建新的 Topic（填写名称、队列数）
4. 用户可以删除 Topic（需确认提示）

**Plans:** 1 plan
- [ ] 09-01-PLAN.md — Topic 管理功能实现

**UI hint:** yes

---

### Phase 10: Consumer Group 管理

**Goal:** 实现 Consumer Group 的查看和位点重置功能

**Depends on:** Phase 9

**Requirements:** CONS-01, CONS-02, CONS-03

**Success Criteria** (what must be TRUE):
1. 用户可以在表格中看到所有 Consumer Group 的名称、类型、状态
2. 用户可以点击 Group 查看详细信息（消费进度、订阅关系、位点）
3. 用户可以重置消费位点（按时间戳或指定位点）

**Plans:** 1 plan
- [ ] 10-01-PLAN.md — Consumer Group 管理功能实现

**UI hint:** yes

---

### Phase 11: 消息管理

**Goal:** 实现消息查询、详情查看和轨迹追踪

**Depends on:** Phase 9

**Requirements:** MSG-01, MSG-02, MSG-03

**Success Criteria** (what must be TRUE):
1. 用户可以按 Topic 和时间范围查询消息列表
2. 用户可以点击消息查看详情（ID、内容、属性、Key、Tag）
3. 用户可以追踪消息的生产→消费全链路

**Plans:** 1 plan
- [x] 11-01-PLAN.md — 消息管理功能实现

**UI hint:** yes

---

### Phase 12: 监控面板

**Goal:** 实现集群监控、Broker 状态、堆积监控和实时图表

**Depends on:** Phase 9

**Requirements:** MON-01, MON-02, MON-03, MON-04, MON-05

**Success Criteria** (what must be TRUE):
1. 用户可以看到集群概览（Broker数量、Topic数量、Consumer数量、堆积总量）
2. 用户可以查看各 Broker 的状态（在线/离线、版本、运行时间）
3. 用户可以查看各 Topic 的消息堆积量
4. 用户可以查看实时 QPS 监控图表（生产/消费）
5. 用户可以查看延迟统计（发送延迟、消费延迟）

**Plans:** 1 plan
- [ ] 12-01-PLAN.md — 监控面板功能实现

**UI hint:** yes

---

## Progress Table

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 9. Topic 管理 | 1/1 | Completed | 2026-04-20 |
| 10. Consumer Group | 1/1 | Completed | 2026-04-28 |
| 11. 消息管理 | 1/1 | Planned | — |
| 12. 监控面板 | 0/1 | Pending | — |

---

## Coverage Map

```
TOPIC-01 → Phase 9
TOPIC-02 → Phase 9
TOPIC-03 → Phase 9
TOPIC-04 → Phase 9
CONS-01 → Phase 10
CONS-02 → Phase 10
CONS-03 → Phase 10
MSG-01 → Phase 11
MSG-02 → Phase 11
MSG-03 → Phase 11
MON-01 → Phase 12
MON-02 → Phase 12
MON-03 → Phase 12
MON-04 → Phase 12
MON-05 → Phase 12

Mapped: 15/15 ✓
```

---

## v1.3 执行顺序

```
Phase 9 (Topic) → Phase 10 (Consumer) ─┬─→ Phase 11 (消息)
                                       │           ↓
                                       └────────→ Phase 12 (监控)
```

**说明:**
- Phase 9 独立，是后续所有阶段的基础
- Phase 10、11、12 可并行开发（都依赖 Phase 9 的 Topic API）

---

*路线图创建完成: 2026-04-28*
*等待用户批准后开始执行*
