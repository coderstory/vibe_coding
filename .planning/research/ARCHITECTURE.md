# Architecture Research

**Domain:** Vue 3 + Spring Boot 管理后台 — 代码重构与目录整理
**Researched:** 2026-05-06
**Confidence:** MEDIUM (基于代码库分析 + 训练数据；WebSearch 不可用，Context7 环境异常)

## 执行摘要

当前代码库存在三层不一致问题：部分模块采用 interface+impl Service 模式，部分仅有实现类；Controller 层直接注入 Mapper 破坏分层；Entity 直接暴露给 API；前端类型定义集中混杂在单一文件。本架构方案在不引入新技术、不改变功能的前提下，提供可渐进实施的目录重组和分层规范。

核心策略是**按业务领域垂直切分 + 统一技术层水平规范**。后端按 `{domain}/` 组织（如 `user/`、`seckill/`），每个领域内部统一 `controller/`、`service/`、`mapper/`、`entity/`、`dto/` 结构。前端按相同领域切分 `api/`、`stores/`、`views/`，并引入 composables 抽取可复用逻辑。

## 当前代码库架构评估

### 当前后端包结构

```
springboot/src/main/java/cn/coderstory/springboot/
├── SpringbootApplication.java          # 主类
├── aspect/
│   └── AuditAspect.java                # 审计切面 — 良好
├── config/                             # 配置类 — 良好
│   ├── CorsConfig.java
│   ├── RedissonConfig.java
│   ├── RocketMQConfig.java
│   ├── SeckillProperties.java
│   ├── SecurityConfig.java
│   └── WebConfig.java
├── controller/                         # 核心控制器 — 扁平，需重组
│   ├── AuditLogController.java
│   ├── AuthController.java
│   ├── KnowledgeController.java
│   ├── MenuController.java
│   ├── RocketMQController.java
│   ├── RocketMQDashboardController.java
│   ├── RoleController.java
│   └── UserController.java
├── entity/                             # 核心实体 — 扁平，需重组
│   ├── AuditLog.java
│   ├── KnowledgeArticle.java
│   ├── KnowledgeArticleTag.java
│   ├── KnowledgeCategory.java
│   ├── KnowledgeFile.java
│   ├── KnowledgeTag.java
│   ├── Menu.java / Role.java / RoleMenuPermission.java / User.java
├── exception/                          # 异常处理 — 良好
│   ├── BusinessException.java
│   └── GlobalExceptionHandler.java
├── limiter/                            # 限流 — 跨领域工具
│   ├── ConcurrencyLimiter.java
│   ├── IpRateLimiter.java
│   └── QpsLimiter.java
├── lock/                               # 分布式锁 — 跨领域工具
│   ├── DistributedLockService.java
│   └── impl/ → 在锁包内很不自然
├── mapper/                             # 核心 Mapper — 扁平
├── monitor/                            # 监控领域 — 已有子包
│   ├── controller/MonitorController.java
│   └── service/MonitorService.java
├── mq/                                 # 消息队列 — 应归入 seckill/
│   ├── consumer/
│   └── producer/
├── order/                              # 订单领域 — 已有子包
│   ├── controller/ / entity/ / mapper/ / service/
├── seckill/                            # 秒杀领域 — 良好子包结构
│   ├── controller/ / dto/ / entity/ / mapper/ / service/ / vo/
├── security/                           # 安全 — 良好
│   ├── BlacklistService.java
│   ├── IdempotentService.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtTokenProvider.java
│   └── PasswordEncoder.java
├── service/                            # 核心 Service — 混乱
│   ├── AuditService.java / AuthService.java / KnowledgeService.java
│   ├── MenuService.java / MenuServiceImpl.java  ← 一个在根目录
│   ├── RoleService.java / RoleServiceImpl.java  ← 一个在根目录
│   ├── UserService.java / RocketMQAdminService.java
│   └── impl/
│       ├── KnowledgeServiceImpl.java
│       ├── UserServiceImpl.java
│       └── RocketMQAdminServiceImpl.java
├── sse/                                # SSE — 应归入 seckill/
│   └── SeckillSseService.java
├── stock/                              # 库存 — 应归入 seckill/
│   ├── consumer/ / entity/ / mapper/ / service/
├── util/
│   └── ZstdUtil.java
└── vo/                                 # 视图对象
    ├── ApiResponse.java
    ├── ResultResponse.java
    └── UserVO.java
```

### 当前前端目录结构

```
app-vue/src/
├── App.vue
├── main.ts
├── api/
│   ├── request.ts                      # Axios 封装 — 良好
│   ├── types.ts                        # 所有类型定义 — 需拆分
│   ├── activity.ts / audit.ts / auth.ts / cart.ts
│   ├── goods.ts / knowledge.ts / menu.ts / monitor.ts
│   ├── order.ts / rocketmq.ts / role.ts / seckill.ts / user.ts
├── assets/
│   ├── base.css / glass.css / main.css / logo.svg
│   └── themes/
│       ├── enterprise-theme.css / macos-26.css
│       └── animations/
├── components/
│   ├── AppHeader.vue / AppMenu.vue / AppTabs.vue
│   ├── HelloWorld.vue / TheWelcome.vue / WelcomeItem.vue  ← 脚手架残留
│   ├── icons/                                               ← 脚手架残留
│   └── knowledge/
│       ├── ArticleEditor.vue / CategoryTree.vue
├── composables/
│   └── useAnimationToggle.ts                                 ← 唯一 composable
├── router/
│   └── index.ts
├── store/
│   └── user.ts                                               ← 单一 store
└── views/
    ├── audit/ / auth/ / business/ / dashboard/ / error/
    ├── layout/ / monitor/ / order/
    ├── rocketmq/                                             ← 粒度过细
    │   ├── BrokerStatusTable.vue / ConsumerGroupDetail.vue
    │   ├── ConsumerGroupList.vue / Dashboard.vue
    │   ├── MessageList.vue / OverviewCard.vue
    │   ├── QpsChart.vue / ResetOffsetDialog.vue
    │   ├── TopicBacklogTable.vue / TopicList.vue
    │   └── __tests__/
    ├── seckill/
    │   ├── activity/ (ActivityForm.vue, ActivityList.vue)
    │   ├── goods/ (GoodsForm.vue, GoodsList.vue)
    │   └── MyReservations.vue / SeckillCart.vue / SeckillDetail.vue
    │       SeckillIndex.vue / SeckillRecord.vue
    └── system/
        ├── MenuManage.vue / RoleManage.vue
        ├── UserDetail.vue / UserManagement.vue
```

### 已识别的架构问题

| 问题 | 严重程度 | 位置 |
|------|---------|------|
| Controller 直接注入 Mapper | 高 | UserController → RoleMapper, SeckillController → SeckillGoodsMapper/SeckillActivityMapper |
| Entity 直接暴露为 API 响应 | 高 | UserController.getUserPage 返回实体列表，RoleController.getAllRoles 返回实体 |
| Service 层组织不一致 | 中 | 部分 interface+impl，部分仅有实现类，impl 位置不统一 |
| DTO 缺失 | 中 | 仅 seckill/dto/ 有，其他模块用 Map<String, Object> 或实体直接接收 |
| 跨领域模块散落 | 中 | mq/、stock/、sse/ 实际属于 seckill 域但散落在顶层 |
| 前端类型单文件 | 中 | api/types.ts 混杂所有领域类型定义 |
| 前端 Composables 缺失 | 中 | 业务逻辑写在 views 中，无可复用逻辑抽取 |
| 脚手架残留 | 低 | HelloWorld, TheWelcome, WelcomeItem, icons/ |
| 前端 Store 单薄 | 低 | 仅有 user.ts，缺少领域 store |
| 配置硬编码 | 高 | JWT secret、数据库密码默写死在 application.yaml |

---

## 推荐的架构重组方案

### 后端：按业务领域垂直切分

```
springboot/src/main/java/cn/coderstory/springboot/
├── SpringbootApplication.java              # 主类（保留在根包）
├── common/                                  # 跨领域共享代码
│   ├── config/                              #   全局配置
│   │   ├── CorsConfig.java
│   │   ├── RedissonConfig.java
│   │   ├── RocketMQConfig.java
│   │   ├── SecurityConfig.java
│   │   └── WebConfig.java
│   ├── exception/                           #   全局异常
│   │   ├── BusinessException.java
│   │   └── GlobalExceptionHandler.java
│   ├── response/                            #   统一响应（原 vo/）
│   │   ├── ApiResponse.java
│   │   └── ResultResponse.java
│   ├── security/                            #   认证安全
│   │   ├── BlacklistService.java
│   │   ├── IdempotentService.java
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── JwtTokenProvider.java
│   │   └── PasswordEncoder.java
│   ├── limiter/                             #   限流组件
│   │   ├── ConcurrencyLimiter.java
│   │   ├── IpRateLimiter.java
│   │   └── QpsLimiter.java
│   ├── lock/                                #   分布式锁
│   │   ├── DistributedLockService.java
│   │   └── DistributedLockServiceImpl.java
│   └── util/                                #   通用工具
│       └── ZstdUtil.java
│
├── user/                                    # 用户管理领域
│   ├── controller/
│   │   └── UserController.java
│   ├── service/
│   │   ├── UserService.java
│   │   └── impl/UserServiceImpl.java
│   ├── mapper/
│   │   └── UserMapper.java
│   ├── entity/
│   │   └── User.java
│   └── dto/
│       ├── UserCreateRequest.java           #   创建用户 DTO
│       ├── UserUpdateRequest.java           #   更新用户 DTO
│       ├── UserQueryRequest.java            #   查询参数 DTO
│       ├── UserPageResponse.java            #   分页响应 DTO
│       └── UserVO.java                      #   详情 VO
│
├── role/                                    # 角色管理领域
│   ├── controller/
│   │   └── RoleController.java
│   ├── service/
│   │   ├── RoleService.java
│   │   └── impl/RoleServiceImpl.java
│   ├── mapper/
│   │   ├── RoleMapper.java
│   │   └── RoleMenuPermissionMapper.java
│   └── entity/
│       ├── Role.java
│       └── RoleMenuPermission.java
│
├── menu/                                    # 菜单管理领域
│   ├── controller/
│   │   └── MenuController.java
│   ├── service/
│   │   ├── MenuService.java
│   │   └── impl/MenuServiceImpl.java
│   ├── mapper/
│   │   └── MenuMapper.java
│   └── entity/
│       └── Menu.java
│
├── auth/                                    # 认证领域
│   ├── controller/
│   │   └── AuthController.java
│   └── service/
│       └── AuthService.java
│
├── audit/                                   # 审计日志领域
│   ├── controller/
│   │   └── AuditLogController.java
│   ├── service/
│   │   └── AuditService.java
│   ├── mapper/
│   │   └── AuditLogMapper.java
│   ├── entity/
│   │   └── AuditLog.java
│   └── aspect/
│       └── AuditAspect.java                 #   从顶层移入此域
│
├── knowledge/                               # 知识库领域
│   ├── controller/
│   │   └── KnowledgeController.java
│   ├── service/
│   │   ├── KnowledgeService.java
│   │   └── impl/KnowledgeServiceImpl.java
│   ├── mapper/
│   │   ├── KnowledgeArticleMapper.java
│   │   ├── KnowledgeArticleTagMapper.java
│   │   ├── KnowledgeCategoryMapper.java
│   │   ├── KnowledgeFileMapper.java
│   │   └── KnowledgeTagMapper.java
│   └── entity/
│       ├── KnowledgeArticle.java
│       ├── KnowledgeArticleTag.java
│       ├── KnowledgeCategory.java
│       ├── KnowledgeFile.java
│       └── KnowledgeTag.java
│
├── seckill/                                 # 秒杀领域
│   ├── controller/
│   │   ├── ActivityController.java
│   │   ├── GoodsController.java
│   │   ├── PreheatController.java
│   │   ├── ReservationController.java
│   │   ├── SeckillActivityController.java
│   │   ├── SeckillController.java
│   │   └── SeckillSseController.java
│   ├── service/
│   │   ├── ActivityService.java / impl/
│   │   ├── GoodsService.java / impl/
│   │   ├── PreheatService.java / impl/
│   │   ├── SeckillService.java / impl/
│   │   ├── SignService.java / impl/
│   │   └── SeckillSseService.java           #   从 sse/ 移入
│   ├── mapper/
│   │   ├── SeckillActivityMapper.java
│   │   ├── SeckillGoodsMapper.java
│   │   ├── SeckillLogMapper.java
│   │   ├── SeckillOrderMapper.java
│   │   ├── SeckillQueueMapper.java
│   │   └── SeckillReservationMapper.java
│   ├── entity/
│   │   ├── SeckillActivity.java
│   │   ├── SeckillGoods.java
│   │   ├── SeckillLog.java
│   │   ├── SeckillOrder.java
│   │   ├── SeckillQueue.java
│   │   ├── SeckillReservation.java
│   │   └── IpBlacklist.java
│   ├── dto/
│   │   ├── SeckillRequest.java
│   │   └── SeckillResponse.java
│   ├── vo/
│   │   └── ActivityDetailVO.java
│   ├── mq/                                  #   从顶层移入
│   │   ├── consumer/
│   │   └── producer/
│   └── stock/                               #   从顶层移入
│       ├── consumer/ / entity/ / mapper/ / service/
│
├── order/                                   # 订单领域（保持现有结构）
│   ├── controller/
│   ├── service/ / impl/
│   ├── mapper/
│   └── entity/
│
└── monitor/                                 # 监控领域（保持现有结构）
    ├── controller/
    └── service/
```

### 后端层次规范

从请求到数据库的完整调用链：

```
┌─────────────────────────────────────────────────────────────────┐
│ Controller 层                                                    │
│ ┌─────────────────────────────────────────────────────────────┐ │
│ │ 职责: 接收 HTTP 请求、参数校验、调用 Service、返回响应        │ │
│ │ 注入: 仅注入 Service 接口（禁止注入 Mapper）                  │ │
│ │ 参数: 使用 DTO（禁止 Map<String, Object>、禁止 Entity）       │ │
│ │ 返回: ApiResponse<T> 包装 DTO/VO                              │ │
│ └─────────────────────────────────────────────────────────────┘ │
│                              │                                    │
│                              ▼                                    │
│ Service 层                                                       │
│ ┌─────────────────────────────────────────────────────────────┐ │
│ │ 职责: 业务逻辑、事务管理、调用 Mapper、跨 Service 协调        │ │
│ │ 边界: 接口 + 实现分离（所有 Service 都走 interface）           │ │
│ │ 事务: @Transactional 放在 Service 方法上                      │ │
│ │ 数据: 接收 DTO/基本类型，返回 Entity 或组装后的 DTO/VO        │ │
│ └─────────────────────────────────────────────────────────────┘ │
│                              │                                    │
│                              ▼                                    │
│ Mapper 层                                                        │
│ ┌─────────────────────────────────────────────────────────────┐ │
│ │ 职责: 数据访问（MyBatis Plus BaseMapper）                     │ │
│ │ 操作: 仅操作 Entity，不包含业务逻辑                           │ │
│ │ 禁止: 跨 Mapper 调用、跨 Service 调用                         │ │
│ └─────────────────────────────────────────────────────────────┘ │
│                              │                                    │
│                              ▼                                    │
│ Entity 层                                                        │
│ ┌─────────────────────────────────────────────────────────────┐ │
│ │ 职责: 数据库表映射，纯 POJO                                   │ │
│ │ 范围: 仅在 Service 和 Mapper 之间传递                         │ │
│ │ 禁止: 暴露到 Controller 层、包含业务逻辑                      │ │
│ └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘

数据传输对象（DTO/VO）的流转:
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│ Request  │────▶│ Input    │────▶│ Entity   │────▶│ Output   │────▶│ Response │
│ (HTTP)   │     │ DTO      │     │ (Service) │     │ DTO/VO   │     │ (JSON)   │
└──────────┘     └──────────┘     └──────────┘     └──────────┘     └──────────┘
     Controller       Controller       Service         Service        Controller
     接收层            转换层           业务层           组装层          响应层
```

#### DTO 分类规范

| 类型 | 包位置 | 方向 | 命名规范 | 示例 |
|------|--------|------|----------|------|
| Request DTO | `dto/` | 客户端 → 服务端 | `{Entity}CreateRequest`, `{Entity}UpdateRequest` | `UserCreateRequest` |
| Query DTO | `dto/` | 客户端 → 服务端 | `{Entity}QueryRequest` | `UserQueryRequest` |
| Response DTO | `dto/` | 服务端 → 客户端 | `{Entity}PageResponse`, `{Entity}Response` | `UserPageResponse` |
| View Object | `vo/` 或 `dto/` | 服务端 → 客户端 | `{Entity}VO` | `UserVO` |

#### Service 层统一规范

所有领域 Service 必须遵循：

```java
// 接口定义 — 放在 domain.service/
public interface UserService {
    IPage<UserVO> queryPage(UserQueryRequest query);
    UserVO getById(Long id);
    void create(UserCreateRequest request);
    void update(Long id, UserUpdateRequest request);
    void delete(Long id);
}

// 实现 — 放在 domain.service.impl/，类名 = 接口名 + Impl
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    // ... 业务逻辑
}
```

**原则：**
- Controller 只依赖 Service 接口（方便单测 Mock）
- Service 实现放在 `impl/` 子包（不放在根目录）
- 简单 Service 也必须拆接口+实现（保持一致性，成本极低）

### 前端：按业务领域垂直切分

```
app-vue/src/
├── main.ts
├── App.vue
│
├── api/                                    # API 调用层
│   ├── client.ts                           #   Axios 封装（原 request.ts）
│   ├── types/                              #   按领域拆分类型定义
│   │   ├── common.ts                       #     公共类型（ApiResponse, PageResult）
│   │   ├── user.ts                         #     用户相关类型
│   │   ├── role.ts                         #     角色相关类型
│   │   ├── menu.ts                         #     菜单相关类型
│   │   ├── auth.ts                         #     认证相关类型
│   │   ├── audit.ts                        #     审计相关类型
│   │   ├── knowledge.ts                    #     知识库相关类型
│   │   ├── seckill.ts                      #     秒杀相关类型
│   │   ├── order.ts                        #     订单相关类型
│   │   ├── rocketmq.ts                     #     RocketMQ 相关类型
│   │   └── index.ts                        #     统一重新导出
│   ├── modules/                            #   按领域拆分 API 函数
│   │   ├── user.ts
│   │   ├── role.ts
│   │   ├── menu.ts
│   │   ├── auth.ts
│   │   ├── audit.ts
│   │   ├── knowledge.ts
│   │   ├── seckill.ts
│   │   ├── cart.ts
│   │   ├── order.ts
│   │   ├── activity.ts
│   │   ├── goods.ts
│   │   ├── monitor.ts
│   │   └── rocketmq.ts
│   └── index.ts                            #   统一导出所有 API 模块
│
├── stores/                                 # Pinia 状态管理
│   ├── user.ts                             #   用户认证状态
│   ├── seckill.ts                          #   秒杀购物车状态
│   ├── app.ts                              #   应用全局状态（侧栏、标签页等）
│   └── index.ts
│
├── composables/                            # 可复用组合式函数
│   ├── useAuth.ts                          #   认证相关逻辑
│   ├── usePagination.ts                    #   分页逻辑
│   ├── useFormDialog.ts                    #   表单对话框逻辑
│   ├── usePermission.ts                    #   权限检查逻辑
│   └── useConfirm.ts                       #   确认操作逻辑
│
├── router/
│   └── index.ts                            #   路由配置 + 导航守卫
│
├── views/                                  # 页面组件（保持现有结构）
│   ├── layout/                             #   布局组件
│   │   └── Layout.vue
│   ├── auth/
│   │   └── Login.vue
│   ├── dashboard/
│   │   └── DashboardIndex.vue
│   ├── system/                             #   系统管理相关页面
│   │   ├── UserManagement.vue
│   │   ├── UserDetail.vue
│   │   ├── RoleManage.vue
│   │   └── MenuManage.vue
│   ├── audit/
│   │   └── AuditLog.vue
│   ├── knowledge/                          #   知识库页面（待建）
│   ├── seckill/
│   │   ├── SeckillIndex.vue
│   │   ├── SeckillDetail.vue
│   │   ├── SeckillCart.vue
│   │   ├── SeckillRecord.vue
│   │   ├── MyReservations.vue
│   │   ├── activity/
│   │   │   ├── ActivityList.vue
│   │   │   └── ActivityForm.vue
│   │   └── goods/
│   │       ├── GoodsList.vue
│   │       └── GoodsForm.vue
│   ├── order/
│   │   ├── OrderList.vue
│   │   └── OrderConfirm.vue
│   ├── rocketmq/
│   │   ├── Dashboard.vue
│   │   ├── TopicList.vue
│   │   ├── ConsumerGroupList.vue
│   │   ├── ConsumerGroupDetail.vue
│   │   ├── MessageList.vue
│   │   └── components/                     #   RocketMQ 领域内部组件
│   │       ├── ResetOffsetDialog.vue
│   │       ├── BrokerStatusTable.vue
│   │       ├── OverviewCard.vue
│   │       ├── QpsChart.vue
│   │       └── TopicBacklogTable.vue
│   ├── monitor/
│   │   └── MonitorDashboard.vue
│   ├── business/
│   │   └── BusinessData.vue
│   └── error/
│       └── NotFound.vue
│
├── components/                             # 全局共享组件
│   ├── layout/                             #   布局组件
│   │   ├── AppHeader.vue
│   │   ├── AppMenu.vue
│   │   └── AppTabs.vue
│   ├── common/                             #   通用业务组件
│   │   ├── PageContainer.vue               #     页面容器（标题+内容）
│   │   ├── ConfirmDialog.vue               #     确认对话框
│   │   ├── TableActions.vue                #     表格操作列
│   │   └── SearchForm.vue                  #     搜索表单
│   └── ...                                 #   各领域共享组件
│
└── assets/                                 # 静态资源
    ├── styles/                             #   全局样式（原 assets/ CSS）
    │   ├── base.css
    │   ├── glass.css
    │   ├── main.css
    │   └── themes/
    │       ├── enterprise-theme.css
    │       ├── macos-26.css
    │       └── animations/
    └── images/
        └── logo.svg
```

#### 前端层次规范

```
┌─────────────────────────────────────────────────────────────────┐
│ Views 页面层                                                     │
│ ┌─────────────────────────────────────────────────────────────┐ │
│ │ 职责: 页面布局、组合子组件、处理用户交互                      │ │
│ │ 调用: composables（逻辑）、stores（状态）、API 模块（数据）    │ │
│ │ 禁止: 直接操作 localStorage、写复杂业务逻辑                   │ │
│ └─────────────────────────────────────────────────────────────┘ │
│                              │                                    │
│                    ┌─────────┼─────────┐                          │
│                    ▼         ▼         ▼                          │
│            Composables   Stores    API Modules                    │
│            (可复用逻辑)  (全局状态)  (数据获取)                     │
│                    │         │         │                          │
│                    └─────────┼─────────┘                          │
│                              ▼                                    │
│ Components 组件层                                                 │
│ ┌─────────────────────────────────────────────────────────────┐ │
│ │ 职责: UI 渲染、事件发射、接收 Props                           │ │
│ │ 禁止: 直接调用 API、包含业务规则                              │ │
│ └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

#### Composables vs Stores 决策树

```
┌─ 是否需要跨组件共享响应式状态？ ──┬── 是 ──▶ 用 Pinia Store
│                                  │
│                                  └── 否 ──▶ 用 Composable（组件内部或局部共享）
│
├─ 是否需要持久化（localStorage）？ ── 是 ──▶ 用 Pinia Store + 手动持久化
│
├─ 是否是纯逻辑（无 UI 状态）？ ──── 是 ──▶ 用 Composable
│
└─ 是否与特定 API 紧密耦合？ ────── 是 ──▶ 放在 API 模块 + Composable 封装调用
```

**具体示例：**
- `useAuth` → Composable（封装 login/logout 流程，内部调用 store 和 API）
- `userStore` → Store（存储 token、用户信息、isLoggedIn）
- `usePagination` → Composable（纯逻辑，无 UI 依赖）
- `seckillStore` → Store（购物车状态，跨多个页面共享）

---

## 配置管理方案

### 当前问题

- 所有配置集中在单一 `application.yaml`（180+ 行）
- JWT secret 明文硬编码
- 数据库密码默认值写死在配置中
- 测试环境 `application-test.yaml` 大量重复主配置

### 推荐拆分方案

```
springboot/src/main/resources/
├── application.yaml                      # 极简主配置 — 仅公共 + 激活profile
├── application-core.yaml                 # 核心应用配置
├── application-datasource.yaml           # 数据源 + Flyway
├── application-security.yaml             # JWT + 安全白名单
├── application-seckill.yaml              # 秒杀业务配置
├── application-rocketmq.yaml             # RocketMQ 配置
├── application-redis.yaml                # Redis + Redisson
├── application-mybatis.yaml              # MyBatis Plus 配置
└── db/migration/                         # Flyway 脚本（不变）
```

**主配置 `application.yaml`：**

```yaml
spring:
  application:
    name: admin-system
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  config:
    import:
      - classpath:application-core.yaml
      - classpath:application-datasource.yaml
      - classpath:application-security.yaml
      - classpath:application-seckill.yaml
      - classpath:application-rocketmq.yaml
      - classpath:application-redis.yaml
      - classpath:application-mybatis.yaml

server:
  port: ${SERVER_PORT:8080}
```

**安全配置 `application-security.yaml`（敏感信息外部化）：**

```yaml
security:
  whitelist: /api/auth/**,/api/knowledge/files/**,/api/seckill/subscribe/**,/api/seckill/unsubscribe/**

jwt:
  secret: ${JWT_SECRET}                    # 必须从环境变量注入，无默认值
  expiration: ${JWT_EXPIRATION:86400000}
  refresh-expiration: ${JWT_REFRESH_EXPIRATION:604800000}
```

### 环境特定配置

| 配置方式 | 场景 | 示例 |
|----------|------|------|
| 环境变量 | 敏感信息、部署差异 | `DB_PASSWORD`, `JWT_SECRET`, `REDIS_HOST` |
| Profile 文件 | 环境差异大 | `application-dev.yaml`, `application-prod.yaml` |
| `.env` 文件 | 本地开发 | `.env` （加入 .gitignore） |

**禁止：**
- 将密码、密钥、Token 写死在 application.yaml 中
- 为每个环境复制完整配置文件（大量重复）

---

## 多模块 vs 单模块决策

### 决策：保持单模块 Gradle 项目

**理由：**

| 因素 | 评估 | 结论 |
|------|------|------|
| 项目规模 | ~100 个 Java 源文件 | 单模块足够 |
| 团队规模 | 单人/小团队 | 多模块增加认知负担 |
| 部署方式 | 单体 JAR 部署 | 多模块不带来部署收益 |
| 构建速度 | Gradle 增量编译已很快 | 当前规模多模块收益微乎其微 |
| 代码隔离 | 包级隔离已足够 | 多模块强制隔离不必要 |

**当前不拆多模块，但通过包结构实现逻辑分层即可。** 如果未来需要独立的 MQ 消费者模块或独立的 Admin 模块，那时再拆。

**拆分信号（满足任一条考虑拆）：**
1. 单个模块超过 200 个源文件
2. 需要独立的部署单元（如独立的 MQ 消费者进程）
3. 多个团队并行开发，需要 API 契约强制隔离
4. 构建时间超过 30 秒且增量编译无法缓解

---

## 交叉关注点组织

### 横切关注点归属

| 关注点 | 当前位置 | 推荐位置 | 实现方式 |
|--------|---------|---------|---------|
| 审计日志 | `aspect/AuditAspect.java` | `audit/aspect/AuditAspect.java` | Spring AOP @Aspect |
| 全局异常 | `exception/GlobalExceptionHandler.java` | `common/exception/` | @RestControllerAdvice |
| 统一响应 | `vo/ApiResponse.java` | `common/response/` | 静态工厂方法 |
| JWT 认证 | `security/JwtAuthenticationFilter.java` | `common/security/` | OncePerRequestFilter |
| 幂等性 | `security/IdempotentService.java` | `common/security/` | Redis + 注解 |
| 限流 | `limiter/*.java` | `common/limiter/` | Redis + 拦截器 |
| 分布式锁 | `lock/*.java` | `common/lock/` | Redisson |
| CORS | `config/CorsConfig.java` | `common/config/` | WebMvcConfigurer |
| 验证 | 分散在 Controller | 统一到 DTO（使用 @Valid + Bean Validation） | jakarta.validation |

### 全局异常处理增强

当前 GlobalExceptionHandler 覆盖了常见异常，建议补充：

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 现有处理器保留...

    // 补充：参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(fe ->
            errors.put(fe.getField(), fe.getDefaultMessage()));
        return ApiResponse.badRequest("参数校验失败", errors);
    }

    // 补充：权限不足
    @ExceptionHandler(AccessDeniedException.class)
    public ApiResponse<Void> handleAccessDenied(AccessDeniedException e) {
        return ApiResponse.forbidden("权限不足");
    }

    // 补充：HTTP 方法不支持
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiResponse<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ApiResponse.error(405, "不支持的请求方法: " + e.getMethod());
    }
}
```

---

## 重构实施中的关键集成点

### 不改的组件

| 组件 | 说明 |
|------|------|
| `JwtTokenProvider` | JWT 生成/解析正确，只移动包 |
| `JwtAuthenticationFilter` | 认证过滤逻辑正确，只移动包 |
| `GlobalExceptionHandler` | 异常处理完整，只移动包 + 补充 |
| `AuditAspect` | 审计切面工作正常，移到 audit 域 |
| 所有 Flyway 迁移脚本 | 数据库迁移历史，不可修改 |
| `build.gradle.kts` | 构建配置正常，只调整 source set（如需要） |
| 前端 `request.ts` | Axios 封装正确，改名 client.ts 只移动文件 |
| Element Plus 配置 | 组件库注册正确 |
| 路由守卫 | 认证拦截逻辑正确 |

### 必须改的组件

| 组件 | 改动 | 原因 |
|------|------|------|
| `UserController` → Mapper 直接注入 | 改为只注入 Service | 破坏分层，难以测试 |
| `SeckillController` → Mapper 直接注入 | 改为调用 Service 层 | 同上 |
| Controller 中 `Map<String, Object>` 参数 | 创建 Request DTO | 类型安全、可读性、可验证 |
| Controller 中直接返回 Entity | 返回 DTO/VO | API 契约稳定性、安全性（避免暴露内部字段） |
| Service 接口缺失 | 补全接口+实现分离 | 可测试性、一致性 |
| `application.yaml` JWT secret | 移除默认值，强制环境变量 | 安全性 |
| 前端 `api/types.ts` | 拆分为 `types/*.ts` | 可维护性 |
| 前端 `api/*.ts` 扁平 | 移入 `api/modules/` | 结构清晰 |
| 脚手架组件 | 删除或移到 `components/demo/` | 减少噪音 |

### 构建顺序建议

```
Phase 1: 基础设施（不涉及业务逻辑）
├── 1.1 创建 common/ 包，移入 config/、exception/、response/、security/、limiter/、lock/、util/
├── 1.2 删除脚手架文件（HelloWorld、TheWelcome、WelcomeItem、icons/）
├── 1.3 拆分 application.yaml 为多个配置文件
├── 1.4 拆分前端 types.ts 为 types/*.ts
└── 1.5 前端 api/ 文件移入 api/modules/，styles 移入 assets/styles/

Phase 2: 领域包化（每个领域独立迁移，可并行）
├── 2.1 user/ 领域 → 创建包 + 移入文件 + 创建 DTO + 修复 Controller
├── 2.2 role/ 领域 → 同上
├── 2.3 menu/ 领域 → 同上
├── 2.4 auth/ 领域 → 同上
├── 2.5 audit/ 领域 → 同上
├── 2.6 knowledge/ 领域 → 同上
└── 2.7 合并 mq/、stock/、sse/ 到 seckill/ 领域

Phase 3: 前端领域化
├── 3.1 创建 domains/ 提取公共 composables（usePagination、useFormDialog、useConfirm）
├── 3.2 为各领域创建 stores/（seckillStore、appStore）
└── 3.3 按需抽取业务逻辑到 composables

Phase 4: 配置安全加固
├── 4.1 移除 JWT secret 默认值 → 强制环境变量
├── 4.2 数据库密码移除默认值 → 强制环境变量
├── 4.3 创建 .env.example 作为本地开发模板
└── 4.4 添加 @ConfigurationProperties 替换 @Value 分散注入
```

---

## 应避免的反模式

### 反模式 1：Controller 直接注入 Mapper

**当前代码（错误）：**
```java
@RestController
public class UserController {
    private final UserService userService;
    private final RoleMapper roleMapper;        // ← 错误：跳过 Service 层
}
```

**问题：** 破坏分层、Controller 测试需要 Mock Mapper、业务逻辑散落到 Controller。

**正确做法：**
```java
@RestController
public class UserController {
    private final UserService userService;      // 仅注入 Service
    // 需要角色数据 → 通过 UserService 获取，或注入 RoleService
}
```

### 反模式 2：Entity 直接暴露为 API 响应

**当前代码（错误）：**
```java
@GetMapping
public ResponseEntity<ApiResponse<List<Role>>> getAllRoles() {  // ← 错误：返回 Entity
    List<Role> roles = roleMapper.selectList(null);
    return ResponseEntity.ok(ApiResponse.success(roles));
}
```

**问题：** 数据库表结构变更直接影响前端、暴露内部字段、JSON 序列化时循环引用风险。

**正确做法：**
```java
@GetMapping
public ApiResponse<List<RoleVO>> getAllRoles() {  // ← 正确：返回 VO
    List<RoleVO> vos = roleService.getAllRoles().stream()
        .map(RoleVO::from)                        // 使用 MapStruct 或手动转换
        .toList();
    return ApiResponse.success(vos);
}
```

### 反模式 3：Map 作为请求/响应体

**当前代码（错误）：**
```java
@PostMapping
public ResponseEntity<ApiResponse<Void>> createUser(@RequestBody Map<String, Object> request) {
    User user = new User();
    user.setUsername((String) request.get("username"));  // ← 类型不安全
    // ...
}
```

**问题：** 无类型安全、无编译期校验、无 API 文档、调用方不知道需要哪些字段。

**正确做法：**
```java
@PostMapping
public ApiResponse<Void> createUser(@Valid @RequestBody UserCreateRequest request) {
    userService.create(request);   // DTO 有明确的字段和类型
    return ApiResponse.success("用户创建成功");
}
```

### 反模式 4：Service 接口放在实现类旁边

**当前代码（错误）：**
```
service/
├── MenuService.java           ← 接口
├── MenuServiceImpl.java       ← 实现在旁边
├── RoleService.java
└── RoleServiceImpl.java
```

**正确做法：** 所有实现统一放在 `impl/` 子包。
```
service/
├── MenuService.java
├── RoleService.java
└── impl/
    ├── MenuServiceImpl.java
    └── RoleServiceImpl.java
```

### 反模式 5：前端所有类型定义在一个文件

**当前代码（错误）：** `api/types.ts` 混合了 User、Role、Menu、Knowledge、Audit、Seckill 等所有类型定义。

**问题：** 文件随项目增长无限膨胀、任何模块修改都触及同一文件、难以查找。

**正确做法：** 拆分到 `types/user.ts`、`types/role.ts` 等，`types/index.ts` 统一重导出。

---

## 架构模式总结

| 原则 | 说明 | 适用范围 |
|------|------|---------|
| **每层只依赖下一层** | Controller → Service → Mapper，不可跨层 | 后端 |
| **数据对象按层隔离** | DTO (传输) / Entity (持久化) / VO (展示)，不可混用 | 后端 |
| **接口与实现分离** | 所有 Service 定义接口，实现放在 impl/ | 后端 |
| **按领域组织包** | 每个业务模块独立的包空间，内部统一层次结构 | 后端 + 前端 |
| **Composable 抽取逻辑** | 视图只负责组装，可复用逻辑进 composable | 前端 |
| **Store 管理共享状态** | 跨组件共享的响应式状态用 Pinia Store | 前端 |
| **配置外部化** | 敏感信息通过环境变量注入，无默认值 | 后端 |
| **渐进式迁移** | 每次只移动一个领域，确保编译通过、测试通过 | 全局 |

---

## 扩展性考虑

| 规模 | 当前方案 | 需调整的内容 |
|------|---------|-------------|
| 当前（~100 Java 文件，~40 前端文件） | 单模块 + 领域包 | 无需调整 |
| 扩展到 200+ 文件 | 单模块 + 领域包 | 考虑提取 common 为独立模块 |
| 扩展到 500+ 文件 | 多模块（common + 各领域独立模块） | 通过 Gradle 多模块管理依赖 |
| 独立部署 MQ 消费者 | 提取 seckill/mq/ 为独立模块 | 独立的 Spring Boot Application |
| 前端页面 > 50 个 | 当前结构 | 考虑路由懒加载分组、views 内按子域拆分 |

---

## 源代码

基于对代码库的完整文件级分析（2026-05-06），包括：
- `springboot/src/main/java/cn/coderstory/springboot/` 下 82+ 个 Java 源文件的逐一审查
- `app-vue/src/` 下 50+ 个 Vue/TypeScript 源文件的逐一审查
- `build.gradle.kts` 和 `application.yaml` 配置审阅

架构模式参考来自 Spring Boot 官方分层约定、Vue 3 Composition API 最佳实践、以及 MyBatis Plus 推荐的使用模式（training data）。由于 WebSearch 和 Context7 在环境中不可用，具体最佳实践细节置信度为 MEDIUM，但基于代码库分析的诊断发现置信度为 HIGH。

---
*Architecture research for: Vue 3 + Spring Boot 管理后台代码重构*
*Researched: 2026-05-06*
