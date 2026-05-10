---
phase: 34
slug: security-hardening-polish
status: approved
nyquist_compliant: true
wave_0_complete: true
created: 2026-05-10
---

# Phase 34 — 安全加固 + 打磨 — Validation Strategy

## Test Framework

| Framework | Language | Scope |
|-----------|----------|-------|
| JUnit 5 + Mockito | Java | 后端 JWT 权限单元测试 |
| Vitest | TypeScript | 前端 composable 单元测试 |
| vue-tsc | TypeScript | 前端类型检查 |
| npm run build | — | 前端构建验证 |

## Requirement-to-Test Mapping

| Requirement | Plan | Test Type | Command |
|-------------|------|-----------|---------|
| HWM-21: ADMIN 角色权限 | 34-01 | Unit | `./gradlew.bat test --tests "*JwtTokenProviderTest"` |
| HWM-23: 骨架/空/错误状态 | 34-01 | Build | `cd app-vue && npm run build` |
| HWM-24: SSE 断连重连提示 | 34-01 | Unit | `cd app-vue && npx vitest run` |

## Automated Tests

| Test | Verification Command |
|------|---------------------|
| JwtTokenProviderTest (5 tests) | `./gradlew.bat test --tests "*JwtTokenProviderTest"` |
| useHardwareMetrics (7 tests, 含重连) | `cd app-vue && npx vitest run src/composables/__tests__/useHardwareMetrics.test.ts` |
| Frontend build | `cd app-vue && npm run build` |

## Manual-Only Verifications

All phase behaviors have automated verification.

## Validation Sign-Off

- [x] All tasks have automated verify
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all MISSING references
- [x] No watch-mode flags
- [x] Feedback latency < 120s
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** approved (2026-05-10) — 5 JWT tests pass, 7 composable tests pass (含断连重连), build OK
