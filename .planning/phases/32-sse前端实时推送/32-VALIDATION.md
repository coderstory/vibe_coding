---
phase: 32
slug: sse-realtime-push-frontend-pages
status: draft
created: 2026-05-10
---

# Phase 32 — SSE 实时推送 + 前端基础页面 — Validation Strategy

## Validation Architecture

### Test Framework

| Framework | Language | Scope |
|-----------|----------|-------|
| JUnit 5 + Mockito | Java | 后端 SSE 服务单元测试 |
| Vitest | TypeScript | 前端 composable 单元测试 |
| vue-tsc | TypeScript | 前端类型检查 |

### Requirement-to-Test Mapping

| Requirement | Plan | Test Type | Command |
|-------------|------|-----------|---------|
| HWM-10: SSE 端点推送 | 32-01 | Unit | `./gradlew.bat test --tests "*HardwareSseServiceTest"` |
| HWM-11: SSE 心跳/清理 | 32-01 | Unit | `./gradlew.bat test --tests "*HardwareSseServiceTest"` |
| HWM-12: 路由+菜单接入 | 32-03 | Type check | `npx vue-tsc --noEmit` |
| HWM-13: CPU 环形图 | 32-03 | Build | `cd app-vue && npm run build` |
| HWM-14: 内存环形图 | 32-03 | Build | `cd app-vue && npm run build` |
| HWM-15: 磁盘进度条 | 32-03 | Build | `cd app-vue && npm run build` |
| HWM-17: 系统信息卡片 | 32-03 | Build | `cd app-vue && npm run build` |
| HWM-19: SSE 连接管理 | 32-02 | Unit | `cd app-vue && npx vitest run` |
| TDD-02: SSE 可测试 | 32-01 | Unit | `./gradlew.bat test --tests "*HardwareSseServiceTest"` |
| TDD-05: Composable 可测试 | 32-02 | Unit | `cd app-vue && npx vitest run` |

### Wave 0: Nyquist Prerequisites

- [x] Phase 32 CONTEXT.md exists with 8 locked decisions (D-01..D-08)
- [x] Phase 32 RESEARCH.md exists with 5 patterns and validation architecture
- [x] Phase 32 UI-SPEC.md exists with visual/interaction contract
- [x] Phase 32 VALIDATION.md exists (this file)

### Wave 1: Unit Tests

| Task | Plan | Verification Command |
|------|------|---------------------|
| HardwareSseService | 32-01 T1, T2, T3 | `./gradlew.bat test --tests "*HardwareSseServiceTest"` |
| useHardwareMetrics composable | 32-02 T2, T3 | `cd app-vue && npx vitest run` |

### Wave 2: Integration and Build

| Task | Plan | Verification Command |
|------|------|---------------------|
| Flyway migration | 32-03 T4 | `cd springboot && ./gradlew.bat flywayMigrate -i` |
| TypeScript type check | 32-03 | `cd app-vue && npx vue-tsc --noEmit` |
| Frontend build | 32-03 | `cd app-vue && npm run build` |
