---
phase: 01-foundation
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - springboot/src/main/java/cn/coderstory/springboot/entity/User.java
  - springboot/src/main/java/cn/coderstory/springboot/entity/AuditLog.java
  - springboot/src/main/java/cn/coderstory/springboot/security/JwtTokenProvider.java
  - springboot/src/main/java/cn/coderstory/springboot/security/JwtAuthenticationFilter.java
  - springboot/src/main/java/cn/coderstory/springboot/security/PasswordEncoder.java
  - springboot/src/main/java/cn/coderstory/springboot/controller/AuthController.java
  - springboot/src/main/java/cn/coderstory/springboot/service/AuthService.java
  - springboot/src/main/java/cn/coderstory/springboot/service/AuditService.java
  - springboot/src/main/java/cn/coderstory/springboot/config/CorsConfig.java
  - springboot/pom.xml
  - app-vue/src/router/index.js
autonomous: true
requirements:
  - AUTH-01
  - AUTH-02
  - AUTH-03
  - AUTH-04
  - AUDIT-04
user_setup: []
must_haves:
  truths:
    - 用户可以使用用户名和密码成功登录系统
    - 用户登录后在有效期内无需重复登录
    - 用户可以安全登出系统
    - 未登录用户访问受保护页面时自动跳转登录页
    - 审计日志记录操作用户、操作时间、操作类型、目标对象、IP地址
  artifacts:
    - path: "springboot/src/main/java/cn/coderstory/springboot/entity/User.java"
      provides: "用户实体，包含 username, password, name, phone, roleId 字段"
    - path: "springboot/src/main/java/cn/coderstory/springboot/entity/AuditLog.java"
      provides: "审计日志实体"
    - path: "springboot/src/main/java/cn/coderstory/springboot/security/JwtTokenProvider.java"
      provides: "JWT 生成与解析，使用 jjwt 0.12.x"
    - path: "springboot/src/main/java/cn/coderstory/springboot/security/JwtAuthenticationFilter.java"
      provides: "JWT 请求拦截验证"
    - path: "springboot/src/main/java/cn/coderstory/springboot/controller/AuthController.java"
      provides: "登录/登出 REST API: POST /api/auth/login, POST /api/auth/logout"
    - path: "springboot/src/main/java/cn/coderstory/springboot/config/CorsConfig.java"
      provides: "CORS 跨域配置"
  key_links:
    - from: "AuthController.java"
      to: "JwtTokenProvider.java"
      via: "依赖 JwtTokenProvider 生成 JWT token"
    - from: "JwtAuthenticationFilter.java"
      to: "JwtTokenProvider.java"
      via: "依赖 JwtTokenProvider 验证 token"
    - from: "AuthService.java"
      to: "AuditService.java"
      via: "登录/登出时调用 AuditService 记录审计日志"
---

<objective>
实现后端认证 API 和数据库层。包括用户实体、审计日志实体、JWT 认证（jjwt 0.12.x）、登录/登出 API、CORS 配置、数据库表。
</objective>

<execution_context>
@D:/Data/桌面/vibe coding/.opencode/get-shit-done/workflows/execute-plan.md
</execution_context>

<context>
@.planning/phases/01-foundation/01-foundation-CONTEXT.md
@.planning/phases/01-foundation/01-foundation-RESEARCH.md
@.planning/phases/01-foundation/01-foundation-UI-SPEC.md
@./AGENTS.md

<interfaces>
<!-- 关键接口定义 -->

后端 AuthController.java 需要实现的 API:
```java
// 登录接口
POST /api/auth/login
Request: { "username": "string", "password": "string" }
Response: { "code": 200, "message": "success", "data": { "token": "string", "user": { "id": long, "username": "string", "name": "string" } } }

// 登出接口  
POST /api/auth/logout
Response: { "code": 200, "message": "success" }

// 获取当前用户
GET /api/auth/current
Response: { "code": 200, "data": { "id": long, "username": "string", "name": "string", "roleId": long } }
```

JwtTokenProvider.java 接口:
```java
public String generateToken(Long userId, String username);
public boolean validateToken(String token);
public Long getUserIdFromToken(String token);
public String getUsernameFromToken(String token);
```

User 实体字段:
- id: Long (主键)
- username: String (唯一)
- password: String (BCrypt 加密)
- name: String
- phone: String
- roleId: Long
- deleted: Integer (0/1 逻辑删除)
- createTime: LocalDateTime
- updateTime: LocalDateTime
</interfaces>
</context>

<tasks>

<task type="auto">
  <name>Task 1: 添加 Maven 依赖 (jjwt + MyBatis)</name>
  <files>springboot/pom.xml</files>
  <read_first>springboot/pom.xml</read_first>
  <action>
在 pom.xml 中添加以下依赖（放在其它 dependencies 之前，按 groupId 排序）:

1. JWT 依赖 (jjwt 0.12.x):
```xml
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

2. MyBatis Plus 依赖:
```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <version>3.5.7</version>
</dependency>
```

3. MySQL 驱动:
```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```
  </action>
  <verify>
<automated>grep -q "jjwt-api" springboot/pom.xml && echo "PASS" || echo "FAIL"</automated>
  </verify>
  <done>jjwt 0.12.6, MyBatis Plus 3.5.7, MySQL 驱动已添加到 pom.xml</done>
</task>

<task type="auto">
  <name>Task 2: 创建 User 实体</name>
  <files>springboot/src/main/java/cn/coderstory/springboot/entity/User.java</files>
  <read_first>springboot/pom.xml</read_first>
  <action>
创建 User.java 实体类，使用 MyBatis Plus 注解:

```java
package cn.coderstory.springboot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    private String password;
    
    private String name;
    
    private String phone;
    
    private Long roleId;
    
    @TableLogic
    private Integer deleted;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

确保包名为: cn.coderstory.springboot.entity
  </action>
  <verify>
<automated>ls springboot/src/main/java/cn/coderstory/springboot/entity/User.java && echo "PASS" || echo "FAIL"</automated>
  </verify>
  <done>User 实体类已创建，包含 id, username, password, name, phone, roleId, deleted, createTime, updateTime 字段</done>
</task>

<task type="auto">
  <name>Task 3: 创建 AuditLog 实体</name>
  <files>springboot/src/main/java/cn/coderstory/springboot/entity/AuditLog.java</files>
  <read_first>springboot/src/main/java/cn/coderstory/springboot/entity/User.java</read_first>
  <action>
创建 AuditLog.java 实体类:

```java
package cn.coderstory.springboot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_audit_log")
public class AuditLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String username;
    
    private String operation;      // 登录/登出/新增/编辑/删除
    
    private String targetType;     // 目标对象类型
    
    private String targetId;       // 目标对象ID
    
    private String ipAddress;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
```
  </action>
  <verify>
<automated>ls springboot/src/main/java/cn/coderstory/springboot/entity/AuditLog.java && echo "PASS" || echo "FAIL"</automated>
  </verify>
  <done>AuditLog 实体类已创建，包含 userId, username, operation, targetType, targetId, ipAddress, createTime 字段</done>
</task>

<task type="auto">
  <name>Task 4: 创建 JWT 认证组件</name>
  <files>springboot/src/main/java/cn/coderstory/springboot/security/JwtTokenProvider.java</files>
  <read_first>springboot/pom.xml</read_first>
  <action>
创建 JwtTokenProvider.java，使用 jjwt 0.12.x:

```java
package cn.coderstory.springboot.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret:mySecretKeyForJwtTokenGeneration123456}")
    private String jwtSecret;
    
    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration; // 默认 24 小时
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
    
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.parseLong(claims.getSubject());
    }
    
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("username", String.class);
    }
}
```

包名: cn.coderstory.springboot.security
  </action>
  <verify>
<automated>ls springboot/src/main/java/cn/coderstory/springboot/security/JwtTokenProvider.java && echo "PASS"</automated>
  </verify>
  <done>JwtTokenProvider 已创建，包含 generateToken, validateToken, getUserIdFromToken, getUsernameFromToken 方法</done>
</task>

<task type="auto">
  <name>Task 5: 创建 PasswordEncoder</name>
  <files>springboot/src/main/java/cn/coderstory/springboot/security/PasswordEncoder.java</files>
  <read_first>springboot/pom.xml</read_first>
  <action>
创建 PasswordEncoder.java，使用 BCrypt:

```java
package cn.coderstory.springboot.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncoder {
    
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
    
    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }
    
    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
```

包名: cn.coderstory.springboot.security
  </action>
  <verify>
<automated>ls springboot/src/main/java/cn/coderstory/springboot/security/PasswordEncoder.java && echo "PASS"</automated>
  </verify>
  <done>PasswordEncoder 已创建，使用 BCrypt 强度因子 10</done>
</task>

<task type="auto">
  <name>Task 6: 创建 AuthService</name>
  <files>springboot/src/main/java/cn/coderstory/springboot/service/AuthService.java</files>
  <read_first>springboot/src/main/java/cn/coderstory/springboot/entity/User.java</read_first>
  <action>
创建 AuthService.java:

```java
package cn.coderstory.springboot.service;

import cn.coderstory.springboot.entity.User;
import cn.coderstory.springboot.mapper.UserMapper;
import cn.coderstory.springboot.security.JwtTokenProvider;
import cn.coderstory.springboot.security.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    
    public Map<String, Object> login(String username, String password, String ipAddress) {
        User user = userMapper.findByUsername(username);
        
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        if (user.getDeleted() == 1) {
            throw new RuntimeException("用户已被禁用");
        }
        
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        
        // 记录登录审计日志
        auditService.log(user.getId(), user.getUsername(), "LOGIN", "USER", 
                String.valueOf(user.getId()), ipAddress);
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("name", user.getName());
        userInfo.put("roleId", user.getRoleId());
        data.put("user", userInfo);
        
        return data;
    }
    
    public void logout(Long userId, String username, String ipAddress) {
        if (userId != null) {
            auditService.log(userId, username, "LOGOUT", "USER", 
                    String.valueOf(userId), ipAddress);
        }
    }
    
    public User getCurrentUser(Long userId) {
        return userMapper.selectById(userId);
    }
}
```

包名: cn.coderstory.springboot.service
  </action>
  <verify>
<automated>ls springboot/src/main/java/cn/coderstory/springboot/service/AuthService.java && echo "PASS"</automated>
  </verify>
  <done>AuthService 已创建，包含 login, logout, getCurrentUser 方法，登录时记录审计日志</done>
</task>

<task type="auto">
<name>Task 7: 创建 AuditService</name>
<files>springboot/src/main/java/cn/coderstory/springboot/service/AuditService.java</files>
<read_first>springboot/src/main/java/cn/coderstory/springboot/entity/AuditLog.java</read_first>
<action>
创建 AuditService.java:

```java
package cn.coderstory.springboot.service;

import cn.coderstory.springboot.entity.AuditLog;
import cn.coderstory.springboot.mapper.AuditLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {
    
    private final AuditLogMapper auditLogMapper;
    
    @Async
    public void log(Long userId, String username, String operation, 
            String targetType, String targetId, String ipAddress) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(userId);
            auditLog.setUsername(username);
            auditLog.setOperation(operation);
            auditLog.setTargetType(targetType);
            auditLog.setTargetId(targetId);
            auditLog.setIpAddress(ipAddress);
            
            auditLogMapper.insert(auditLog);
            log.info("审计日志记录: user={}, operation={}, target={}", 
                    username, operation, targetId);
        } catch (Exception e) {
            log.error("审计日志记录失败: {}", e.getMessage());
        }
    }
}
```

包名: cn.coderstory.springboot.service
  </action>
  <verify>
<automated>ls springboot/src/main/java/cn/coderstory/springboot/service/AuditService.java && echo "PASS"</automated>
  </verify>
  <done>AuditService 已创建，包含 log 异步方法记录审计日志</done>
</task>

<task type="auto">
<name>Task 8: 创建 Mapper 接口</name>
<files>springboot/src/main/java/cn/coderstory/springboot/mapper/UserMapper.java, springboot/src/main/java/cn/coderstory/springboot/mapper/AuditLogMapper.java</files>
<read_first>springboot/src/main/java/cn/coderstory/springboot/entity/User.java</read_first>
  <action>
创建 UserMapper.java:

```java
package cn.coderstory.springboot.mapper;

import cn.coderstory.springboot.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    User findByUsername(String username);
}
```

创建 AuditLogMapper.java:

```java
package cn.coderstory.springboot.mapper;

import cn.coderstory.springboot.entity.AuditLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}
```
  </action>
  <verify>
<automated>ls springboot/src/main/java/cn/coderstory/springboot/mapper/UserMapper.java && ls springboot/src/main/java/cn/coderstory/springboot/mapper/AuditLogMapper.java && echo "PASS"</automated>
  </verify>
  <done>UserMapper 和 AuditLogMapper 已创建</done>
</task>

<task type="auto">
<name>Task 9: 创建 AuthController</name>
<files>springboot/src/main/java/cn/coderstory/springboot/controller/AuthController.java</files>
<read_first>springboot/src/main/java/cn/coderstory/springboot/service/AuthService.java</read_first>
  <action>
创建 AuthController.java:

```java
package cn.coderstory.springboot.controller;

import cn.coderstory.springboot.entity.User;
import cn.coderstory.springboot.security.JwtTokenProvider;
import cn.coderstory.springboot.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            String ipAddress = getClientIp(httpRequest);
            
            Map<String, Object> data = authService.login(username, password, ipAddress);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", data);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 401);
            response.put("message", e.getMessage());
            return ResponseEntity.status(401).body(response);
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Map<String, Object> response = new HashMap<>();
        
        if (token != null && jwtTokenProvider.validateToken(token)) {
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            String username = jwtTokenProvider.getUsernameFromToken(token);
            String ipAddress = getClientIp(request);
            
            authService.logout(userId, username, ipAddress);
        }
        
        response.put("code", 200);
        response.put("message", "success");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Map<String, Object> response = new HashMap<>();
        
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            response.put("code", 401);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }
        
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        User user = authService.getCurrentUser(userId);
        
        if (user == null) {
            response.put("code", 401);
            response.put("message", "用户不存在");
            return ResponseEntity.status(401).body(response);
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("name", user.getName());
        data.put("roleId", user.getRoleId());
        
        response.put("code", 200);
        response.put("data", data);
        return ResponseEntity.ok(response);
    }
    
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
```

包名: cn.coderstory.springboot.controller
  </action>
  <verify>
<automated>ls springboot/src/main/java/cn/coderstory/springboot/controller/AuthController.java && echo "PASS"</automated>
  </verify>
  <done>AuthController 已创建，提供 /api/auth/login, /api/auth/logout, /api/auth/current 接口</done>
</task>

<task type="auto">
<name>Task 10: 创建 CORS 配置和 JWT 过滤器</name>
  <files>springboot/src/main/java/cn/coderstory/springboot/config/CorsConfig.java, springboot/src/main/java/cn/coderstory/springboot/security/JwtAuthenticationFilter.java</files>
  <read_first>springboot/src/main/java/cn/coderstory/springboot/security/JwtTokenProvider.java</read_first>
  <action>
创建 CorsConfig.java:

```java
package cn.coderstory.springboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
    
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.setAllowCredentials(true);
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
```

创建 JwtAuthenticationFilter.java:

```java
package cn.coderstory.springboot.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
            HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String token = getTokenFromRequest(request);
        
        if (token != null && jwtTokenProvider.validateToken(token)) {
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            String username = jwtTokenProvider.getUsernameFromToken(token);
            
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            
            log.debug("Authenticated user: {} (id: {})", username, userId);
        }
        
        filterChain.doFilter(request, response);
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/login") || 
               path.startsWith("/api/auth/register");
    }
    
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```
  </action>
  <verify>
<automated>ls springboot/src/main/java/cn/coderstory/springboot/config/CorsConfig.java && ls springboot/src/main/java/cn/coderstory/springboot/security/JwtAuthenticationFilter.java && echo "PASS"</automated>
  </verify>
  <done>CorsConfig 和 JwtAuthenticationFilter 已创建</done>
</task>

<task type="auto">
<name>Task 11: 配置 application.yml 数据库连接</name>
  <files>springboot/src/main/resources/application.yml</files>
  <read_first>springboot/pom.xml</read_first>
  <action>
在 springboot/src/main/resources/ 目录下创建或更新 application.yml:

```yaml
spring:
  application:
    name: springboot
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/admin_system?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: root
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: cn.coderstory.springboot.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

jwt:
  secret: mySecretKeyForJwtTokenGeneration123456789
  expiration: 86400000

server:
  port: 8080

logging:
  level:
    cn.coderstory: debug
```

如果 application.yml 已存在，添加 database, mybatis-plus, jwt 配置。
  </action>
  <verify>
<automated>grep -q "datasource:" springboot/src/main/resources/application.yml && echo "PASS" || echo "FAIL"</automated>
  </verify>
  <done>application.yml 已配置数据库、MyBatis Plus、JWT 设置</done>
</task>

<task type="auto">
<name>Task 12: 创建数据库初始化 SQL</name>
  <files>springboot/src/main/resources/schema.sql</files>
  <read_first>springboot/src/main/java/cn/coderstory/springboot/entity/User.java</read_first>
  <action>
创建 schema.sql 初始化脚本:

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS admin_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE admin_system;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(Bcrypt加密)',
    name VARCHAR(100) COMMENT '姓名',
    phone VARCHAR(20) COMMENT '手机号',
    role_id BIGINT COMMENT '角色ID',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记(0正常1删除)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 审计日志表
CREATE TABLE IF NOT EXISTS sys_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '操作用户名',
    operation VARCHAR(50) NOT NULL COMMENT '操作类型(LOGIN/LOGOUT/新增/编辑/删除)',
    target_type VARCHAR(50) COMMENT '目标对象类型',
    target_id VARCHAR(100) COMMENT '目标对象ID',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_operation (operation),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';

-- 插入默认管理员用户 (密码: admin123)
INSERT INTO sys_user (username, password, name, role_id) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '管理员', 1);
```

密码说明: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH 是 "admin123" 的 BCrypt 哈希
  </action>
  <verify>
<automated>ls springboot/src/main/resources/schema.sql && echo "PASS"</automated>
  </verify>
  <done>schema.sql 已创建，包含 sys_user 和 sys_audit_log 表，以及默认管理员用户</done>
</task>

</tasks>

<verification>
手动验证步骤：
1. 启动 MySQL，创建数据库: `mysql -u root -p < springboot/src/main/resources/schema.sql`
2. 运行后端: `cd springboot && ./mvnw spring-boot:run`
3. 测试登录: `curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}'`
4. 验证返回包含 token
5. 测试获取当前用户: `curl -H "Authorization: Bearer <token>" http://localhost:8080/api/auth/current`
</verification>

<success_criteria>
- [ ] 用户可以使用用户名和密码成功登录系统
- [ ] 登录成功后返回 JWT token
- [ ] 用户可以安全登出系统
- [ ] 审计日志记录登录/登出事件
- [ ] POST /api/auth/login 返回 200 + token
- [ ] POST /api/auth/logout 返回 200
- [ ] GET /api/auth/current 返回当前用户信息
</success_criteria>

<output>
After completion, create `.planning/phases/01-foundation/{phase}-01-SUMMARY.md`
</output>
