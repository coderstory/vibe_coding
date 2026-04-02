# Phase 2: 用户与权限管理 - Research

**Researched:** 2026-04-02
**Domain:** RBAC权限管理系统 + Spring Boot后端 + Vue3前端
**Confidence:** HIGH

## Summary

Phase 2 implements a complete user-role-permission management system with audit logging. The core model is User→Role→Menu where users get permissions through role assignment, and permissions are page-level (not button-level). The existing User entity needs significant field additions, and new entities for Role and Menu are required. The existing AppMenu component needs dynamic permission filtering.

**Primary recommendation:** Use MyBatis-Plus for CRUD operations, implement a hierarchical menu permission model with a sys_menu table, and use Element Plus el-tree with show-checkbox for permission assignment dialogs.

---

## User Constraints (from CONTEXT.md)

### Locked Decisions

- **D-01:** 用户表字段：用户名、密码、姓名、性别、头像、创建日期、是否启用、邮箱、部门、岗位
- **D-02:** 手机号字段不在用户表中
- **D-03:** 页面级权限（不是按钮级权限）
- **D-04:** 用户分配角色，角色分配菜单访问权限
- **D-05:** 树形菜单结构，勾选分配权限
- **D-06:** 管理员直接输入新密码（不是随机生成或默认密码）
- **D-07:** 核心4字段：操作人、时间、操作类型、目标
- **D-08:** 审计日志查询支持：时间范围 + 操作人 + 操作类型

### OpenCode's Discretion
- 列表分页大小、排序规则等细节由 OpenCode 决定
- 前端表格组件具体实现由 OpenCode 决定（遵循 Element Plus 规范）

### Deferred Ideas (OUT OF SCOPE)
None — discussion stayed within phase scope

---

## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| USER-01 | 管理员可以查看用户列表（分页、搜索） | UserMapper + UserService with pagination |
| USER-02 | 管理员可以新增用户（用户名、密码、姓名、手机号、角色） | Note: D-02 says no phone field, use gender/avatar/department/position instead |
| USER-03 | 管理员可以编辑用户信息 | UserService.updateUser() |
| USER-04 | 管理员可以删除用户（逻辑删除） | MyBatis-Plus @TableLogic |
| USER-05 | 管理员可以重置用户密码 | PasswordEncoder.encode() + UserMapper.updatePassword() |
| ROLE-01 | 管理员可以查看角色列表 | RoleMapper + RoleService |
| ROLE-02 | 管理员可以新增角色（角色名、描述、权限） | Role + RoleMenuPermission |
| ROLE-03 | 管理员可以编辑角色 | RoleService.update() |
| ROLE-04 | 管理员可以删除角色 | RoleService.delete() with @TableLogic |
| MENU-02 | 用户只能看到有权限访问的菜单项 | AppMenu filtering based on user role menus |
| AUDIT-01 | 系统自动记录用户登录/登出事件 | AuditService.log() already exists in AuthService |
| AUDIT-02 | 系统自动记录关键数据操作（新增/编辑/删除） | AOP-based audit interceptor |
| AUDIT-03 | 管理员可以查询审计日志（按用户、操作类型、时间范围） | AuditLogMapper with date range query |
| UI-03 | 列表页面统一使用表格组件，支持分页 | Element Plus el-table + el-pagination |
| UI-04 | 表单页面统一使用 Element Plus 表单组件 | Element Plus el-form |
| UI-05 | 操作反馈使用 Message 消息提示 | ElMessage.success/error |

---

## Standard Stack

### Backend (Spring Boot 4.0.5 + Java 21)

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| MyBatis-Plus | 3.5.16 | ORM + CRUD | Already in pom.xml, @TableLogic for soft delete |
| Spring Security | Boot 4.0.5 | BCrypt密码编码 | Already in pom.xml |
| jjwt | 0.12.6 | JWT token | Already in pom.xml |
| Lombok | Boot 4.0.5 | 减少样板代码 | Already in pom.xml |

### Frontend (Vue 3.5 + Element Plus 2.9)

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Element Plus | 2.9.0 | UI组件库 | Already in package.json |
| @element-plus/icons-vue | 2.3.1 | 图标 | Already in package.json |
| axios | 1.7.9 | HTTP客户端 | Already in package.json |
| vue-router | 4.5.0 | 路由 | Already in package.json |

### Additional Dependencies Needed

```bash
# Backend - No new dependencies needed
# MyBatis-Plus, Spring Security already in pom.xml

# Frontend - No new dependencies needed  
# Element Plus already in package.json
```

---

## Architecture Patterns

### Recommended Project Structure

```
springboot/src/main/java/cn/coderstory/springboot/
├── entity/
│   ├── User.java              # 需扩展：D-01字段
│   ├── Role.java              # 新增
│   ├── Menu.java              # 新增（sys_menu表）
│   ├── RoleMenuPermission.java # 新增（sys_role_menu表）
│   └── AuditLog.java          # 已存在
├── mapper/
│   ├── UserMapper.java        # 需扩展
│   ├── RoleMapper.java        # 新增
│   ├── MenuMapper.java        # 新增
│   └── AuditLogMapper.java    # 已存在
├── service/
│   ├── UserService.java       # 需扩展
│   ├── RoleService.java       # 新增
│   ├── MenuService.java       # 新增
│   └── AuditService.java      # 已存在
├── controller/
│   ├── UserController.java    # 新增
│   ├── RoleController.java    # 新增
│   ├── MenuController.java    # 新增（获取菜单树）
│   └── AuditLogController.java # 新增
└── aspect/
    └── AuditAspect.java       # 新增（AOP审计日志）

app-vue/src/
├── views/
│   ├── UserManagement.vue      # 新增
│   ├── RoleManagement.vue      # 新增
│   └── AuditLog.vue           # 已存在（需完善）
├── api/
│   ├── user.js                # 新增
│   ├── role.js                # 新增
│   └── audit.js               # 新增
└── components/
    └── AppMenu.vue            # 需改造：动态菜单权限过滤
```

### Database Schema (schema.sql additions)

```sql
-- 角色表
CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    description VARCHAR(255) COMMENT '描述',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 菜单表（支持层级结构）
CREATE TABLE sys_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    path VARCHAR(255) COMMENT '路由路径',
    icon VARCHAR(50) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 角色-菜单权限关联表
CREATE TABLE sys_role_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 修改sys_user表（添加字段）
ALTER TABLE sys_user 
ADD COLUMN gender TINYINT COMMENT '性别(0女1男)' AFTER name,
ADD COLUMN avatar VARCHAR(255) COMMENT '头像URL' AFTER gender,
ADD COLUMN email VARCHAR(100) COMMENT '邮箱' AFTER role_id,
ADD COLUMN department VARCHAR(100) COMMENT '部门' AFTER email,
ADD COLUMN position VARCHAR(100) COMMENT '岗位' AFTER department,
ADD COLUMN enabled TINYINT DEFAULT 1 COMMENT '是否启用(0禁用1启用)' AFTER position;
```

### Permission Model Pattern

```
用户表 (sys_user)
├── id
├── username
├── role_id → 关联角色
└── ...

角色表 (sys_role)
├── id
├── role_name
├── role_code (唯一)
└── ...

菜单表 (sys_menu)
├── id
├── parent_id (自关联，层级结构)
├── name
├── path (路由路径)
└── ...

角色菜单关联表 (sys_role_menu)
├── role_id → 角色
└── menu_id → 菜单
```

### RBAC Permission Flow

```
1. 用户登录 → 获取用户信息 + role_id
2. 前端请求 /api/menus?roleId={roleId} → 返回用户有权限的菜单树
3. AppMenu 根据权限菜单动态渲染
4. 后端接口使用 @PreAuthorize 或拦截器检查菜单权限
```

---

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| 密码加密 | 手写BCrypt | Spring Security PasswordEncoder | 已集成，直接注入使用 |
| 软删除 | 手写deleted标志 | MyBatis-Plus @TableLogic | 自动处理，查询自动过滤已删除记录 |
| 分页查询 | 手写LIMIT | MyBatis-Plus IPage | 自动分页，类型安全 |
| 菜单树结构 | 手写递归 | 数据库parent_id自关联 + 递归查询 | 标准的层级数据模型 |
| 审计日志记录 | 每个方法手动调用 | AOP切面 | 统一处理，减少重复代码 |

**Key insight:** MyBatis-Plus @TableLogic 是软删除的标准实现，所有 SELECT 查询会自动加上 `deleted = 0` 条件。

---

## Common Pitfalls

### Pitfall 1: 权限过滤不完整
**What goes wrong:** 用户可以通过直接访问URL绕过菜单隐藏
**Why it happens:** 前端菜单过滤只是UI隐藏，后端没有权限校验
**How to avoid:** 后端使用拦截器/注解检查用户角色是否有权访问该菜单路径
**Warning signs:** 浏览器直接访问 `/dashboard/system/user` 应该返回403或重定向

### Pitfall 2: 审计日志遗漏关键操作
**What goes wrong:** 忘记在某些Service方法中记录审计日志
**Why it happens:** 手动调用auditService.log()容易遗漏
**How to avoid:** 使用AOP切面统一拦截@Service标注类的public方法，自动记录

### Pitfall 3: 菜单树递归性能问题
**What goes wrong:** 菜单层级深时递归查询N+1问题
**Why it happens:** 在Java代码中递归组装树结构
**How to avoid:** 使用单一SQL查询所有菜单，用Map+循环在Java中组装树（更可控），或使用递归CTE（MySQL 8.0+）

### Pitfall 4: 用户关联角色设计混淆
**What goes wrong:** 一个用户只能有一个角色
**Why it happens:** sys_user.role_id 是BIGINT不是数组
**How to avoid:** 确认业务需求 - 如果需要多角色，用户-角色改为中间表。当前设计是单角色，参考D-04"用户分配角色"

### Pitfall 5: 前端路由与后端路径不匹配
**What goes wrong:** 菜单路径与实际路由不匹配导致404
**Why it happens:** AppMenu.vue中的path需要与router定义一致
**How to avoid:** 统一管理菜单path与vue-router路径

---

## Code Examples

### 1. User Entity (需要扩展的字段)

```java
// Source: 基于现有 User.java
@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String name;
    
    // 新增字段 (D-01)
    private Integer gender;        // 性别: 0-女, 1-男
    private String avatar;         // 头像URL
    private String email;          // 邮箱
    private String department;     // 部门
    private String position;       // 岗位
    private Integer enabled;       // 是否启用: 0-禁用, 1-启用
    
    private Long roleId;           // 角色ID (保持现有字段)
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

### 2. Role Entity

```java
// Source: 新建
@Data
@TableName("sys_role")
public class Role {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String roleName;
    private String roleCode;
    private String description;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

### 3. Menu Entity

```java
// Source: 新建
@Data
@TableName("sys_menu")
public class Menu {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;       // 父菜单ID，0表示顶级
    private String name;         // 菜单名称
    private String path;         // 路由路径
    private String icon;         // 图标
    private Integer sortOrder;   // 排序
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
```

### 4. RoleMenuPermission Entity

```java
// Source: 新建
@Data
@TableName("sys_role_menu")
public class RoleMenuPermission {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long roleId;
    private Long menuId;
}
```

### 5. MyBatis-Plus 分页查询

```java
// Source: MyBatis-Plus官方文档
IPage<User> selectUserPage(Page<User> page, String username, String name, String department, Integer enabled) {
    return userMapper.selectPage(page, 
        new LambdaQueryWrapper<User>()
            .like(username != null, User::getUsername, username)
            .like(name != null, User::getName, name)
            .eq(department != null, User::getDepartment, department)
            .eq(enabled != null, User::getEnabled, enabled)
            .orderByDesc(User::getCreateTime)
    );
}
```

### 6. Element Plus el-tree 权限分配

```vue
<!-- Source: Element Plus官方文档 + UI-SPEC -->
<template>
  <el-dialog title="分配权限" v-model="dialogVisible" width="500px">
    <el-tree
      ref="menuTreeRef"
      :data="menuTreeData"
      :props="{ label: 'name', children: 'children' }"
      node-key="id"
      show-checkbox
      :default-expand-all="true"
      :default-checked-keys="checkedMenuIds"
    />
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSavePermissions">保存权限</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'

const menuTreeRef = ref()
const checkedMenuIds = ref([])

const handleSavePermissions = async () => {
  // 获取所有选中节点（包括半选状态的父节点）
  const checkedKeys = menuTreeRef.value.getCheckedKeys(false)  // false: 不包含半选
  const halfCheckedKeys = menuTreeRef.value.getHalfCheckedKeys() // 半选父节点
  const allSelected = [...checkedKeys, ...halfCheckedKeys]
  
  await saveRoleMenus(roleId.value, allSelected)
}
</script>
```

### 7. 审计日志AOP切面

```java
// Source: 新建
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {
    
    private final AuditService auditService;
    
    // 拦截所有Service层的public方法（排除审计相关方法避免循环）
    @Around("execution(* cn.coderstory.springboot.service..*(..)) && !execution(* cn.coderstory.springboot.service.AuditService.*(..))")
    public Object auditAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        // 获取操作类型（通过方法名推断）
        String operation = inferOperationType(methodName);
        
        // 执行原方法
        Object result = joinPoint.proceed();
        
        // 记录审计日志
        try {
            HttpServletRequest request = getHttpRequest();
            Long userId = getCurrentUserId(request);
            String username = getCurrentUsername(request);
            String targetType = className.replace("Service", "");
            
            auditService.log(userId, username, operation, targetType, 
                extractTargetId(result), getIpAddress(request));
        } catch (Exception e) {
            log.error("审计日志记录失败", e);
        }
        
        return result;
    }
    
    private String inferOperationType(String methodName) {
        if (methodName.startsWith("save") || methodName.startsWith("create") || methodName.startsWith("add")) {
            return "新增";
        } else if (methodName.startsWith("update") || methodName.startsWith("edit")) {
            return "编辑";
        } else if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
            return "删除";
        }
        return "操作";
    }
}
```

---

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| 按钮级权限 | 页面级权限 | D-03 (this phase) | 更简单，适合后台管理系统 |
| 硬编码菜单 | 动态菜单权限 | MENU-02 | 用户只能看到有权限的菜单 |
| 手动审计日志 | AOP自动审计 | AUDIT-02 | 不遗漏任何关键操作 |

**Deprecated/outdated:**
- 无

---

## Open Questions

1. **用户是否支持多角色？**
   - What we know: 当前schema是user.role_id (单BIGINT)，用户只能有一个角色
   - What's unclear: D-04说"用户分配角色"，未明确是否支持多角色
   - Recommendation: 先按单角色实现，复杂需求可在v2扩展为用户-角色中间表

2. **菜单路径是否需要唯一？**
   - What we know: Menu.path用于前端路由匹配
   - What's unclear: 是否需要保证path在同级菜单中唯一
   - Recommendation: path在同一父节点下应唯一，但不必全局唯一（不同模块可以有相同path但不同parent）

3. **审计日志是否需要记录操作详情（旧值/新值）？**
   - What we know: D-07只要求4字段（操作人、时间、操作类型、目标）
   - What's unclear: 是否需要记录变更前后的具体值
   - Recommendation: MVP只记录4字段，v2可扩展target_detail字段存储JSON格式的变更详情

4. **初始管理员角色如何创建？**
   - What we know: schema.sql中admin用户role_id=1，但没有创建role id=1的SQL
   - What's unclear: 初始数据是否需要在schema.sql中创建管理员角色和完整菜单
   - Recommendation: schema.sql添加初始角色和菜单数据

---

## Environment Availability

> Step 2.6: SKIPPED (no external dependencies identified beyond existing codebase)

Phase 2 is purely code/config changes with no external tool dependencies:
- Backend: Uses existing Spring Boot 4.0.5 + MyBatis-Plus 3.5.16 (already in pom.xml)
- Frontend: Uses existing Element Plus 2.9.0 (already in package.json)
- Database: MySQL already configured in existing schema.sql

---

## Validation Architecture

> Skip this section - workflow.nyquist_validation is explicitly set to false in config.json

---

## Sources

### Primary (HIGH confidence)
- Element Plus el-tree component documentation - Tree with checkbox mode for permission assignment
- MyBatis-Plus documentation - Pagination, @TableLogic, LambdaQueryWrapper patterns
- Existing codebase analysis - User.java, AuditLog.java, AuthService.java, pom.xml, package.json

### Secondary (MEDIUM confidence)
- Spring Security PasswordEncoder - Already in Spring Security starter
- AOP audit logging patterns - Standard Spring Boot practice

### Tertiary (LOW confidence)
- None

---

## Metadata

**Confidence breakdown:**
- Standard Stack: HIGH - All dependencies already in pom.xml/package.json
- Architecture: HIGH - RBAC pattern is standard, database schema well understood
- Pitfalls: MEDIUM - Permission filtering edge cases need validation

**Research date:** 2026-04-02
**Valid until:** 2026-05-02 (30 days, RBAC patterns are stable)
