---
phase: 33
slug: complete-charts-trends
status: approved
nyquist_compliant: true
wave_0_complete: true
created: 2026-05-10
---

# Phase 33 — 完整图表 + 趋势 — Validation Strategy

## Test Framework

| Framework | Language | Scope |
|-----------|----------|-------|
| Vitest | TypeScript | 前端 composable 单元测试 |
| vue-tsc | TypeScript | 前端类型检查 |
| npm run build | — | 前端构建验证 |

## Requirement-to-Test Mapping

| Requirement | Plan | Test Type | Command |
|-------------|------|-----------|---------|
| HWM-16: 磁盘 IO / 网络实时折线图 | 33-01 | Build | `cd app-vue && npm run build` |
| HWM-18: CPU/内存 1h 趋势折线图 | 33-01 | Build | `cd app-vue && npm run build` |
| HWM-20: 海滩风主题配色 | 33-01 | Build | `cd app-vue && npm run build` |
| HWM-22: ECharts 生命周期管理 | 33-01 | Build | `cd app-vue && npm run build` |
| TDD-04: 组件逻辑单元测试 | 33-01 | Unit | `cd app-vue && npx vitest run src/composables/__tests__/useHardwareTrend.test.ts` |

## Automated Tests

| Test | Verification Command |
|------|---------------------|
| useHardwareTrend (6 tests) | `cd app-vue && npx vitest run src/composables/__tests__/useHardwareTrend.test.ts` |
| useHardwareMetrics regression (7 tests) | `cd app-vue && npx vitest run src/composables/__tests__/useHardwareMetrics.test.ts` |
| TypeScript type check | `cd app-vue && npx vue-tsc --noEmit` |
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

**Approval:** approved (2026-05-10) — 6 composable tests pass, type check + build pass, code verified via git log
