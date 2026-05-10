---
phase: 31
slug: 后端采集服务-REST-API
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-05-10
---

# Phase 31 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 5 + Mockito + Spring Boot Test |
| **Config file** | Spring Boot 默认（build.gradle.kts test 配置） |
| **Quick run command** | `cd springboot && ./gradlew.bat test --tests "*HardwareMonitorControllerTest" 2>&1 \| tail -20` |
| **Full suite command** | `cd springboot && ./gradlew.bat test -x check 2>&1 \| tail -20` |
| **Estimated runtime** | ~60s |

---

## Sampling Rate

- **Every task commit:** Run quick command for affected module
- **Every plan wave:** Run full suite
- **Before `/gsd-verify-work`:** Full suite green
- **Max feedback latency:** 120s

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Test Type | Automated Command |
|---------|------|------|-------------|-----------|-------------------|
| 31-01-01 | 01 | 1 | HWM-01 | compile | `cd springboot && ./gradlew.bat build -x test -x check 2>&1 \| tail -20` |
| 31-01-02 | 01 | 1 | HWM-08, TDD-01 | compile | `cd springboot && ./gradlew.bat build -x test -x check 2>&1 \| tail -20` |
| 31-02-01 | 02 | 2 | TDD-03 | test | `cd springboot && ./gradlew.bat test --tests "*RingBufferTest" 2>&1 \| tail -20` |
| 31-02-02 | 02 | 2 | HWM-02~06, HWM-09, TDD-01 | test | `cd springboot && ./gradlew.bat test --tests "*HardwareMetricsServiceImplTest" 2>&1 \| tail -20` |
| 31-03-01 | 03 | 2 | HWM-07, HWM-08, HWM-09 | test | `cd springboot && ./gradlew.bat test --tests "*HardwareMonitorControllerTest" 2>&1 \| tail -20` |
| 31-03-02 | 03 | 2 | TDD-01 | integration | `cd springboot && ./gradlew.bat test --tests "*HardwareMetricsServiceIT" 2>&1 \| tail -20` |

---

## Wave 0 Requirements

Existing infrastructure covers all phase requirements. No Wave 0 needed.

---

## Manual-Only Verifications

All phase behaviors have automated verification.

---

## Validation Sign-Off

- [x] All tasks have `<automated>` verify
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all MISSING references
- [x] No watch-mode flags
- [x] Feedback latency < 120s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
