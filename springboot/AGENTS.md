# springboot AGENTS.md

本目录为 Spring Boot 后端专用开发指南。请务必同时阅读根目录的 `../AGENTS.md` 了解全局规则。

---

## 模块概述

| 项目 | 版本 |
|------|------|
| Spring Boot | 4.0.5 |
| Java | 21 |
| MyBatis Plus | 3.5.x |
| Flyway | 数据库迁移 |
| JWT | 认证 |

---

## 开发命令

```powershell
# Windows
cd springboot
.\mvnw.cmd spring-boot:run

# 编译打包
mvn package

# 运行所有测试
mvn test

# 运行单个测试类
mvn test -Dtest=SpringbootApplicationTests

# 跳过测试打包
mvn package -DskipTests
```

---

## 技术栈

| 技术 | 用途 | 备注 |
|------|------|------|
| Spring Boot 4 | 核心框架 | Java 21 |
| MyBatis Plus | ORM | 使用 BaseMapper 简化 CRUD |
| Flyway | 数据库迁移 | 脚本位于 `src/main/resources/db/migration/` |
| Spring Security | 安全框架 | JWT 认证 |
| Lombok | 简化代码 | @Data, @Service 等注解 |

---

## 代码规范

### 类结构

```java
package cn.coderstory.springboot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findById(Long id) {
        log.info("查找用户: {}", id);
        return userRepository.findById(id)
            .orElseThrow(() -> new BusinessException("用户不存在"));
    }
}
```

### 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 类名 | PascalCase | `UserService` |
| 方法/变量 | camelCase | `getUserById` |
| 常量 | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| 包名 | 全小写 | `cn.coderstory.springboot` |
| Controller | `*Controller` | `UserController` |
| Service | `*Service` | `UserService` |
| Mapper | `*Mapper` | `UserMapper` |

---

## 目录结构

```
springboot/src/main/java/cn/coderstory/springboot/
├── controller/       # REST 控制器
│   ├── UserController.java
│   ├── AuthController.java
│   ├── MenuController.java
│   ├── RoleController.java
│   └── AuditLogController.java
├── service/         # 服务接口
│   ├── UserService.java
│   └── impl/        # 服务实现
│       └── UserServiceImpl.java
├── mapper/          # MyBatis Mapper（对应数据库表）
│   ├── UserMapper.java
│   ├── MenuMapper.java
│   └── ...
├── entity/          # 数据库实体
│   ├── User.java
│   ├── Menu.java
│   └── ...
├── exception/       # 异常处理
│   ├── BusinessException.java      # 业务异常
│   └── GlobalExceptionHandler.java  # 全局异常处理器
├── security/        # 安全相关
│   ├── JwtAuthenticationFilter.java
│   ├── JwtTokenProvider.java
│   └── PasswordEncoder.java
├── config/         # 配置类
│   ├── SecurityConfig.java
│   ├── CorsConfig.java
│   └── WebConfig.java
├── aspect/         # 切面
│   └── AuditAspect.java
├── util/           # 工具类
│   └── ZstdUtil.java
└── vo/             # 视图对象
    └── UserVO.java
```

---

## 数据库迁移（Flyway）

### 迁移脚本位置

```
src/main/resources/db/migration/
├── V1__init.sql                    # 初始化表结构
├── V2__knowledge_base.sql          # 知识库表
├── V3__fix_knowledge_menu_path.sql
├── V4__remove_theme_settings.sql
└── V5__user_management_menu.sql    # 用户管理菜单
```

### 命名规范

- 格式：`V{版本号}__{描述}.sql`
- 版本号使用整数，从 1 开始递增
- 描述使用下划线分隔单词
- 示例：`V1__init.sql`、`V2__add_user_phone.sql`

### 迁移规则

1. **每次数据库变更必须创建新迁移脚本**，禁止修改已执行的脚本
2. 迁移脚本必须具有幂等性（重复执行不会报错）
3. 使用 `CREATE TABLE IF NOT EXISTS`、`ALTER TABLE ADD COLUMN IF NOT EXISTS` 等安全语句

### 已有迁移脚本

| 版本 | 描述 |
|------|------|
| V1 | 初始化 sys_user, sys_audit_log, sys_menu, sys_role, sys_role_menu, sys_user_settings |
| V2 | 知识库相关表 |
| V3 | 修复菜单路径 |
| V4 | 移除主题设置 |
| V5 | 用户管理菜单数据 |

---

## 已有模式（来自代码探索）

### 全局异常处理

```java
// cn.coderstory.springboot.exception.BusinessException
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}

// cn.coderstory.springboot.exception.GlobalExceptionHandler
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<String> handleBusinessException(BusinessException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
```

### MyBatis Plus 使用

```java
// cn.coderstory.springboot.mapper.UserMapper
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 继承 BaseMapper 提供基本的 CRUD 方法
    // 自定义方法可以使用 XML 或 @Select 注解
}
```

### JWT 认证流程

```
JwtTokenProvider - 生成和验证 Token
JwtAuthenticationFilter - 拦截请求，提取和验证 JWT
SecurityConfig - 配置 Spring Security 策略
```

---

## 已识别的问题（反模式）

### ⚠️ 需修复项

1. **pom.xml 中硬编码数据库密码**
   - 位置：`pom.xml` 第 177-181 行附近
   - 问题：jdbc:mysql://localhost:3306/admin_system，user:root, password:123456
   - 建议：使用环境变量或 application.yml 中的配置

2. **V1__init.sql 中默认管理员密码提示**
   - 位置：`V1__init.sql` 第 44-46 行
   - 问题：注释中写明"密码: admin123"
   - 建议：移除明文密码注释

3. **Mapper 接口中直接写 SQL**（如果存在）
   - 位置：`mapper/UserMapper.java` 等
   - 问题：违反"SQL 写在 mapper.xml 中"的规范
   - 建议：将 SQL 移至 `resources/mapper/*.xml`

---

## 相关文件

- `../AGENTS.md` - 全局项目规范
- `../.planning/` - GSD 工作流规划目录
- `./pom.xml` - Maven 依赖配置
- `./src/main/resources/application.yaml` - Spring 配置
- `./src/main/resources/db/migration/` - Flyway 迁移脚本

---

*最后更新：2026-04-18*
