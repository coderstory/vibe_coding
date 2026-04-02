---
name: security-checklist
description: >-
  安全审计检查清单。当进行代码安全审查、实现认证授权、处理敏感数据时使用。
---

# 安全审计检查清单

本技能提供 RobitCode 项目的安全检查标准，帮助开发者在编码和审查时发现潜在安全问题。

---

## 一、认证与授权

### 1.1 JWT Token 安全

| 检查项 | 要求 | 风险等级 |
|--------|------|----------|
| 密钥强度 | 至少 256 位，使用安全随机生成 | 🔴 高 |
| Token 过期时间 | 访问 Token ≤ 2小时，刷新 Token ≤ 7天 | 🟡 中 |
| Token 存储 | 前端使用 httpOnly cookie 或内存存储 | 🟡 中 |
| Token 刷新 | 刷新时验证旧 Token 有效性 | 🟡 中 |
| Token 黑名单 | 注销时立即加入黑名单 | 🔴 高 |

### 1.2 密码安全

| 检查项 | 要求 | 风险等级 |
|--------|------|----------|
| 加密算法 | 使用 BCrypt（强度 ≥ 10） | 🔴 高 |
| 密码强度 | 至少 8 位，包含大小写字母和数字 | 🟡 中 |
| 密码传输 | 必须使用 HTTPS | 🔴 高 |
| 密码重置 | 验证旧密码或使用临时 Token | 🔴 高 |
| 登录失败限制 | 5 次失败后锁定账户 15 分钟 | 🟡 中 |

### 1.3 权限控制

```java
// ✅ 正确：使用注解控制权限
@PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
public UserVO getUser(Long id) { ... }

// ❌ 错误：仅在前端隐藏按钮
// 后端接口没有权限检查
```

---

## 二、输入验证

### 2.1 基本验证规则

| 输入类型 | 验证规则 | 示例 |
|----------|----------|------|
| 字符串 | 长度限制、字符白名单 | 用户名：4-20位字母数字 |
| 数字 | 范围限制、类型检查 | 年龄：0-150 |
| 日期 | 格式验证、范围检查 | 出生日期：不能是未来日期 |
| 文件 | 类型、大小、内容检查 | 图片：仅 jpg/png，≤ 5MB |

### 2.2 SQL 注入防护

```java
// ✅ 正确：使用参数化查询
@Select("SELECT * FROM users WHERE username = #{username}")
User findByUsername(String username);

// ❌ 错误：字符串拼接
@Select("SELECT * FROM users WHERE username = '" + username + "'")
User findByUsername(String username);
```

### 2.3 XSS 防护

```java
// ✅ 正确：转义 HTML 特殊字符
public String sanitize(String input) {
    return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
}

// 前端使用 v-text 而非 v-html
<span v-text="userInput"></span>
```

### 2.4 文件上传安全

| 检查项 | 要求 |
|--------|------|
| 文件类型 | 检查扩展名和 MIME 类型 |
| 文件内容 | 验证文件头魔术数字 |
| 文件大小 | 限制最大文件大小 |
| 存储路径 | 不使用用户提供的文件名 |
| 执行权限 | 上传目录禁止执行权限 |

---

## 三、敏感数据处理

### 3.1 日志脱敏

```java
// ✅ 正确：敏感数据脱敏
log.info("用户登录: username={}, ip={}", maskUsername(username), ip);

// ❌ 错误：记录明文密码
log.info("用户登录: username={}, password={}", username, password);
```

### 3.2 响应脱敏

| 数据类型 | 脱敏规则 |
|----------|----------|
| 手机号 | 保留前 3 后 4 位：138****1234 |
| 身份证 | 保留前 3 后 4 位：310***********1234 |
| 邮箱 | 保留前 2 后缀：zh***@example.com |
| 银行卡 | 保留后 4 位：************1234 |
| 密码 | 不返回 |

### 3.3 数据库加密

| 字段类型 | 加密方式 |
|----------|----------|
| 密码 | BCrypt 哈希 |
| 身份证 | AES 对称加密 |
| 银行卡 | AES 对称加密 |
| 敏感配置 | 加密存储或使用密钥管理服务 |

---

## 四、API 安全

### 4.1 接口防护

| 检查项 | 要求 | 风险等级 |
|--------|------|----------|
| HTTPS | 生产环境强制 HTTPS | 🔴 高 |
| CORS | 配置具体的允许域名 | 🔴 高 |
| 请求频率限制 | 实现接口限流 | 🟡 中 |
| 请求体大小限制 | 限制请求体大小 | 🟡 中 |
| 超时设置 | 设置合理的连接和读取超时 | 🟡 中 |

### 4.2 白名单配置

```java
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
            // 静态资源
            .requestMatchers("/static/**").permitAll()
            // 认证接口
            .requestMatchers("/api/auth/login", "/api/auth/refresh").permitAll()
            // API 文档
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
            // 健康检查
            .requestMatchers("/actuator/health").permitAll()
            // 其他接口需要认证
            .anyRequest().authenticated()
        );
        return http.build();
    }
}
```

### 4.3 敏感接口保护

```java
// 敏感操作需要二次验证
@PostMapping("/password/change")
public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
    // 1. 验证当前密码
    if (!userService.verifyPassword(userId, dto.getOldPassword())) {
        throw new BusinessException("PASSWORD_WRONG", "当前密码错误");
    }

    // 2. 检查密码强度
    if (!PasswordUtil.isStrong(dto.getNewPassword())) {
        throw new BusinessException("PASSWORD_WEAK", "密码强度不足");
    }

    // 3. 更新密码
    userService.updatePassword(userId, dto.getNewPassword());

    // 4. 强制重新登录
    tokenBlacklistService.addToBlacklist(currentToken);

    return Result.success();
}
```

---

## 五、依赖安全

### 5.1 依赖检查

| 检查项 | 工具 | 频率 |
|--------|------|------|
| 已知漏洞 | OWASP Dependency-Check | 每次构建 |
| 过时依赖 | Maven Versions Plugin | 每周 |
| 许可证合规 | License Maven Plugin | 新增依赖时 |

### 5.2 Maven 配置

```xml
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>8.2.1</version>
    <configuration>
        <failBuildOnCVSS>7</failBuildOnCVSS>
    </configuration>
</plugin>
```

---

## 六、安全检查清单

### 6.1 代码审查清单

- [ ] 所有接口都有权限控制
- [ ] 所有用户输入都有验证
- [ ] 敏感数据已脱敏或加密
- [ ] 没有硬编码的密钥或密码
- [ ] 没有记录敏感信息到日志
- [ ] 文件上传有安全检查
- [ ] SQL 使用参数化查询
- [ ] XSS 风险已处理

### 6.2 配置审查清单

- [ ] JWT 密钥足够强壮且安全存储
- [ ] 数据库密码不是默认值
- [ ] CORS 配置了具体域名
- [ ] 生产环境强制 HTTPS
- [ ] 错误页面不暴露堆栈信息
- [ ] 敏感配置使用环境变量

### 6.3 部署审查清单

- [ ] 服务器防火墙已配置
- [ ] 数据库不暴露公网
- [ ] 定期备份数据
- [ ] 日志有访问控制
- [ ] 监控告警已配置
- [ ] 灾难恢复计划已制定

---

## 七、常见安全漏洞

| 漏洞类型 | 防护措施 |
|----------|----------|
| SQL 注入 | 参数化查询、输入验证 |
| XSS | 输出编码、CSP 策略 |
| CSRF | CSRF Token、SameSite Cookie |
| 敏感数据泄露 | 加密存储、传输加密、访问控制 |
| 越权访问 | 权限检查、数据隔离 |
| 暴力破解 | 登录限制、验证码 |
| 文件上传漏洞 | 类型检查、内容检查、隔离存储 |

---

## 八、安全响应

### 8.1 漏洞等级

| 等级 | 定义 | 响应时间 |
|------|------|----------|
| 🔴 严重 | 可获取系统权限或大量敏感数据 | 24 小时内修复 |
| 🟠 高危 | 可获取部分敏感数据 | 3 天内修复 |
| 🟡 中危 | 可能造成数据泄露 | 1 周内修复 |
| 🟢 低危 | 影响有限 | 下个版本修复 |

### 8.2 应急响应流程

1. **发现**：接收安全报告或监控告警
2. **评估**：确认漏洞等级和影响范围
3. **修复**：开发修复补丁
4. **测试**：验证修复效果
5. **部署**：发布修复版本
6. **复盘**：分析原因，改进流程
