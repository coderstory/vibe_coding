# Codebase Structure

**Analysis Date:** 2026-05-10

## Directory Layout

```
vibe_coding/
├── .claude/                   # Claude AI 配置与 project memory
├── .gradle/                   # Gradle 构建缓存（生成，不提交）
├── .idea/                     # JetBrains IDE 配置（生成，不提交）
├── .playwright/               # Playwright 浏览器测试数据
├── .planning/                 # GSD 项目规划文件
│   ├── codebase/              #  本文件所在位置：代码库分析文档
│   ├── phases/                #  各阶段实施计划与总结
│   ├── research/              #  技术调研与架构演进记录
│   └── index.json             #  规划索引
├── app-vue/                   # Vue 3 前端应用
│   ├── public/                #  静态资源（favicon 等）
│   ├── src/                   #  前端源码
│   ├── index.html             #  HTML 入口
│   ├── vite.config.ts         #  Vite 构建配置
│   ├── vitest.config.ts       #  单元测试配置
│   ├── tsconfig.json          #  TypeScript 配置（路径别名 @/ -> ./src/*）
│   ├── eslint.config.mjs      #  ESLint 10 flat config
│   ├── stylelint.config.mjs   #  Stylelint 17 config
│   ├── .prettierrc            #  Prettier 格式化配置
│   └── package.json           #  依赖与脚本（node ^20.19 / >=22.12）
├── docs/                      # 项目文档
│   ├── comment-standards.md   #  注释规范（L0-L3 层级）
│   ├── seckill/user-guide.md  #  秒杀系统用户手册
│   ├── browser-automation-guide.md
│   └── superpowers-zh.md      #  Superpowers-ZH 技能列表
├── springboot/                # Spring Boot 后端应用
│   ├── src/                   #  后端源码 + 资源
│   ├── build.gradle.kts       #  Gradle Kotlin DSL 构建配置
│   ├── settings.gradle.kts    #  项目设置（含版本目录 libs.versions.toml）
│   ├── gradle.properties      #  Gradle 属性
│   ├── gradlew.bat            #  Gradle Wrapper（Windows）
│   └── checkstyle.xml         #  Checkstyle 代码风格配置
├── CLAUDE.md                  #  AI 编程指南（项目规范）
├── README.md                  #  项目说明
├── README-FOR-GSD.md          #  GSD 工作流说明
└── .editorconfig              #  编辑器通用配置
```

## Directory Purposes

### Top-Level

**`app-vue/`:** Vue 3 + Vite + TypeScript 前端单页应用。开发服务器端口 5173。

**`springboot/`:** Spring Boot 4.1 + Java 26 后端 REST API。服务端口 8080。

**`.planning/`:** GSD 规划系统文件。包含代码库分析文档、阶段实施计划和状态摘要。由 `/gsd-map-codebase`, `/gsd-plan-phase`, `/gsd-execute-phase` 命令管理。

**`docs/`:** 项目文档。包括秒杀系统手册、浏览器自动化指南、注释规范和 Superpowers-ZH 技能列表。

### Frontend: `app-vue/src/`

```
app-vue/src/
├── api/                       # API 请求层
│   ├── request.ts             #  Axios 实例，请求/响应拦截器，token 刷新逻辑
│   ├── types.ts               #  集中类型定义（ApiResponse, User, Menu, Role 等）
│   └── modules/               #  按业务域拆分的 API 模块
│       ├── auth.ts            #  登录/登出/刷新 token/获取当前用户
│       ├── user.ts            #  用户管理 CRUD
│       ├── role.ts            #  角色管理 CRUD
│       ├── menu.ts            #  菜单管理 CRUD
│       ├── audit.ts           #  审计日志查询
│       ├── knowledge.ts       #  知识库（文章、分类、标签）
│       ├── seckill.ts         #  秒杀抢购、签到、签名
│       ├── goods.ts           #  秒杀商品
│       ├── order.ts           #  订单管理
│       ├── cart.ts            #  购物车
│       ├── monitor.ts         #  系统监控（硬件指标）
│       └── rocketmq.ts        #  RocketMQ 管理（Topic/Consumer/消息/集群/仪表盘）
├── assets/
│   └── themes/
│       └── animations/        #  海浪动画等 CSS 动画
├── components/
│   ├── common/                #  通用组件
│   ├── layout/                #  布局组件
│   │   ├── AppHeader.vue      #  顶部导航
│   │   ├── AppMenu.vue        #  侧边菜单
│   │   └── AppTabs.vue        #  标签页导航
│   └── business/
│       └── knowledge/         #  知识库业务组件
│           ├── ArticleEditor.vue   #  富文本编辑器（wangEditor）
│           └── CategoryTree.vue    #  分类树组件
├── composables/
│   └── useAnimationToggle.ts  #  动画开关组合式函数
├── router/
│   ├── index.ts               #  路由实例创建（createWebHistory）
│   ├── guards.ts              #  导航守卫（登录验证拦截）
│   └── modules/
│       └── routes.ts          #  路由配置表
├── store/
│   └── user.ts                #  Pinia 用户状态管理（token, user, isLoggedIn）
└── views/
    ├── auth/                  #  登录页
    ├── dashboard/             #  仪表盘/首页
    ├── system/                #  系统管理页面
    ├── audit/                 #  审计日志页面
    ├── business/              #  业务管理
    ├── monitor/               #  系统监控页面
    ├── order/                 #  订单管理页面
    ├── rocketmq/              #  RocketMQ 管理页面（Dashboard/Topic/Consumer/Message）
    ├── seckill/               #  秒杀系统页面
    │   ├── activity/          #  活动管理
    │   └── goods/             #  商品管理
    ├── layout/                #  布局页面
    └── error/                 #  错误页面
```

### Backend: `springboot/src/main/java/cn/coderstory/springboot/`

```
springboot/src/main/java/cn/coderstory/springboot/
├── SpringbootApplication.java           # Spring Boot 应用入口
│
├── config/                     # 共享配置层
│   ├── SecurityConfig.java              # Spring Security 过滤器链配置
│   ├── JwtAuthenticationFilter.java     # JWT 令牌认证过滤器
│   ├── JwtTokenProvider.java            # JWT 令牌生成/验证/解析
│   ├── PasswordEncoder.java             # 密码加密器
│   ├── CorsConfig.java                  # CORS 跨域配置
│   ├── RedissonConfig.java              # Redisson 客户端配置
│   ├── RocketMQConfig.java              # RocketMQ 连接配置
│   ├── WebConfig.java                   # Web MVC 配置
│   ├── SeckillProperties.java           # 秒杀业务配置属性
│   ├── IdempotentService.java           # Redis 幂等性服务
│   ├── BlacklistService.java            # IP 黑名单服务
│   ├── MonitorHardwareProperties.java   # 硬件监控配置
│   └── MonitoringSchedulerConfig.java   # 监控调度配置
│
├── controller/                 # 控制器层（REST API）
│   ├── auth/AuthController.java                 # 认证（登录/注册/刷新/登出）
│   ├── user/UserController.java                 # 用户管理
│   ├── role/RoleController.java                 # 角色管理
│   ├── menu/MenuController.java                 # 菜单管理
│   ├── audit/AuditLogController.java            # 审计日志
│   ├── knowledge/KnowledgeController.java       # 知识库
│   ├── seckill/
│   │   ├── ActivityController.java              # 秒杀活动管理
│   │   ├── GoodsController.java                 # 秒杀商品管理
│   │   ├── SeckillController.java               # 秒杀下单/结果查询
│   │   ├── SeckillActivityController.java       # 秒杀活动查询
│   │   ├── SeckillSseController.java            # SSE 订阅/取消/状态查询
│   │   ├── PreheatController.java               # 活动预热
│   │   └── ReservationController.java           # 预约管理
│   ├── monitor/
│   │   ├── MonitorController.java               # 系统监控
│   │   └── hardware/HardwareMonitorController.java  # 硬件指标监控
│   ├── order/
│   │   ├── OrderController.java                 # 订单管理
│   │   └── CartController.java                  # 购物车
│   └── rocketmq/
│       ├── RocketMQController.java              # RocketMQ 管理
│       └── RocketMQDashboardController.java     # RocketMQ 仪表盘
│
├── service/                    # 服务层（接口 + 实现）
│   ├── auth/
│   │   ├── AuthService.java
│   │   └── impl/AuthServiceImpl.java
│   ├── user/
│   │   ├── UserService.java
│   │   └── impl/UserServiceImpl.java
│   ├── role/
│   │   ├── RoleService.java
│   │   └── impl/RoleServiceImpl.java
│   ├── menu/
│   │   ├── MenuService.java
│   │   └── impl/MenuServiceImpl.java
│   ├── audit/
│   │   ├── AuditService.java
│   │   └── impl/AuditServiceImpl.java
│   ├── knowledge/
│   │   ├── KnowledgeService.java
│   │   └── impl/KnowledgeServiceImpl.java
│   ├── seckill/
│   │   ├── SeckillService.java / impl/SeckillServiceImpl.java
│   │   ├── ActivityService.java / impl/ActivityServiceImpl.java
│   │   ├── GoodsService.java / impl/GoodsServiceImpl.java
│   │   ├── SignService.java / impl/SignServiceImpl.java
│   │   ├── PreheatService.java / impl/PreheatServiceImpl.java
│   │   ├── ReservationNotifyServiceImpl.java
│   │   ├── stock/
│   │   │   ├── StockService.java / impl/StockServiceImpl.java
│   │   │   ├── ReconcileService.java / impl/ReconcileServiceImpl.java
│   ├── monitor/
│   │   ├── MonitorService.java / impl/MonitorServiceImpl.java
│   │   ├── hardware/
│   │   │   ├── HardwareMetricsService.java / impl/HardwareMetricsServiceImpl.java
│   │   │   └── RingBuffer.java              # 无锁环形缓冲区
│   ├── order/
│   │   ├── OrderService.java / impl/OrderServiceImpl.java
│   │   ├── CartService.java / impl/CartServiceImpl.java
│   │   ├── TimeoutCancelService.java / impl/TimeoutCancelServiceImpl.java
│   └── rocketmq/
│       ├── RocketMQAdminService.java / impl/RocketMQAdminServiceImpl.java
│       ├── RocketMQClusterService.java / impl/...
│       ├── RocketMQConsumerService.java / impl/...
│       ├── RocketMQMessageService.java / impl/...
│       ├── RocketMQTopicService.java / impl/...
│       └── impl/RocketMQUtils.java
│
├── mapper/                     # MyBatis Plus 数据访问层
│   ├── user/UserMapper.java
│   ├── role/RoleMapper.java + RoleMenuPermissionMapper.java
│   ├── menu/MenuMapper.java
│   ├── audit/AuditLogMapper.java
│   ├── knowledge/ (5 mappers: Article/ArticleTag/Category/File/Tag)
│   ├── seckill/ (7 mappers: Activity/Goods/Log/Order/Queue/Reservation + stock/StockMapper)
│   ├── monitor/ (monitor-specific mapper)
│   ├── order/ (OrderMapper + CartMapper)
│   └── rocketmq/ (RocketMQ mappers)
│
├── entity/                     # 实体类（数据库表映射）
│   ├── user/User.java
│   ├── role/Role.java + RoleMenuPermission.java
│   ├── menu/Menu.java
│   ├── audit/AuditLog.java
│   ├── knowledge/ (Article/ArticleTag/Category/File/Tag)
│   ├── seckill/ (Activity/Goods/Log/Order/Queue/Reservation/IpBlacklist + stock/Stock)
│   ├── order/ (Order + Cart)
│   └── rocketmq/ (RocketMQ entities)
│
├── dto/                        # 数据传输对象
│   ├── ApiResponse.java        # 统一 API 响应（共享）
│   ├── auth/ (Auth DTOs)
│   ├── user/UserVO.java
│   ├── seckill/SeckillRequest.java + SeckillResponse.java
│   ├── knowledge/ (Knowledge DTOs)
│   └── monitor/ (monitor DTOs, hardware DTOs)
│
├── vo/                         # 视图对象
│   └── seckill/ActivityDetailVO.java
│
├── mq/                         # RocketMQ 消息
│   └── seckill/
│       ├── consumer/StockConsumer.java          # 库存扣减消费者（已注释 @RocketMQMessageListener）
│       └── producer/OrderTransactionProducer.java  # 订单事务消息生产者
│
├── sse/                        # SSE 推送
│   └── seckill/SeckillSseService.java          # 秒杀结果 SSE 推送
│
├── aspect/
│   └── AuditAspect.java        # 审计日志 AOP 切面
│
├── exception/
│   ├── BusinessException.java  # 业务异常（带 HTTP 状态码）
│   └── GlobalExceptionHandler.java  # 全局异常处理器
│
├── limiter/                    # 限流器
│   ├── QpsLimiter.java         # QPS 滑动窗口限流
│   ├── ConcurrencyLimiter.java # 并发数信号量限流
│   └── IpRateLimiter.java      # IP 速率限流
│
├── lock/                       # 分布式锁
│   ├── DistributedLockService.java        # 接口
│   └── impl/DistributedLockServiceImpl.java  # Redis 实现
│
└── util/
    └── ZstdUtil.java           # Zstd 压缩工具类
```

### Backend Resources

```
springboot/src/main/resources/
├── application.yaml            # 主配置（引入 config/*.yaml）
├── config/
│   ├── datasource.yaml         # 数据源配置
│   ├── cache.yaml              # Redis 缓存配置
│   ├── mq.yaml                 # RocketMQ 配置
│   ├── security.yaml           # 安全白名单配置
│   └── business.yaml           # 业务参数配置
├── db/migration/               # Flyway 数据库迁移脚本
│   ├── V1__init.sql            # 初始表结构
│   ├── V2__knowledge_base.sql  # 知识库表
│   ├── V3__fix_knowledge_menu_path.sql
│   ├── V4__remove_theme_settings.sql
│   ├── V5__user_management_menu.sql
│   ├── V6__add_audit_description.sql
│   ├── V7__add_menu_management.sql
│   ├── V8__seckill_activity.sql  # 秒杀活动表
│   ├── V9__seckill_goods.sql     # 秒杀商品表
│   ├── V10__seckill_queue.sql    # 秒杀排队表
│   ├── V11__seckill_order.sql    # 秒杀订单表
│   ├── V12__seckill_reservation.sql
│   ├── V13__stock.sql            # 库存表
│   ├── V14__cart.sql             # 购物车表
│   ├── V15__ip_blacklist.sql     # IP 黑名单
│   ├── V16__seckill_log.sql      # 秒杀日志
│   ├── V17__seckill_menu_and_permission.sql
│   ├── V18__rename_menu_to_learning_tools.sql
│   ├── V19__seckill_goods_activity_menu.sql
│   ├── V20__rocketmq_topic_menu.sql
│   ├── V21__rocketmq_consumer_group_menu.sql
│   ├── V22__fix_topic_menu_icon.sql
│   ├── V23__rocketmq_message_menu.sql
│   └── V24__rocketmq_dashboard_menu.sql
└── mapper/                     # MyBatis Plus XML 映射（复杂 SQL）
    ├── UserMapper.xml
    ├── MenuMapper.xml
    ├── KnowledgeArticleMapper.xml
    ├── KnowledgeArticleTagMapper.xml
    └── KnowledgeCategoryMapper.xml
```

### Backend Tests

```
springboot/src/test/java/cn/coderstory/springboot/
├── ArchitectureTest.java       # ArchUnit 分层架构规则测试
├── controller/
│   ├── monitor/MonitorControllerTest.java
│   └── monitor/hardware/HardwareMonitorControllerTest.java
├── service/
│   └── monitor/hardware/HardwareMetricsServiceImplTest.java
├── security/                   # 安全相关测试
├── seckill/
│   └── service/SeckillServiceImplTest.java
├── order/
│   └── service/OrderServiceImplTest.java
├── stock/
│   └── service/StockServiceImplTest.java
├── limiter/                    # 限流器测试
├── lock/                       # 分布式锁测试
└── exception/                  # 异常处理测试
```

## Key File Locations

**Entry Points:**
- `springboot/src/main/java/cn/coderstory/springboot/SpringbootApplication.java`: Spring Boot 应用入口
- `app-vue/src/main.ts`: Vue 3 应用入口
- `app-vue/src/router/index.ts`: 路由实例创建入口

**Configuration:**
- `springboot/build.gradle.kts`: 后端构建配置（依赖管理、插件、Flyway、Checkstyle）
- `springboot/settings.gradle.kts`: 项目设置，含版本目录 `libs.versions.toml`
- `app-vue/vite.config.ts`: 前端 Vite 构建配置
- `app-vue/tsconfig.json`: TypeScript 配置（`@/` -> `./src/*`）
- `app-vue/package.json`: 前端依赖与 npm 脚本
- `springboot/src/main/resources/application.yaml`: 后端主配置（引入 config/*.yaml）
- `springboot/src/main/resources/config/`: 配置拆分目录（datasource/cache/mq/security/business）

**Core Logic:**
- `springboot/src/main/java/cn/coderstory/springboot/config/JwtAuthenticationFilter.java`: JWT 认证过滤器
- `springboot/src/main/java/cn/coderstory/springboot/config/JwtTokenProvider.java`: JWT 令牌管理
- `springboot/src/main/java/cn/coderstory/springboot/config/SecurityConfig.java`: Spring Security 配置
- `springboot/src/main/java/cn/coderstory/springboot/aspect/AuditAspect.java`: 审计日志 AOP
- `springboot/src/main/java/cn/coderstory/springboot/exception/GlobalExceptionHandler.java`: 全局异常处理
- `springboot/src/main/java/cn/coderstory/springboot/exception/BusinessException.java`: 业务异常
- `springboot/src/main/java/cn/coderstory/springboot/dto/ApiResponse.java`: 统一响应封装
- `app-vue/src/api/request.ts`: Axios 实例（含 token 注入、401 自动刷新、错误消息提取）

**Testing:**
- `springboot/src/test/java/cn/coderstory/springboot/ArchitectureTest.java`: ArchUnit 分层架构验证
- `app-vue/vitest.config.ts`: 前端 Vitest 测试配置

## Naming Conventions

**Files:**

| Scope | Pattern | Example |
|-------|---------|---------|
| Vue components | `PascalCase.vue` | `AppHeader.vue`, `CategoryTree.vue` |
| Service interfaces | `*Service.java` | `UserService.java`, `SeckillService.java` |
| Service implementations | `*ServiceImpl.java` | `UserServiceImpl.java`, `SeckillServiceImpl.java` |
| Controllers | `*Controller.java` | `UserController.java`, `SeckillController.java` |
| Mappers | `*Mapper.java` | `UserMapper.java`, `SeckillActivityMapper.java` |
| Entities | PascalCase matching table | `User.java`, `SeckillOrder.java` |
| DTOs | Descriptive | `SeckillRequest.java`, `LoginResult.java` |
| Frontend API modules | `{domain}.ts` (camelCase) | `auth.ts`, `seckill.ts`, `rocketmq.ts` |
| Route files | `{name}.ts` | `index.ts`, `guards.ts`, `routes.ts` |

**Directories:**

| Scope | Pattern | Example |
|-------|---------|---------|
| Backend domain packages | Single word, lowercase | `user/`, `seckill/`, `rocketmq/`, `monitor/` |
| Backend layer packages | Single word, lowercase | `controller/`, `service/`, `mapper/`, `entity/` |
| Frontend views | Single word, lowercase | `views/auth/`, `views/seckill/`, `views/rocketmq/` |
| Frontend API modules | Single word, lowercase | `api/modules/auth.ts`, `api/modules/seckill.ts` |

## Where to Add New Code

**New Business Domain (e.g., `coupon`):**

Backend:
- Controller: `springboot/src/main/java/cn/coderstory/springboot/controller/coupon/CouponController.java`
- Service interface: `springboot/src/main/java/cn/coderstory/springboot/service/coupon/CouponService.java`
- Service impl: `springboot/src/main/java/cn/coderstory/springboot/service/coupon/impl/CouponServiceImpl.java`
- Mapper: `springboot/src/main/java/cn/coderstory/springboot/mapper/coupon/CouponMapper.java`
- Entity: `springboot/src/main/java/cn/coderstory/springboot/entity/coupon/Coupon.java`
- DTOs: `springboot/src/main/java/cn/coderstory/springboot/dto/coupon/`
- DB migration: `springboot/src/main/resources/db/migration/V25__coupon.sql` (with next version number)

Frontend:
- API module: `app-vue/src/api/modules/coupon.ts`
- Views: `app-vue/src/views/coupon/`
- Routes: Add to `app-vue/src/router/modules/routes.ts`

**New Shared Component:**
- Implementation: `app-vue/src/components/common/MyComponent.vue`

**New Shared Utility (backend):**
- Utility class: `springboot/src/main/java/cn/coderstory/springboot/util/MyUtil.java`

**New Configuration:**
- Config class: `springboot/src/main/java/cn/coderstory/springboot/config/MyConfig.java`
- Config properties: `springboot/src/main/resources/config/myfeature.yaml`

**New Cross-Cutting Aspect:**
- Aspect class: `springboot/src/main/java/cn/coderstory/springboot/aspect/MyAspect.java`

**New Test:**
- Backend unit test: `springboot/src/test/java/cn/coderstory/springboot/{domain}/` (mirrors main structure)
- Frontend test: `app-vue/src/{module}/__tests__/` (co-located with source)

## Special Directories

**`.gradle/`:** Gradle 构建缓存和工具链下载。生成目录，不提交。

**`.idea/`:** JetBrains IntelliJ IDEA 项目配置。生成目录，不提交。

**`.playwright/`:** Playwright 浏览器自动化测试数据。生成目录。

**`.planning/`:** GSD 工作流规划文件。包含阶段计划、代码库分析、执行摘要。提交到仓库。

---

*Structure analysis: 2026-05-10*
