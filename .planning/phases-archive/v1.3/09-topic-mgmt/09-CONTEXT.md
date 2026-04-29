# Phase 9: Topic 管理 - Context

**Gathered:** 2026-04-28
**Status:** Ready for planning

<domain>
## Phase Boundary

实现 Topic 的查看、创建、删除功能。用户可以在表格中看到所有 Topic，可以创建新的 Topic，可以删除已有 Topic。

</domain>

<decisions>
## Implementation Decisions

### Admin API 客户端
- **D-01:** 使用 rocketmq-client-apache 调用 RocketMQ Admin API
- **D-02:** 通过 Spring Boot 配置注入 MQAdmin instance

### 列表页展示
- **D-03:** 表格列：名称、队列数、状态、消息数量、创建时间
- **D-04:** 支持按名称模糊筛选

### 创建 Topic 表单
- **D-05:** 必填字段：Topic 名称
- **D-06:** 可选字段：队列数（默认 8）、权限设置（默认 READ）
- **D-07:** 表单验证：名称不能为空，不能包含特殊字符

### 删除保护机制
- **D-08:** 删除前检查该 Topic 是否有活跃的 Consumer Group 订阅
- **D-09:** 显示确认对话框，提示影响范围
- **D-10:** 输入 Topic 名称进行二次确认

### 技术架构
- **D-11:** 后端：新建 RocketMQController + RocketMQAdminService
- **D-12:** 前端：复用现有 Element Plus el-table、el-dialog、el-form 组件
- **D-13:** API 路径：/api/rocketmq/topics (GET/POST/DELETE)

### 异常处理
- **D-14:** Topic 正在被使用时禁止删除，提示用户先停止 Consumer
- **D-15:** 网络异常时显示友好错误提示，不暴露内部错误

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### 项目规范
- `.planning/PROJECT.md` — 项目愿景和技术约束
- `.planning/REQUIREMENTS.md` — MQ-TOPIC 相关需求定义
- `.planning/ROADMAP.md` — Phase 9 的 Success Criteria

### 后端代码模式
- `springboot/src/main/java/cn/coderstory/springboot/controller/` — 现有 Controller 模式参考
- `springboot/src/main/java/cn/coderstory/springboot/service/impl/` — Service 实现模式
- `springboot/src/main/resources/application.yaml` — RocketMQ 配置（name-server: localhost:9876）

### 前端代码模式
- `app-vue/src/views/` — 现有页面组件参考
- `app-vue/src/api/` — API 调用模式

### 技术选型
- RocketMQ 客户端：rocketmq-spring-boot-starter 2.3.5（兼容服务端 5.3.2）
- Admin API：rocketmq-client-apache

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- Element Plus el-table：现有用户列表使用，可以复用分页、筛选模式
- Element Plus el-dialog：现有弹窗模式
- Element Plus el-form：现有表单验证模式

### Established Patterns
- REST API 风格：@RestController + @GetMapping/@PostMapping/@DeleteMapping
- Service 层：接口 + impl 实现类
- 前端 API 调用：axios 封装在 api/ 目录

### Integration Points
- 侧边栏菜单：需要添加 RocketMQ 管理入口
- 路由：添加 /rocketmq/topics 路由
- 权限控制：复用现有 RBAC 权限体系

</code_context>

<specifics>
## Specific Ideas

- Topic 状态：在线(ACTIVE)、暂停(SUSPEND)、未知(UNKNOWN)
- 队列数建议范围：1-16
- 权限选项：READ、WRITE、READ_WRITE

</specifics>

<deferred>
## Deferred Ideas

- Topic 配置修改（Phase 10 Consumer Group 相关）
- 消息内容查看（Phase 11 消息管理）

</deferred>

---
*Phase: 09-topic-mgmt*
*Context gathered: 2026-04-28*
