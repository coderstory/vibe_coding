# 代码组织规范研究

**域:** Vue 3 + Spring Boot 管理后台代码组织标准
**研究日期:** 2026-05-06
**置信度:** HIGH（基于 Vue 官方风格指南 + 阿里巴巴 Java 开发手册 + 现有项目分析）

---

## 执行摘要

当前项目前端 (Vue 3 + Element Plus) 和后端 (Spring Boot + MyBatis Plus) 的目录结构存在不一致性。前端整体采用按域分视图（views/rocketmq/, views/seckill/），但 components/ 和 api/ 扁平化严重；后端混合了按层分包（根层 controller/service/entity/mapper/）和按域分包（seckill/, order/, stock/），两种风格共存导致新功能模块无处安放。

本次研究基于 Vue 官方风格指南、阿里巴巴 Java 开发手册（P3C）和现有项目代码分析，提供统一的代码组织规范、目录结构建议和命名约定。

---

## 一、目录组织：表需规范（Table Stakes）

每个项目必须满足的代码组织基本要求。

### 1.1 前端 (Vue 3 + TypeScript + Element Plus)

| 规范 | 为什么必须 | 实施难度 | 当前状态 |
|------|-----------|---------|---------|
| views/ 按业务域分目录 | 页面文件超过 10 个后查找困难。Vue 风格指南推荐按功能域组织 | 低 | 已基本实现，但 rocketmq/ 下 10 个文件扁平 |
| components/ 区分公共和私有组件 | 公共组件（跨页面复用）和私有组件（单页面使用）混放导致导入路径混乱 | 低 | 未实现，10 个 .vue 文件平铺在 components/ 根目录 |
| API 模块与后端 Controller 一一对应 | API 文件应与后端接口一致，便于定位和修改 | 低 | 基本实现，但 15 个 api 文件扁平在 api/ 根目录 |
| 每个组件独立一个 .vue 文件 | Vue 风格指南 Priority B 强制要求 | 低 | 已实现 |
| Composables 统一放在 composables/ 目录 | Composition API 复用逻辑集中管理，以 use 前缀命名 | 低 | 已实现，仅 useAnimationToggle.ts 一个文件 |
| Router 模块化拆分 | 单一路由文件随页面增加迅速膨胀 | 低 | 未实现，router/index.ts 单文件已 7KB+ |
| Store 按业务域拆分 | 单一 store 随业务增长膨胀为数千行 | 低 | 仅有 user.ts 一个 store |

### 1.2 后端 (Spring Boot + MyBatis Plus)

| 规范 | 为什么必须 | 实施难度 | 当前状态 |
|------|-----------|---------|---------|
| Controller/Service/Mapper 三层分离 | 阿里巴巴 Java 开发手册强制：禁止 Controller 直接调用 Mapper | 低 | 已实现 |
| Service 接口 + Impl 实现类 | 阿里巴巴规约强制：暴露服务应为接口，实现类用 Impl 后缀 | 低 | 部分实现（Menu/Role 有接口+实现，但 service/ 根目录混放） |
| Entity 与数据库表一一映射 | MyBatis Plus 依赖 entity 与表结构对应 | 低 | 已实现 |
| Mapper XML 统一放在 resources/mapper/ | MyBatis Plus 默认扫描路径 | 低 | 已实现 |
| 统一返回对象 (ApiResponse) | 前端需要一致格式处理成功/失败 | 低 | 已实现（vo/ApiResponse.java） |
| 全局异常处理器 | 避免异常栈直接暴露给前端 | 低 | 已实现（exception/） |

### 1.3 配置文件

| 规范 | 为什么必须 | 实施难度 | 当前状态 |
|------|-----------|---------|---------|
| 敏感信息不硬编码 | 硬编码凭证导致安全事故 | 低 | 已使用 ${DB_USER:root} 环境变量 |
| 区分环境配置文件 | 环境配置混用导致数据污染 | 中 | 有 application.yaml + application-test.yaml，但 test.yaml 大量重复 |
| application.yaml 按关注点拆分 | 单文件 150+ 行修改困难 | 中 | 未实现，application.yaml 是单一 154 行文件 |

---

## 二、目录组织：增强规范（Differentiators）

实施后可显著提升代码库长期维护性的模式。

### 2.1 前端增强规范

| 规范 | 价值 | 难度 | 建议阶段 |
|------|------|------|---------|
| components/ 划分 common/ 和 business/ 子目录 | common/ 存放纯 UI 组件（类似 Element Plus 的封装），business/ 存放复用业务组件。新开发者立即理解组件作用域 | 低 | Phase 1 |
| views/ 内部使用 components/ 子目录存放页面私有组件 | 页面子组件不混入全局 components/。seckill/activity/components/ 模式值得推广 | 低 | Phase 1 |
| API 模块按业务域分目录（api/modules/） | api/modules/rocketmq.ts 对应后端 RocketMQ 相关接口 | 低 | Phase 1 |
| router/ 拆分为 modules/ + guards.ts | router/modules/rocketmq.ts 包含所有 RocketMQ 路由，主入口仅聚合 | 低 | Phase 1 |
| 布局组件移入 components/layout/ | 布局组件（Layout.vue）不是业务页面，应独立于 views/ | 低 | Phase 1 |
| 页面组件统一使用 Page 后缀 | LoginPage.vue, TopicListPage.vue 直接表达"这是一个页面" | 低 | Phase 2 |
| TypeScript 类型定义按域拆分 | api/types.ts 当前 15KB+，应拆为 api/types/user.ts, api/types/rocketmq.ts 等 | 中 | Phase 2 |
| 复杂页面使用页面级目录（page.vue + components/ + composables/ + types.ts） | 复杂页面（如 SeckillDetail）所有相关文件就近放置 | 中 | Phase 2 |

### 2.2 后端增强规范

| 规范 | 价值 | 难度 | 建议阶段 |
|------|------|------|---------|
| 统一采用 package-by-feature（按业务域分包） | 消除当前混合风格（按层 vs 按域）的不一致性 | 中 | Phase 1 |
| 为每个 feature 包建立统一子包结构 | 每个业务域内部结构一致：controller/service/mapper/entity/dto/ | 中 | Phase 1 |
| 跨业务通用组件移至 shared/ 包 | shared/config/, shared/security/, shared/exception/, shared/util/ 等 | 中 | Phase 1 |
| DTO/VO 命名统一：入参用 DTO，出参用 VO | 当前 dto/ 和 vo/ 并存，命名含义不清晰 | 低 | Phase 1 |
| application.yaml 拆分为 5 个关注点文件 | 数据源、缓存、MQ、安全、业务配置各独立文件 | 中 | Phase 1 |
| application-{profile}.yaml 仅包含环境差异 | 消除 test.yaml 中与主配置重复的内容 | 中 | Phase 1 |
| @ConfigurationProperties 类型安全配置 | 避免 @Value 散布各处，集中类型安全的配置映射 | 中 | Phase 2 |

---

## 三、反模式（Anti-Features）

| 反模式 | 为什么有害 | 替代方案 |
|--------|-----------|---------|
| 后端包结构混用按层和按域两种风格 | 当前 controller/ 在根层（按层），而 seckill/controller/ 在子包（按域），令人困惑 | 全量迁移为按域分包，通用组件移至 shared/ |
| 前端 components/ 不放任何子目录 | 10 个 .vue 文件平铺，查找困难 | 划分 common/（纯 UI）、business/（业务）和 layout/（布局） |
| application-test.yaml 几乎完整复制主配置 | 修改主配置后测试环境未同步，背离 profile 的初衷 | test.yaml 仅覆盖差异项（数据库名、Redis 库号、连接池大小） |
| Service 接口和实现类放在同一目录 | 违反阿里巴巴规约：暴露的服务应为接口，Impl 后缀与接口区分 | service/ 目录仅放接口，service/impl/ 放实现 |
| router/index.ts 单文件包含所有路由和导航守卫 | 7KB+ 单文件，每增加一个页面都要修改 | 拆分为 router/modules/*.ts + router/guards.ts |
| Controller 中包含业务逻辑 | RocketMQController 有大量 MQ 管理逻辑内嵌 | 复杂逻辑下沉到 Service 层，Controller 仅做参数校验和转发 |
| 使用 type 而非 interface 定义对象类型 | type 不支持声明合并，extends 语法不如 interface 直观 | API 响应类型优先用 interface，联合类型可用 type |
| 无用的 Vite 模板文件残留 | HelloWorld.vue, TheWelcome.vue, WelcomeItem.vue 从未使用 | 移除 |

---

## 四、推荐目录结构

### 4.1 后端目标结构

```
springboot/src/main/java/cn/coderstory/springboot/
├── shared/                          ← 跨业务通用层
│   ├── config/                      ← 所有 @Configuration 类
│   │   ├── CorsConfig.java
│   │   ├── SecurityConfig.java
│   │   ├── WebConfig.java
│   │   ├── MyBatisPlusConfig.java
│   │   └── RedissonConfig.java
│   ├── security/                    ← JWT 过滤器等
│   ├── aspect/                      ← AOP 切面
│   ├── exception/                   ← 全局异常处理
│   ├── util/                        ← 通用工具类
│   ├── vo/                          ← 通用 VO (ApiResponse, ResultResponse)
│   ├── limiter/                     ← 限流组件
│   └── lock/                        ← 分布式锁组件
│       └── impl/
│
├── auth/                            ← 认证业务域
│   ├── controller/AuthController.java
│   └── service/
│       ├── AuthService.java
│       └── impl/AuthServiceImpl.java
│
├── user/                            ← 用户管理业务域
│   ├── controller/
│   │   ├── UserController.java
│   │   └── RoleController.java
│   ├── service/
│   │   ├── UserService.java
│   │   ├── RoleService.java
│   │   └── impl/
│   ├── mapper/
│   │   ├── UserMapper.java
│   │   └── RoleMapper.java
│   ├── entity/
│   │   ├── User.java
│   │   └── Role.java
│   └── dto/
│       └── UserQueryDTO.java
│
├── knowledge/                       ← 知识库业务域
│   ├── controller/KnowledgeController.java
│   ├── service/
│   │   ├── KnowledgeService.java
│   │   └── impl/
│   ├── mapper/
│   ├── entity/
│   └── dto/
│
├── menu/                            ← 菜单/权限业务域
│   ├── controller/MenuController.java
│   ├── service/
│   │   ├── MenuService.java
│   │   └── impl/
│   ├── mapper/MenuMapper.java
│   └── entity/Menu.java
│
├── audit/                           ← 审计日志业务域
│   ├── controller/AuditLogController.java
│   ├── service/
│   │   ├── AuditService.java
│   │   └── impl/
│   ├── mapper/
│   └── entity/AuditLog.java
│
├── seckill/                         ← 秒杀业务域
│   ├── controller/
│   ├── service/impl/
│   ├── mapper/
│   ├── entity/
│   ├── dto/
│   └── vo/
│
├── order/                           ← 订单业务域
│   ├── controller/
│   ├── service/impl/
│   ├── mapper/
│   └── entity/
│
├── stock/                           ← 库存业务域
│   ├── consumer/
│   ├── service/impl/
│   ├── mapper/
│   └── entity/
│
├── rocketmq/                        ← RocketMQ 管理业务域
│   ├── controller/
│   │   ├── RocketMQController.java
│   │   └── RocketMQDashboardController.java
│   ├── service/
│   │   ├── RocketMQAdminService.java
│   │   └── impl/
│   └── dto/
│
├── monitor/                         ← 监控业务域
│   ├── controller/
│   └── service/
│
├── mq/                              ← MQ 基础设施（非业务）
│   ├── consumer/
│   └── producer/
│
└── sse/                             ← SSE 基础设施
```

### 4.2 前端目标结构

```
app-vue/src/
├── api/
│   ├── request.ts                   ← Axios 实例 + 拦截器
│   ├── types/                       ← 类型定义按域拆分
│   │   ├── index.ts                 ← 通用类型 (ApiResponse 等)
│   │   ├── user.ts
│   │   ├── rocketmq.ts
│   │   └── seckill.ts
│   ├── modules/                     ← API 模块按业务域拆分
│   │   ├── auth.ts
│   │   ├── user.ts
│   │   ├── role.ts
│   │   ├── menu.ts
│   │   ├── rocketmq.ts
│   │   ├── seckill.ts
│   │   ├── knowledge.ts
│   │   ├── audit.ts
│   │   ├── monitor.ts
│   │   ├── order.ts
│   │   ├── goods.ts
│   │   ├── activity.ts
│   │   └── cart.ts
│   └── index.ts                     ← 统一导出
│
├── assets/
│   ├── styles/                      ← 全局样式
│   │   ├── variables.css            ← CSS 变量
│   │   ├── reset.css                ← 重置样式
│   │   └── global.css               ← 全局样式
│   ├── images/                      ← 静态图片
│   └── icons/                       ← SVG 图标
│
├── components/
│   ├── common/                      ← 纯 UI 组件（无业务状态依赖）
│   │   ├── BaseButton.vue
│   │   ├── BaseTable.vue
│   │   ├── BaseDialog.vue
│   │   └── BaseCard.vue
│   ├── layout/                      ← 布局组件（从 views/layout/ 迁移）
│   │   ├── AppHeader.vue
│   │   ├── AppMenu.vue
│   │   ├── AppTabs.vue
│   │   └── AppLayout.vue
│   └── business/                    ← 复用业务组件（依赖业务状态/API）
│       ├── UserSelector.vue
│       └── StatusBadge.vue
│
├── composables/
│   ├── useAnimationToggle.ts
│   ├── usePermission.ts
│   ├── useTable.ts
│   └── usePagination.ts
│
├── router/
│   ├── index.ts                     ← 路由实例 + 全局配置
│   ├── guards.ts                    ← 导航守卫
│   └── modules/                     ← 路由模块
│       ├── index.ts                 ← 聚合导出
│       ├── auth.ts
│       ├── dashboard.ts
│       ├── system.ts
│       ├── rocketmq.ts
│       ├── seckill.ts
│       ├── knowledge.ts
│       ├── monitor.ts
│       └── order.ts
│
├── store/
│   ├── index.ts                     ← Pinia 实例
│   ├── user.ts
│   ├── app.ts                       ← 应用级状态（侧边栏折叠等）
│   └── permission.ts                ← 权限/菜单状态
│
└── views/                           ← 页面组件（按业务域分目录）
    ├── auth/
    │   └── LoginPage.vue
    ├── dashboard/
    │   └── DashboardPage.vue
    ├── error/
    │   ├── NotFoundPage.vue
    │   └── ForbiddenPage.vue
    ├── system/
    │   ├── UserManagementPage.vue
    │   ├── UserDetailPage.vue
    │   ├── RoleManagementPage.vue
    │   └── MenuManagementPage.vue
    ├── rocketmq/
    │   ├── TopicListPage.vue
    │   ├── ConsumerGroupListPage.vue
    │   ├── ConsumerGroupDetailPage.vue
    │   ├── MessageListPage.vue
    │   ├── DashboardPage.vue
    │   └── components/              ← 页面私有子组件
    │       ├── BrokerStatusTable.vue
    │       ├── OverviewCard.vue
    │       ├── QpsChart.vue
    │       ├── TopicBacklogTable.vue
    │       └── ResetOffsetDialog.vue
    ├── seckill/
    │   ├── SeckillIndexPage.vue
    │   ├── SeckillDetailPage.vue
    │   ├── SeckillCartPage.vue
    │   ├── SeckillRecordPage.vue
    │   ├── MyReservationsPage.vue
    │   ├── activity/
    │   │   ├── ActivityListPage.vue
    │   │   └── ActivityFormPage.vue
    │   └── goods/
    │       ├── GoodsListPage.vue
    │       └── GoodsFormPage.vue
    ├── order/
    │   ├── OrderListPage.vue
    │   └── OrderConfirmPage.vue
    ├── monitor/
    │   └── MonitorDashboardPage.vue
    ├── audit/
    │   └── AuditLogPage.vue
    └── business/
        └── BusinessDataPage.vue
```

### 4.3 配置文件目标结构

```
springboot/src/main/resources/
├── config/                          ← 拆分的配置文件
│   ├── application-datasource.yaml      ← 数据源 + Flyway + MyBatis Plus
│   ├── application-cache.yaml           ← Redis + Redisson
│   ├── application-mq.yaml              ← RocketMQ
│   ├── application-security.yaml        ← JWT + Security 白名单
│   └── application-business.yaml        ← 秒杀业务配置 (seckill.*)
├── application.yaml                 ← 主配置（应用名、端口、config import）
├── application-dev.yaml             ← 开发环境覆盖（仅差异项）
├── application-test.yaml            ← 测试环境覆盖（仅差异项）
├── application-prod.yaml            ← 生产环境覆盖（仅差异项）
├── db/
│   └── migration/                   ← Flyway 迁移脚本（保持不变）
│       └── V*.sql
└── mapper/                          ← MyBatis Mapper XML（保持不变）
    └── *.xml
```

主配置 `application.yaml` 使用 `spring.config.import`：

```yaml
spring:
  application:
    name: admin-system
  config:
    import:
      - classpath:config/application-datasource.yaml
      - classpath:config/application-cache.yaml
      - classpath:config/application-mq.yaml
      - classpath:config/application-security.yaml
      - classpath:config/application-business.yaml

server:
  port: 8080

logging:
  level:
    cn.coderstory: DEBUG
    org.flywaydb: DEBUG
```

---

## 五、命名规范

### 5.1 前端命名规范

| 元素 | 规范 | 示例 | 源 |
|------|------|------|-----|
| 页面组件文件 | PascalCase，推荐 Page 后缀 | `LoginPage.vue`, `TopicListPage.vue` | Vue 风格指南 + 项目实践 |
| 公共 UI 组件 | PascalCase，Base/App 前缀 | `BaseButton.vue`, `BaseTable.vue` | Vue 风格指南 Priority B |
| 业务组件 | PascalCase，描述性命名 | `UserSelector.vue`, `StatusBadge.vue` | Vue 风格指南 |
| 页面私有子组件 | PascalCase，放在页面目录的 components/ 下 | `seckill/components/CountdownTimer.vue` | Vue 风格指南（紧密耦合组件规则） |
| Composables | camelCase，use 前缀 | `useAnimationToggle.ts`, `usePermission.ts` | Vue 官方文档 |
| Store | camelCase，描述性命名 | `user.ts`, `app.ts`, `permission.ts` | Pinia 官方建议 |
| API 模块 | kebab-case，与后端 Controller 域对应 | `rocketmq.ts`, `seckill.ts` | 项目实践 |
| 类型定义 | PascalCase 接口名，kebab-case 文件名 | `types/user.ts` 中 `interface UserInfo` | TypeScript 惯例 |
| Router 模块 | kebab-case | `modules/rocketmq.ts` | Vue Router 惯例 |
| 目录名 | kebab-case | `user-management/`, `rocketmq/` | 前端项目惯例 |

### 5.2 后端命名规范

| 元素 | 规范 | 示例 | 源 |
|------|------|------|-----|
| Controller 类 | PascalCase + Controller 后缀 | `UserController.java` | Spring MVC 惯例 |
| Service 接口 | PascalCase + Service 后缀 | `UserService.java` | 阿里巴巴规约 |
| Service 实现 | PascalCase + ServiceImpl 后缀 | `UserServiceImpl.java` | 阿里巴巴规约（强制） |
| Mapper 接口 | PascalCase + Mapper 后缀 | `UserMapper.java` | MyBatis Plus 惯例 |
| Entity | PascalCase，与表名对应（下划线转驼峰） | `User.java`, `SeckillActivity.java` | MyBatis Plus 惯例 |
| DTO（数据传输对象） | PascalCase + DTO 后缀 | `UserQueryDTO.java`, `SeckillRequestDTO.java` | 阿里巴巴规约 |
| VO（视图对象） | PascalCase + VO 后缀 | `UserVO.java`, `SeckillResultVO.java` | 阿里巴巴规约 |
| Config 类 | PascalCase + Config 后缀 | `CorsConfig.java`, `SecurityConfig.java` | Spring Boot 惯例 |
| Utils 工具类 | PascalCase + Utils 后缀 | `JwtUtils.java`, `RedisUtils.java` | 阿里巴巴规约 |
| Exception 类 | PascalCase + Exception 后缀 | `BusinessException.java` | 阿里巴巴规约（强制） |
| 包名 | 全小写，单数形式，点分隔 | `cn.coderstory.springboot.seckill` | 阿里巴巴规约（强制） |
| 方法名 | lowerCamelCase，动词前缀 | `getUserById()`, `listTopics()`, `updateStatus()` | 阿里巴巴规约（强制） |

### 5.3 Service/DAO 方法命名前缀（阿里巴巴规约）

| 前缀 | 用途 | 示例 |
|------|------|------|
| `get` | 获取单个对象 | `getUserById(Long id)` |
| `list` | 获取多个对象 | `listUsersByRole(String role)` |
| `count` | 获取统计值 | `countActiveUsers()` |
| `save` / `insert` | 插入 | `saveUser(User user)` |
| `remove` / `delete` | 删除 | `removeUser(Long id)` |
| `update` | 修改 | `updateUserStatus(Long id, Integer status)` |

---

## 六、包结构选择：package-by-feature vs package-by-layer

### 6.1 结论：采用 package-by-feature（按业务域分包）

**理由：**

1. **当前项目已部分采用**：seckill/, order/, stock/, monitor/ 已按域分包，是项目中最清晰的模块。继续朝此方向统一而非回退
2. **业务域边界清晰**：项目有明确的业务域划分（用户管理、知识库、秒杀、RocketMQ 管理、审计），符合按域分包的前提条件
3. **阿里巴巴规约推荐**：应用分层指南建议将 Web 层（Controller）、Service 层、DAO 层作为逻辑分层，具体组织可按业务模块划分
4. **包内高内聚**：修改一个业务功能只需在一个包内操作（controller + service + mapper + entity 都在同一业务域包下）
5. **新功能添加简单**：新增一个业务域只需新建一个包，复制标准子包结构即可

**边界情况处理：**

- **纯技术组件**（config/, security/, aspect/, exception/, util/, limiter/, lock/）→ 放入 `shared/` 包
- **MQ 基础设施**（consumer/, producer/）→ 保留在 `mq/` 包，因为跨业务域共用
- **SSE 基础设施** → 保留在 `sse/` 包
- **简单 CRUD 模块**（audit/）→ 同样按域分包，保持一致性

### 6.2 前端：继续保持 package-by-feature

前端天然就是按域分视图的模式（views/rocketmq/, views/seckill/），只需要将其他目录（components/, api/, router/）也统一应用此模式即可。

---

## 七、规范依赖关系

```
Phase 1 必须完成（阻断项）:
    后端统一按域分包 ──→ 决定几乎所有后续后端规范的实现方式
    application.yaml 拆分 ──→ 不依赖代码变更，可最早独立完成
    消除 test.yaml 重复 ──→ 依赖 application.yaml 拆分结果
    service/ 接口与实现分离 ──→ 与按域分包同时进行，避免二次移动

Phase 1 独立可执行:
    前端 components/ 分区 ──→ 不依赖其他变更
    前端 API 模块分目录 ──→ 不依赖其他变更
    前端 router 模块拆分 ──→ 不依赖其他变更（但建议先调整 views 结构）
    前端 views 内组件整理 ──→ 不依赖其他变更

Phase 2 依赖 Phase 1:
    Entity/DTO/VO 命名统一 ──→ 依赖后端统一分包完成
    TS 类型定义拆分 ──→ 依赖 API 模块分目录完成
    @ConfigurationProperties ──→ 依赖 application.yaml 拆分完成
    页面级目录规范 ──→ 依赖 Phase 1 views 调整完成
```

---

## 八、优先级矩阵

| 规范 | 维护性价值 | 实施成本 | 优先级 |
|------|-----------|---------|--------|
| application.yaml 拆分 | 高（每次改配置受益） | 中 | P1 |
| 消除 test.yaml 重复 | 高（防止配置不同步） | 低 | P1 |
| 后端统一按域分包 | 高（新开发者定位代码） | 中 | P1 |
| service/ 接口与实现分离 | 中（代码一致性） | 低 | P1 |
| 前端 components/ 分区 | 中（查找组件效率） | 低 | P1 |
| 前端 API 模块分目录 | 中（定位 API 效率） | 低 | P1 |
| 前端 router 模块拆分 | 中（路由管理效率） | 低 | P1 |
| rocketmq/ views 分离页面与子组件 | 中（目录整洁度） | 低 | P1 |
| 前端 views/ 页面命名加 Page 后缀 | 低（视觉一致性） | 低 | P2 |
| Entity/DTO/VO 命名统一 | 中（类型语义清晰） | 中 | P2 |
| TS 类型定义拆分 | 中（类型管理效率） | 中 | P2 |
| @ConfigurationProperties 配置类 | 中（配置类型安全） | 中 | P2 |
| 页面级目录规范（复杂页面） | 中（仅复杂页面） | 中 | P2 |
| 清理无用代码/模板 | 低 | 低 | P2 |

---

## 九、实施检查清单

### Phase 1: 结构统一

**配置文件：**
- [ ] 创建 resources/config/ 目录，拆分 5 个关注点配置文件
- [ ] 修改 application.yaml 添加 spring.config.import
- [ ] 精简 application-test.yaml 仅保留环境差异
- [ ] 验证应用在所有 profile 下正常启动

**后端目录：**
- [ ] 创建 shared/ 包，迁移 config/, security/, aspect/, exception/, util/, vo/, limiter/, lock/
- [ ] 创建 user/ 包，迁移 UserController, RoleController 及相关 service/mapper/entity
- [ ] 创建 auth/ 包，迁移 AuthController 及相关 service
- [ ] 创建 menu/ 包，迁移 MenuController 及相关 service/mapper/entity
- [ ] 创建 knowledge/ 包，迁移 KnowledgeController 及相关 service/mapper/entity
- [ ] 创建 audit/ 包，迁移 AuditLogController 及相关 service/mapper/entity
- [ ] 创建 rocketmq/ 包，迁移 RocketMQController, RocketMQDashboardController 及相关 service
- [ ] 整理 service/impl/ 目录：所有实现类必须放在 impl/ 子目录中
- [ ] 删除旧的根层 controller/, service/, entity/, mapper/, vo/ 目录

**前端目录：**
- [ ] components/ 划分为 common/, layout/, business/ 子目录
- [ ] 将 AppHeader.vue, AppMenu.vue, AppTabs.vue 移入 components/layout/
- [ ] views/layout/Layout.vue 移入 components/layout/AppLayout.vue
- [ ] 移除无用模板文件（HelloWorld.vue, TheWelcome.vue, WelcomeItem.vue）
- [ ] api/ 创建 modules/ 子目录，按业务域分类
- [ ] api/types.ts 重度拆分可延后到 Phase 2
- [ ] router/index.ts 拆分为 modules/ + guards.ts
- [ ] views/rocketmq/ 页面子组件移入 rocketmq/components/
- [ ] 更新所有 import 路径

### Phase 2: 规范化

**后端：**
- [ ] Entity/DTO/VO 后缀统一
- [ ] 引入 @ConfigurationProperties 类型安全配置
- [ ] 补充 package-info.java

**前端：**
- [ ] views/ 页面组件统一加 Page 后缀
- [ ] api/types/ 按域拆分类型定义
- [ ] 复杂页面（SeckillDetail）实施页面级目录规范

---

## 来源

- [Vue.js 官方风格指南 — Priority B: Strongly Recommended](https://vuejs.org/style-guide/rules-strongly-recommended) — HIGH 置信度（官方文档原始 Markdown 已获取）
- [Vue.js Composables 官方文档](https://vuejs.org/guide/reusability/composables) — HIGH 置信度（官方文档原始 Markdown 已获取）
- [阿里巴巴 Java 开发手册 — 工程结构/应用分层](https://github.com/alibaba/p3c) — HIGH 置信度（原始文档已获取）
- [阿里巴巴 Java 开发手册 — 编程规约/命名风格](https://github.com/alibaba/p3c) — HIGH 置信度（原始文档已获取）
- Spring Boot Reference Documentation — MEDIUM 置信度（未获取最新版本原始文档，基于广泛社区的惯例）
- 现有项目代码分析（app-vue/src/ 和 springboot/src/main/java/）— HIGH 置信度（直接分析项目代码）

---
*代码组织规范研究完成于: 2026-05-06*
*适用于: v1.5 前后端代码重构与目录整理里程碑*
