# 后台管理系统架构研究

**Domain:** 企业内部后台管理系统
**Researched:** 2026-04-02
**Confidence:** HIGH

## 概览

本项目是一个面向企业内部使用的后台管理系统，采用 Vue 3 (SPA) + Spring Boot 4.0.5 (REST API) + MySQL 技术栈。需要实现认证授权、数据审计、CRUD 操作等功能。

---

## 推荐架构

### 系统分层概览

```
┌─────────────────────────────────────────────────────────────────┐
│                        前端 (Vue 3 SPA)                          │
├─────────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐            │
│  │   Views      │  │  Components  │  │   Router     │            │
│  │  (页面视图)   │  │  (组件库)    │  │  (路由管理)   │            │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘            │
│         │                │                │                     │
│  ┌──────┴────────────────┴────────────────┴───────┐              │
│  │              Pinia Store (状态管理)              │              │
│  │  ┌────────┐  ┌────────┐  ┌────────┐           │              │
│  │  │  User  │  │  Menu  │  │  Tab   │           │              │
│  │  └────────┘  └────────┘  └────────┘           │              │
│  └────────────────────┬─────────────────────────┘              │
│                       │                                       │
│  ┌────────────────────┴─────────────────────────┐              │
│  │              Services (API 调用层)              │              │
│  └───────────────────────────────────────────────┘              │
└─────────────────────────────────────────────────────────────────┘
                           │ HTTP/REST
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                      后端 (Spring Boot)                          │
├─────────────────────────────────────────────────────────────────┤
│  ┌──────────────────────────────────────────────────────┐       │
│  │              Controller (REST 端点)                    │       │
│  │  /api/auth/*  /api/users/*  /api/audit/*  /api/*     │       │
│  └──────────────────────┬───────────────────────────────┘       │
│                          │                                       │
│  ┌──────────────────────┴───────────────────────────────┐       │
│  │              Service (业务逻辑层)                        │       │
│  │  AuthService  UserService  AuditService  BizService     │       │
│  └──────────────────────┬───────────────────────────────┘        │
│                          │                                       │
│  ┌──────────────────────┴───────────────────────────────┐        │
│  │              Repository (数据访问层)                   │        │
│  │  JPA Repositories  +  MyBatis (复杂查询)              │        │
│  └──────────────────────┬───────────────────────────────┘        │
│                          │                                       │
│  ┌──────────────────────┴───────────────────────────────┐        │
│  │              Database (MySQL)                         │        │
│  │  users  roles  permissions  audit_logs  biz_tables    │        │
│  └───────────────────────────────────────────────────────┘        │
└─────────────────────────────────────────────────────────────────┘
```

---

## 前端模块划分

### 推荐目录结构

```
app-vue/src/
├── api/                    # API 接口定义
│   ├── index.js           # Axios 实例配置
│   ├── auth.js            # 认证相关 API
│   ├── user.js            # 用户管理 API
│   └── audit.js          # 审计日志 API
│
├── assets/                # 静态资源
│   ├── styles/           # 全局样式
│   └── images/          # 图片资源
│
├── components/           # 公共组件（可复用）
│   ├── common/          # 基础组件
│   │   ├── BaseButton.vue
│   │   ├── BaseTable.vue
│   │   └── BaseModal.vue
│   ├── icons/          # 图标组件
│   └── layout/         # 布局组件
│       ├── AppSidebar.vue    # 左侧菜单
│       ├── AppHeader.vue     # 顶部栏
│       └── AppTabbar.vue     # 页签栏
│
├── composables/          # 组合式函数（Vue 3）
│   ├── useAuth.js        # 认证逻辑
│   ├── useTable.js       # 表格操作
│   └── usePermission.js  # 权限判断
│
├── router/               # 路由配置
│   ├── index.js         # 路由主文件
│   └── guards.js        # 路由守卫
│
├── store/                # Pinia 状态管理
│   ├── index.js         # Store 入口
│   ├── modules/
│   │   ├── user.js      # 用户状态
│   │   ├── permission.js # 权限状态
│   │   └── tabs.js      # 页签状态
│   └── getters.js       # 计算属性
│
├── utils/                # 工具函数
│   ├── request.js       # Axios 封装
│   ├── storage.js       # 存储封装
│   └── format.js        # 格式化函数
│
├── views/                # 页面视图（按模块组织）
│   ├── login/           # 登录模块
│   │   └── Login.vue
│   ├── layout/          # 主布局
│   │   └── MainLayout.vue
│   ├── user/            # 用户管理模块
│   │   ├── UserList.vue
│   │   └── UserForm.vue
│   └── audit/           # 审计日志模块
│       └── AuditLog.vue
│
├── App.vue              # 根组件
└── main.js             # 入口文件
```

### 模块划分原则

| 模块类型 | 说明 | 划分依据 |
|---------|------|---------|
| **Views** | 页面级组件 | 按业务域划分（用户、审计、业务数据） |
| **Components** | 可复用组件 | 跨多个模块使用、无业务逻辑 |
| **Composables** | 可复用逻辑 | 多个组件共享的逻辑抽取 |
| **Store** | 状态管理 | 按状态类型划分（用户、权限、UI状态） |

### 前端状态流

```
用户登录
    ↓
[Pinia User Store] ←→ [API Service] ←→ [后端 API]
    ↓
[路由守卫] 检查登录状态 + 权限
    ↓
[主布局] 加载菜单 + 用户信息
    ↓
[页签状态] 记录打开的页面
```

---

## 后端 REST API 设计

### API 目录结构

```
cn.coderstory.springboot/
├── config/                  # 配置类
│   ├── SecurityConfig.java    # Spring Security 配置
│   ├── CorsConfig.java        # CORS 配置
│   └── WebConfig.java         # Web 配置
│
├── controller/              # REST 控制器
│   ├── AuthController.java     # 认证接口
│   ├── UserController.java     # 用户管理
│   ├── RoleController.java     # 角色管理
│   ├── AuditController.java    # 审计日志
│   └── [业务模块]/
│
├── service/                 # 业务逻辑层
│   ├── AuthService.java
│   ├── UserService.java
│   ├── RoleService.java
│   ├── AuditService.java
│   └── impl/
│
├── repository/             # 数据访问层
│   ├── UserRepository.java
│   ├── RoleRepository.java
│   ├── AuditLogRepository.java
│   └── [业务Repository]
│
├── entity/                 # 实体类
│   ├── User.java
│   ├── Role.java
│   ├── Permission.java
│   ├── AuditLog.java
│   └── [业务实体]
│
├── dto/                    # 数据传输对象
│   ├── request/           # 请求 DTO
│   └── response/          # 响应 DTO
│
├── security/              # 安全相关
│   ├── JwtService.java      # JWT 服务
│   ├── JwtAuthFilter.java   # JWT 过滤器
│   └── UserDetailsServiceImpl.java
│
├── audit/                 # 审计相关
│   ├── AuditAspect.java      # 审计切面
│   └── AuditType.java       # 审计类型枚举
│
└── exception/             # 异常处理
    ├── GlobalExceptionHandler.java
    └── BusinessException.java
```

### REST API 规范

| 方法 | 端点 | 说明 |
|------|------|------|
| POST | /api/auth/login | 用户登录 |
| POST | /api/auth/logout | 用户登出 |
| GET | /api/auth/me | 获取当前用户 |
| GET | /api/users | 获取用户列表 |
| GET | /api/users/{id} | 获取单个用户 |
| POST | /api/users | 创建用户 |
| PUT | /api/users/{id} | 更新用户 |
| DELETE | /api/users/{id} | 删除用户 |
| GET | /api/audit/logs | 获取审计日志 |
| GET | /api/roles | 获取角色列表 |
| GET | /api/menus | 获取菜单树 |

### 响应格式

```json
// 成功响应
{
  "code": 200,
  "message": "success",
  "data": { ... }
}

// 错误响应
{
  "code": 400,
  "message": "参数错误",
  "data": null
}
```

---

## 认证授权设计

### 推荐方案：JWT 认证

**为什么选择 JWT 而非 Session：**

| 维度 | JWT | Session |
|------|-----|---------|
| 存储 | 前端 localStorage/cookie | 服务端 Redis/数据库 |
| 扩展性 | 无状态，易于水平扩展 | 需要共享 session 存储 |
| 移动端 | 天然支持 | 需要额外处理 |
| 复杂度 | 实现简单 | 需要 session 管理 |
| 撤销 | 需黑名单或短 token | 直接删除即可 |

**适用场景：** 本项目是内部后台系统，用户量有限（通常 <1000），JWT 完全满足需求。

### JWT 实现要点

```
1. 登录成功后，服务器生成 JWT 返回给前端
2. 前端在每次请求的 Header 中携带 JWT
3. 服务器通过 Filter 验证 JWT 并设置 SecurityContext
4. 使用 @PreAuthorize 进行方法级权限控制
```

### 密码安全

```java
// 使用 BCrypt 加密，强度因子 12
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
}
```

### 权限控制层级

| 层级 | 实现方式 | 适用场景 |
|------|---------|---------|
| **URL 级** | SecurityConfig.requestMatchers | 路由拦截 |
| **方法级** | @PreAuthorize | 细粒度控制 |
| **数据级** | Service 中手动判断 | 行级权限 |

---

## 数据审计设计

### 审计策略选择

| 方案 | 实现方式 | 适用场景 |
|------|---------|---------|
| **JPA Auditing** | @CreatedDate, @LastModifiedDate | 记录创建/修改时间 |
| **AOP 切面** | 自定义 @Audit 注解 | 记录操作人和详情 |
| **Hibernate Envers** | 自动版本化实体 | 需要完整历史记录 |
| **业务日志表** | 手动记录关键操作 | 灵活定制审计内容 |

**本项目推荐：AOP 切面 + 业务日志表**

### 审计表设计

```sql
-- 审计日志表
CREATE TABLE audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,           -- 操作人
    username VARCHAR(50),               -- 操作人姓名（冗余便于查询）
    operation VARCHAR(50) NOT NULL,    -- 操作类型：CREATE/UPDATE/DELETE/LOGIN
    module VARCHAR(50) NOT NULL,       -- 模块：用户管理/业务数据/认证
    target_type VARCHAR(100),          -- 操作对象类型：User, Order 等
    target_id VARCHAR(100),            -- 操作对象ID
    detail TEXT,                       -- 操作详情（JSON 格式）
    ip_address VARCHAR(50),            -- IP 地址
    user_agent VARCHAR(500),          -- 浏览器信息
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (user_id),
    INDEX idx_module (module),
    INDEX idx_created_at (created_at)
);
```

### AOP 审计实现

```java
// 1. 定义审计注解
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    String module();        // 模块名
    String operation();    // 操作类型
}

// 2. 审计切面
@Aspect
@Component
public class AuditAspect {
    
    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint pjp, Auditable auditable) throws Throwable {
        // 记录操作前状态
        // 执行方法
        Object result = pjp.proceed();
        // 记录操作后状态和结果
        saveAuditLog(auditable, pjp, result);
        return result;
    }
}

// 3. 使用示例
@Service
public class UserService {
    
    @Auditable(module = "用户管理", operation = "创建用户")
    public User createUser(UserDTO dto) {
        // ...
    }
}
```

### 审计记录时机

| 操作类型 | 记录时机 | 记录内容 |
|---------|---------|---------|
| **登录/登出** | 认证成功/请求时 | 用户、IP、时间、结果 |
| **增删改** | 方法执行后 | 操作人、对象、变更前后值 |
| **敏感查询** | 方法执行后 | 查询条件、返回记录数 |

---

## 数据流

### 用户登录流程

```
┌─────────┐    POST /api/auth/login     ┌─────────────┐
│  前端   │ ─────────────────────────▶  │  Controller │
│  登录页 │                             └──────┬──────┘
└─────────┘                                   │
                                              ▼
┌─────────┐                            ┌─────────────┐
│ local   │◀─────────────────────────  │   Service   │
│Storage/ │     返回 JWT Token          │  验证密码    │
│ Cookie  │                             └──────┬──────┘
└─────────┘                                   │
                                              ▼
                                     ┌─────────────┐
                                     │  Database   │
                                     │  查询用户    │
                                     └─────────────┘
```

### 业务操作流程

```
┌─────────┐    GET/POST/PUT/DELETE    ┌─────────────┐
│  前端   │ ─────────────────────────▶  │   Filter    │
│  业务页 │   Header: Authorization    │  JWT 验证    │
└─────────┘                             └──────┬──────┘
                                              │
                                              ▼
┌─────────┐                            ┌─────────────┐
│  Pinia  │◀─────────────────────────  │  Controller │
│  Store  │     返回 Response           │  参数校验    │
└─────────┘                             └──────┬──────┘
                                              │
                                              ▼
                                     ┌─────────────┐
                                     │   Service    │
                                     │  业务逻辑    │
                                     └──────┬──────┘
                                            │
                        ┌───────────────────┼───────────────────┐
                        ▼                   ▼                   ▼
               ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
               │  Audit Log  │     │  Repository │     │  其他 Service│
               │  保存日志    │     │  数据操作    │     │             │
               └─────────────┘     └──────┬──────┘     └─────────────┘
                                          │
                                          ▼
                                 ┌─────────────┐
                                 │   MySQL     │
                                 │   数据库    │
                                 └─────────────┘
```

---

## 可扩展性考虑

| 规模 | 架构调整 | 关键优化点 |
|------|---------|----------|
| **0-100 用户** | 单体架构，无需调整 | 基本 CRUD 足够 |
| **100-1000 用户** | 添加缓存（Redis） | 查询缓存、Session 缓存 |
| **1000+ 用户** | 数据库读写分离 | 主从复制、连接池调优 |
| **10000+ 用户** | 微服务拆分 | 按模块拆分为独立服务 |

### 早期预防措施

1. **数据库索引** — 为高频查询字段添加索引
2. **分页查询** — 列表接口必须支持分页
3. **日志异步化** — 审计日志写入不阻塞主流程
4. **事务控制** — 合理设置事务边界，避免长事务

---

## 构建顺序建议

### Phase 1: 基础设施层

```
1. 项目脚手架搭建
   ├── Spring Boot 项目结构
   ├── Vue 3 项目结构
   └── 数据库初始化

2. 认证授权基础
   ├── 数据库用户/角色表
   ├── Spring Security + JWT 配置
   ├── 登录/登出 API
   └── 前端登录页面 + 路由守卫

3. 基础 UI 框架
   ├── 主布局组件（侧边栏、顶栏）
   ├── 路由配置
   └── 状态管理基础
```

**交付物：** 可登录的管理后台框架

### Phase 2: 用户管理模块

```
1. 后端
   ├── 用户 CRUD API
   ├── 角色管理 API
   └── 审计日志基础

2. 前端
   ├── 用户列表页
   ├── 用户表单（新增/编辑）
   ├── 角色管理页面
   └── 审计日志查看页
```

**交付物：** 完整的用户和权限管理功能

### Phase 3: 业务数据管理

```
1. 后端
   ├── 业务数据 CRUD
   ├── 数据导入导出
   └── 业务数据审计

2. 前端
   ├── 业务数据列表页
   ├── 业务数据表单
   ├── 导入导出功能
   └── 数据统计（可选）
```

**交付物：** 业务数据管理功能

### Phase 4: 完善与优化

```
1. 安全性加固
   ├── 接口限流
   ├── SQL 注入防护
   └── XSS 防护

2. 性能优化
   ├── 分页优化
   ├── 缓存策略
   └── 前端懒加载

3. 功能完善
   ├── 通知系统（可选）
   └── 数据备份（可选）
```

---

## 反模式避免

### 后端反模式

| 反模式 | 问题 | 正确做法 |
|--------|------|---------|
| **在 Controller 做业务逻辑** | 代码重复、难以测试 | 业务逻辑放在 Service 层 |
| **直接返回实体** | 暴露内部结构 | 使用 DTO 转换 |
| **N+1 查询** | 数据库压力大 | 使用 JOIN 或批量查询 |
| **大事务** | 数据库锁时间长 | 拆分事务范围 |

### 前端反模式

| 反模式 | 问题 | 正确做法 |
|--------|------|---------|
| **组件嵌套过深** | 难以维护 | 组件扁平化、Props 传递 |
| **全局状态滥用** | 状态混乱 | 组件状态优先、需要时再用 Pinia |
| **API 直接调用** | 代码重复 | 封装 API Service |
| **硬编码 URL** | 难以维护 | 使用路由名称或常量 |

---

## 信息来源

- [Spring Boot Security Best Practices](https://katyella.com/blog/spring-boot-security-best-practices/) — Spring Security 6.x 配置、JWT 实现
- [Vue 3 Dashboard Architecture](https://dev.to/ernest_litsa_6cbeed4e5669/how-to-architect-scalable-dashboards-in-vue-3-with-modular-components-50d1) — 模块化组件设计
- [JWT vs Session Comparison](https://medium.com/@bilalkazim/jwt-vs-session-based-authentication-when-to-choose-which-and-why-newer-better-df4581b91495) — 认证方案对比
- [Spring Boot Audit Trail](https://medium.com/@sarveshkhamkar321/audit-trail-in-spring-boot-jpa-auditing-hibernate-envers-with-examples-cb32bcc8fc32) — 审计实现方案

---

*架构研究完成：2026-04-02*
