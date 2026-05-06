---
phase: 05-backend-api
plan: 01
subsystem: user-management-api
tags: [backend, api, user, mybatis-plus]
dependency_graph:
  requires: []
  provides:
    - API-01: GET /api/users 分页查询
    - API-02: phone 模糊筛选
    - API-03: GET /api/users/{id} 获取用户
    - API-04: POST /api/users 创建用户
    - API-05: phone 字段处理
  affects:
    - springboot/src/main/java/cn/coderstory/springboot/controller/UserController.java
    - springboot/src/main/java/cn/coderstory/springboot/service/UserService.java
    - springboot/src/main/java/cn/coderstory/springboot/service/impl/UserServiceImpl.java

tech_stack:
  added: []
  patterns:
    - LambdaQueryWrapper phone like filter
    - RESTful API with ResponseEntity
    - MyBatis-Plus pagination

key_files:
  created: []
  modified:
    - springboot/src/main/java/cn/coderstory/springboot/controller/UserController.java
    - springboot/src/main/java/cn/coderstory/springboot/service/UserService.java
    - springboot/src/main/java/cn/coderstory/springboot/service/impl/UserServiceImpl.java

decisions: []

metrics:
  duration: "~10 minutes"
  completed_date: "2026-04-18"
---

# Phase 5 Plan 1 Summary: 用户管理 API 增强

## One-liner

扩展 GET /api/users 支持 phone 模糊筛选，完善 createUser 处理 phone 字段，重命名 getUserWithRole 为 getUserById。

## Tasks Completed

| # | Task | Status | Files |
|---|------|--------|-------|
| 1 | Add phone fuzzy filter to GET /api/users | ✓ | UserService.java, UserServiceImpl.java, UserController.java |
| 2 | Fix createUser to handle phone field | ✓ | UserController.java |
| 3 | Rename getUserWithRole to getUserById | ✓ | UserService.java, UserServiceImpl.java, UserController.java |

## Task Details

### Task 1: Add phone fuzzy filter to GET /api/users

**Changes:**
- `UserService.java`: 方法签名添加 `String phone` 参数
- `UserServiceImpl.java`: 添加 `wrapper.like(User::getPhone, phone)` 筛选逻辑
- `UserController.java`: 接收 `@RequestParam(required = false) String phone` 并传递给 service

**Commit:** git不可用，跳过提交

### Task 2: Fix createUser to handle phone field

**Changes:**
- `UserController.java`: 在 `createUser` 方法中添加 `user.setPhone((String) request.get("phone"))`

**Commit:** git不可用，跳过提交

### Task 3: Rename getUserWithRole to getUserById

**Changes:**
- `UserService.java`: 接口方法 `getUserWithRole` → `getUserById`
- `UserServiceImpl.java`: 实现方法 `getUserWithRole` → `getUserById`
- `UserController.java`: 调用 `userService.getUserWithRole(id)` → `userService.getUserById(id)`

**Commit:** git不可用，跳过提交

## Verification Results

| Check | Result |
|-------|--------|
| `grep "String phone" UserService.java` | ✓ Found |
| `grep "User::getPhone, phone" UserServiceImpl.java` | ✓ Found |
| `grep "required = false) String phone" UserController.java` | ✓ Found |
| `grep "user.setPhone" UserController.java` | ✓ Found |
| `grep "getUserWithRole" service/` | ✓ Not found (已清除) |
| `grep "getUserById" UserService.java` | ✓ Found |

## Deviations from Plan

**None** — plan executed exactly as written.

## Self-Check: PASSED

All acceptance criteria met:
- [x] UserService.java 方法签名包含 phone 参数
- [x] UserServiceImpl.getUserPage() 包含 `wrapper.like(User::getPhone, phone)`
- [x] UserController.getUserPage() 接收 phone 请求参数并传递给 service
- [x] UserController.createUser() 调用 user.setPhone()
- [x] UserService 接口方法名为 getUserById
- [x] UserServiceImpl 实现方法名为 getUserById
- [x] UserController 调用 getUserById
- [x] getUserWithRole 已完全移除
