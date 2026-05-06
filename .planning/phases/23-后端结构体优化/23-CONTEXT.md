# Phase 23: 后端结构体优化 - Context

**Gathered:** 2026-05-07
**Status:** Ready for planning

<domain>
## Phase Boundary

后端代码结构与目录重构 — 合并零散工具类、拆分臃肿大类、删除无用 DTO/VO/实体字段、精简 Mapper XML、移除未引用的 Service 方法；将目录从 domain-first 重构为 type-first。

**要求（来自 REQUIREMENTS.md）：**
- BAC-04: 合并过于零散的辅助工具类
- BAC-05: 删除未使用的 DTO/VO 类及冗余的实体字段
- BAC-07: 精简冗余的 Mapper XML 映射配置
- BAC-08: 检查并删除未被任何 Controller 引用的 Service 方法
- BAC-09: 拆分过于臃肿的大类（上帝类）
- BAC-10: 将后端目录从 domain-first 重构为 type-first

</domain>

<spec_lock>
## Locked Requirements

以下决策来自用户讨论，PLAN.md 必须全部覆盖：

| 需求 | 决策 |
|------|------|
| BAC-04 | 工具类合并 — ZstdUtil 仅有的显式 Util，暂无合并必要，验证后确认 |
| BAC-05 | DTO/VO + 实体字段 + Mapper XML 全面清理 |
| BAC-06 | （Phase 22 已完成） |
| BAC-07 | 全面清理 Mapper XML 冗余配置 |
| BAC-08 | 严格标准：全局搜索确认无任何调用才删除 |
| BAC-09 | 拆分 RocketMQAdminServiceImpl（1098行）按功能域（Topic/Consumer/Message/Cluster）拆分；ActivityServiceImpl(460) 和 SeckillServiceImpl(350) 保持不动 |
| BAC-10 | 全量重构：controller/service/mapper/entity 全部移动，10个域一次性完成 |

</spec_lock>

<decisions>
## Implementation Decisions

### 上帝类拆分策略（BAC-09）
- **D-01:** 仅拆分 `RocketMQAdminServiceImpl`（1098行），按 RocketMQ 功能域拆分为 4 个类
- **D-02:** 拆分维度：TopicService、ConsumerService、MessageService、ClusterService
- **D-03:** `ActivityServiceImpl`（460行）和 `SeckillServiceImpl`（350行）保持不动

### type-first 目录重构（BAC-10）
- **D-04:** 全量重构 — controller/service/mapper/entity 全部移动，10 个域一次性完成
- **D-05:** 新结构：`controller/{domain}/`、`service/{domain}/`、`mapper/{domain}/`、`entity/{domain}/`
- **D-06:** 使用 `git mv` 分步移动，每步 `./gradlew.bat build` 验证
- **D-07:** 同步更新所有 `package` 声明和 `import` 引用
- **D-08:** seckill 域中 `mq/`、`sse/`、`stock/`、`dto/`、`vo/` 子包也需对应调整

### DTO/VO/XML 清理（BAC-05, BAC-07）
- **D-09:** 全面清理 — DTO/VO 类、实体冗余字段、Mapper XML 全部检查
- **D-10:** 删除前全局搜索确认无反射/序列化引用

### Service 方法删除（BAC-08）
- **D-11:** 严格标准 — 仅在确认不被任何 Controller 调用、也不被其他 Service 调用时才删除
- **D-12:** 每个待删方法做全局搜索确认（java/lua/yaml/yml 文件）

### 工具类合并（BAC-04）
- **D-13:** 当前仅 `ZstdUtil` 一个显式 Util 类，验证后无需合并
- **D-14:** shared/limiter、shared/lock 等服务本身就是独立类，不属于"零散工具类"

### 待办事项
- **T-01:** MonitorController 注入 MonitorService 替代直接操作 Redis（见 todo）
- **T-02:** 新增 GET /api/monitor/stock/{goodsId} 端点

### Claude's Discretion
- RocketMQ 拆分后 4 个类的具体职责划分细节
- 目录重构的 `git mv` 分批顺序
- DTO/VO 中哪些字段算"冗余"的具体判断

</decisions>

<specifics>
## Specific Ideas

- BAC-10（type-first）是工作量最大的项，涉及全局包名和 import 修改，建议放在最后执行
- RocketMQ 拆分建议先于目录重构，否则移动后再拆分更复杂
- 目录重构使用 `git mv` + 全局 sed 替换 package 声明 + IDE 自动优化 import

</specifics>

<canonical_refs>
## Canonical References

### 要求定义
- `.planning/REQUIREMENTS.md` §BAC-04~BAC-10 — Phase 23 的具体清理要求
- `.planning/ROADMAP.md` §Phase 23 — 阶段目标和成功标准

### 代码基线
- `springboot/src/main/java/cn/coderstory/springboot/` — 后端全部 121 个 Java 源文件
- `springboot/src/main/resources/mapper/` — 5 个 Mapper XML 文件

### 待办事项
- `.planning/todos/pending/monitor-service-wiring.md` — MonitorService 未使用的待办修复

</canonical_refs>

<code_context>
## Existing Code Insights

### 上帝类候选
| 类 | 行数 | 拆分策略 |
|----|------|---------|
| RocketMQAdminServiceImpl | 1098 | 按 Topic/Consumer/Message/Cluster 拆为 4 类 |

### DTO/VO 类
- `user/dto/UserVO.java` — 用户视图对象
- `seckill/dto/SeckillRequest.java` — 秒杀请求 DTO
- `seckill/dto/SeckillResponse.java` — 秒杀响应 DTO
- `seckill/vo/ActivityDetailVO.java` — 活动详情 VO

### 当前目录结构（10 个域）
```
{domain}/controller/   →   controller/{domain}/
{domain}/service/      →   service/{domain}/
{domain}/mapper/       →   mapper/{domain}/
{domain}/entity/       →   entity/{domain}/
```

### Mapper XML 文件
- `mapper/KnowledgeArticleMapper.xml`
- `mapper/KnowledgeArticleTagMapper.xml`
- `mapper/KnowledgeCategoryMapper.xml`
- `mapper/MenuMapper.xml`
- `mapper/UserMapper.xml`

</code_context>

<deferred>
## Deferred Ideas

None — 讨论严格保持在 Phase 23 范围内。

</deferred>

---

*Phase: 23-后端结构体优化*
*Context gathered: 2026-05-07*
