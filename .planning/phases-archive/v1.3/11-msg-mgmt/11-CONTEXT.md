# Phase 11: 消息管理 - Context

**Gathered:** 2026-04-29
**Status:** Ready for planning

<domain>
## Phase Boundary

实现消息查询、详情查看和轨迹追踪。用户可以按 Topic 和时间范围查询消息列表，可以点击消息查看详情，可以追踪消息的生产→消费全链路。

</domain>

<decisions>
## Implementation Decisions

### 消息查询方式
- **D-01:** 支持两种查询方式：
  1. 按 Topic + 时间范围查询（主方式）
  2. 按 msgId 直接查询（辅助方式）
- **D-02:** 时间范围限制：最多查询 7 天内的消息（避免性能问题）
- **D-03:** 分页：每页最多 100 条消息，支持翻页

### 消息列表展示
- **D-04:** 表格列：消息ID、Tags、Keys、时间、QueueId、Offset
- **D-05:** 支持按 Topic 筛选（必选）和时间范围筛选（必选）
- **D-06:** 消息 ID 点击可打开详情弹窗

### 消息详情弹窗
- **D-07:** 显示完整字段：
  - 消息ID (msgId)
  - Topic
  - Tags
  - Keys
  - 时间 (StoreTimestamp)
  - QueueId
  - QueueOffset
  - Properties（展开为表格）
  - Body（截断显示，支持查看完整）
- **D-08:** 使用 el-dialog 弹窗展示

### 消息轨迹追踪
- **D-09:** 使用表格形式展示轨迹
- **D-10:** 表格列：Consumer Group、消费状态、消费时间、堆积量
- **D-11:** 轨迹数据来源：RocketMQ 内置 trace topic (RMQ_SYS_TRACE_TOPIC)

### 技术架构
- **D-12:** 后端：扩展 RocketMQAdminService，新增 getMessageList()、getMessageDetail()、getMessageTrace()
- **D-13:** 前端：新建 MessageList.vue（列表页）、MessageDetailDialog.vue（详情弹窗）、MessageTraceDialog.vue（轨迹弹窗）
- **D-14:** API 路径：
  - `GET /api/rocketmq/messages?topic=X&startTime=X&endTime=X&maxMsg=100`
  - `GET /api/rocketmq/messages/{topic}/{msgId}`
  - `GET /api/rocketmq/messages/{topic}/{msgId}/trace`

### OpenCode's Discretion
- 消息 body 的截断长度
- 分页 UI 的具体样式（el-pagination 或 load more 按钮）
- 轨迹表格的详细布局

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### 项目规范
- `.planning/PROJECT.md` — 项目愿景和技术约束
- `.planning/REQUIREMENTS.md` — MQ-MSG 相关需求定义
- `.planning/ROADMAP.md` — Phase 11 的 Success Criteria

### Phase 9/10 上下文（继承决策）
- `.planning/phases/09-topic-mgmt/09-CONTEXT.md` — Admin API 客户端使用 rocketmq-client-apache
- `.planning/phases/10-consumer-group-mgmt/10-CONTEXT.md` — 前端组件模式（el-table、el-dialog）
- `springboot/src/main/java/cn/coderstory/springboot/service/RocketMQAdminService.java` — Admin API 封装
- `app-vue/src/api/rocketmq.ts` — 前端 API 调用模式

### 后端代码模式
- `springboot/src/main/java/cn/coderstory/springboot/controller/RocketMQController.java` — Controller 模式参考
- `springboot/src/main/java/cn/coderstory/springboot/service/impl/RocketMQAdminServiceImpl.java` — Service 实现模式

### 前端代码模式
- `app-vue/src/views/rocketmq/TopicList.vue` — 列表页面模式
- `app-vue/src/views/rocketmq/ConsumerGroupDetail.vue` — 详情弹窗模式

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- Element Plus el-table：消息列表复用
- Element Plus el-dialog：详情弹窗复用
- Element Plus el-date-picker：时间范围选择复用
- Element Plus el-pagination：分页复用

### Established Patterns
- REST API 风格：@RestController + @GetMapping
- Service 层：接口 + impl 实现类
- 前端 API 调用：扩展 api/rocketmq.ts
- 弹窗模式：外层 el-dialog + 内层表单/表格

### Integration Points
- 侧边栏菜单：RocketMQ 管理入口已存在（Phase 9）
- 路由：/rocketmq/messages（新建）
- 权限控制：复用现有 RBAC 体系

</code_context>

<specifics>
## Specific Ideas

- 时间范围选择使用 el-date-picker type="datetimerange"
- 消息 ID 列使用 monospace 字体显示
- 轨迹表格按消费时间排序（最新的在最上面）

</specifics>

<deferred>
## Deferred Ideas

- 消息内容修改（Phase 范围外）
- 消息重发/补偿（风险较高，需要业务场景确认）
- 死信队列管理（后期根据实际需求添加）

</deferred>

---
*Phase: 11-msg-mgmt*
*Context gathered: 2026-04-29*