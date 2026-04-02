# Phase 1: 认证与基础框架 - Research

**Researched:** 2026-04-02

## 1. 技术选型建议

### 1.1 JWT 认证

| 组件 | 选型 | 理由 |
|------|------|------|
| 后端 JWT 库 | jjwt 0.12.x | Spring Boot 生态主流选择 |
| 前端存储 | localStorage | 简单易用，配合路由守卫 |
| 密码加密 | BCrypt | Spring Security 标准方案 |
| 登录入口 | /api/auth/login | RESTful 设计 |

**依赖：**
- 后端：`jjwt-api`, `jjwt-impl`, `jjwt-jackson` (jjwt 0.12.x)
- 前端：`vue-router`, `axios`

### 1.2 前端框架

| 组件 | 选型 | 版本 |
|------|------|------|
| UI 组件库 | Element Plus | 2.x |
| 路由 | vue-router | 4.x |
| HTTP 客户端 | axios | 1.x |
| JWT 解析 | jwt-decode | 4.x |

### 1.3 后端安全

- 方案：手动实现 JWT 过滤器（不引入 Spring Security）
- 理由：简化依赖，保持轻量

## 2. 实现方案

### 2.1 后端模块结构

```
cn.coderstory.springboot/
├── config/
│   └── CorsConfig.java          # CORS 跨域配置
├── security/
│   ├── JwtTokenProvider.java    # JWT 生成/解析
│   ├── JwtAuthenticationFilter.java  # 请求拦截
│   └── PasswordEncoder.java    # BCrypt 加密
├── controller/
│   └── AuthController.java     # 登录/登出 API
├── entity/
│   ├── User.java                # 用户实体
│   └── AuditLog.java           # 审计日志实体
├── service/
│   ├── AuthService.java         # 认证逻辑
│   └── AuditService.java        # 审计记录
└── mapper/
    ├── UserMapper.java
    └── AuditLogMapper.java
```

### 2.2 前端模块结构

```
app-vue/src/
├── router/
│   └── index.js                 # 路由 + 守卫
├── api/
│   └── auth.js                  # 认证 API
├── store/
│   └── user.js                 # 用户状态
├── views/
│   ├── Login.vue               # 登录页
│   └── Layout.vue              # 管理后台布局
└── components/
    ├── AppMenu.vue             # 左侧菜单
    ├── AppTabs.vue             # 页签切换
    └── AppHeader.vue           # 顶部用户栏
```

### 2.3 数据库设计

**用户表 (sys_user)**
```sql
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100),
    phone VARCHAR(20),
    role_id BIGINT,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME
);
```

**审计日志表 (sys_audit_log)**
```sql
CREATE TABLE sys_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    username VARCHAR(50),
    operation VARCHAR(50),      -- 登录/登出/新增/编辑/删除
    target_type VARCHAR(50),    -- 目标对象类型
    target_id VARCHAR(100),     -- 目标对象ID
    ip_address VARCHAR(50),
    create_time DATETIME
);
```

## 3. 依赖分析

### 3.1 后端 Maven 依赖

```xml
<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```

### 3.2 前端 npm 依赖

```bash
npm install element-plus vue-router axios jwt-decode @element-plus/icons-vue
```

## 4. 常见陷阱

### 4.1 JWT 存储
- **陷阱**：XSS 攻击风险
- **方案**：设置合理的 token 过期时间，配合 HttpOnly cookie（可选）

### 4.2 CORS 跨域
- **陷阱**：前后端分离时跨域请求失败
- **方案**：后端配置 CorsConfig，允许前端域名

### 4.3 密码加密
- **陷阱**：使用弱加密算法
- **方案**：使用 BCrypt，强度因子 10-12

### 4.4 前端路由守卫
- **陷阱**：刷新页面丢失登录状态
- **方案**：在路由守卫中检查 localStorage 中的 token

### 4.5 Element Plus 按需引入
- **陷阱**：全量引入导致打包体积过大
- **方案**：使用 unplugin-vue-components 自动按需引入

## 5. 验证架构

### 5.1 功能验证

| 场景 | 验证方式 |
|------|----------|
| 用户登录 | POST /api/auth/login 成功返回 token |
| token 验证 | 携带有效 token 访问受保护接口成功 |
| 登出 | POST /api/auth/logout 清除 token |
| 未登录访问 | 跳转登录页 |
| 菜单显示 | 左侧菜单正确渲染 |
| 页签切换 | 多页面切换正常 |
| 审计记录 | 登录/登出生成审计日志 |

### 5.2 测试用例

- 后端：AuthServiceTest（登录成功/失败/无用户）
- 前端：登录页面单元测试（表单验证、提交逻辑）

---

*Research completed: 2026-04-02*