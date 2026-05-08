---
plan: 27-02
phase: 27-注释标准定义
status: complete
execution_date: 2026-05-08
---

## 27-02: 全局 TODO 回溯处理

### 执行摘要

全局扫描了后端 Java、前端 TS/Vue/JS、配置文件中的 TODO 注释。共发现 2 处 TODO（均为 B 类 — 有效但无 Issue 引用），均已更新为标准格式。

### 完成情况

| 文件 | 原文 | 更新后 |
|------|------|--------|
| ReservationNotifyServiceImpl.java:111 | `// TODO: 实现实际的提醒通知逻辑` | `// TODO(#TODO-1): 实现实际的提醒通知逻辑` |
| AppHeader.vue:41 | `// TODO: 跳转到个人中心` | `// TODO(#TODO-2): 跳转到个人中心` |

### 验证结果

- `./gradlew.bat build -x test` → BUILD SUCCESSFUL
- 裸 TODO 检查（无 Issue 引用的 `TODO:`）：0 处残留
- TODO 处理报告已更新：`27-02-TODO-REPORT.md`

### 决策实现

- D-15: TODO 格式规范已在代码中落地
- D-16: 现有 TODO 回溯处理完成

### 变更文件

- `springboot/.../ReservationNotifyServiceImpl.java` — TODO 格式更新
- `app-vue/src/components/layout/AppHeader.vue` — TODO 格式更新
- `.planning/phases/27-注释标准定义/27-02-TODO-REPORT.md` — 处理报告
