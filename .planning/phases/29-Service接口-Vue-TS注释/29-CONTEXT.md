# Phase 29: Service 接口 + Vue/TS 注释 — Context

**Gathered:** 2026-05-08
**Status:** Ready for planning

<domain>
## Phase Boundary

为后端 Service 接口和前端 Vue/TS 代码添加符合 Phase 27 规范的注释。不引入新工具，不新增业务功能。

**要求（来自 ROADMAP.md）：**
- BKND-08: ~23 个 Service 接口完整 Javadoc（接口职责 + 每个方法参数/返回值/异常说明）
- FRNT-01: 37 个 Vue 组件包含组件职责注释，`defineProps`/`defineEmits` 包含 JSDoc 类型说明
- FRNT-02: 15 个 API 模块函数包含 `@param` 参数说明
- FRNT-03: 1 个 Pinia Store 文件包含 Store 职责和 action 说明
- FRNT-04: 3 个 Router 模块包含路由说明和 guard 策略注释

</domain>

<decisions>
## Implementation Decisions

### Phase 27 标准的延续（沿用 Phase 28 模式）
- D-01: 先后端后前端 — Service 接口注释简单直接，先快速完成
- D-02: 后端 Service 合并在 Plan 01，前端合并在 Plan 02+Plan 03
- D-03: 共 2~3 个 Plan，串行 Wave

### 注释粒度
- D-04: Service 接口方法使用完整 Javadoc 模板（方法说明 + @param + @return + @throws）
- D-05: CRUD Service 方法简洁，复杂业务方法（秒杀下单、RocketMQ 管理等）略详细
- D-06: Vue 组件职责用 `<script lang="ts">` 块注释（非 setup），Props/Emits 用行内 JSDoc
- D-07: API 模块函数使用 JSDoc 块 + @param（参照 Phase 27 TSDoc 模板）
- D-08: Pinia Store 使用 `//` 行注释说明职责
- D-09: Router 路由表中每条路由配置添加行注释

### @since
- D-10: 所有新加类级 Javadoc 统一使用 `@since 1.7.0`
</decisions>

<canonical_refs>
## Canonical References

### 要求定义
- `.planning/ROADMAP.md` §Phase 29 — 阶段目标和成功标准
- `CLAUDE.md` §注释规范 — Phase 27 定义的注释标准

### 受影响的文件

**Plan 01: 后端 Service 接口（23 个）**
- `service/audit/AuditService.java`
- `service/auth/AuthService.java`
- `service/knowledge/KnowledgeService.java`
- `service/menu/MenuService.java`
- `service/monitor/MonitorService.java`
- `service/order/CartService.java`
- `service/order/OrderService.java`
- `service/order/TimeoutCancelService.java`
- `service/rocketmq/RocketMQAdminService.java`
- `service/rocketmq/impl/RocketMQSubService.java`
- `service/role/RoleService.java`
- `service/seckill/ActivityService.java`
- `service/seckill/GoodsService.java`
- `service/seckill/PreheatService.java`
- `service/seckill/SeckillService.java`
- `service/seckill/SignService.java`
- `service/seckill/ReconcileService.java`
- `service/seckill/stock/StockService.java`
- `service/user/UserService.java`
- `config/BlacklistService.java`
- `config/IdempotentService.java`
- `lock/DistributedLockService.java`
- `sse/seckill/SeckillSseService.java`

**Plan 02: Vue 组件（37 个）**
- `components/layout/AppMenu.vue`, `AppTabs.vue`, `AppHeader.vue`
- `components/business/knowledge/ArticleEditor.vue`, `CategoryTree.vue`
- `views/` 下 32 个页面组件

**Plan 03: API/Store/Router（19 个）**
- `api/modules/` 下 12 个 `.ts` 文件
- `api/types.ts`, `api/request.ts`
- `store/user.ts`
- `router/index.ts`, `router/guards.ts`, `router/modules/routes.ts`
</canonical_refs>
</domain>
