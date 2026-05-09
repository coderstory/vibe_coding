---
gsd_state_version: 1.0
milestone: v1.8
milestone_name: Windows 硬件负载监控
status: planning
last_updated: "2026-05-09T10:00:00.000Z"
last_activity: 2026-05-09
progress:
  total_phases: 4
  completed_phases: 0
  total_plans: 0
  completed_plans: 0
  percent: 0
---

## Project Reference

See: .planning/PROJECT.md (updated 2026-05-07)

**Core value:** 提供清晰、高效的企业级管理后台界面，通过夏日海滩风主题营造清爽专业的视觉体验。
**Current focus:** v1.8 Windows 硬件负载监控 — 规划中

## Current Position

Phase: 31 - 后端采集服务 + REST API
Plan: 待规划
Status: Not started

Progress: [                    ] 0%

## v1.8 Phase Structure

| Phase | Goal | Requirements | Status |
|-------|------|--------------|--------|
| 31 | 后端采集服务 + REST API | HWM-01~09, TDD-01, TDD-03 | Not started |
| 32 | SSE 实时推送 + 前端基础页面 | HWM-10~15, HWM-17, HWM-19, TDD-02, TDD-05 | Not started |
| 33 | 完整图表 + 趋势 | HWM-16, HWM-18, HWM-20, HWM-22, TDD-04 | Not started |
| 34 | 安全加固 + 打磨 | HWM-21, HWM-23, HWM-24 | Not started |

## 技术决策

| 决策 | 依据 | 状态 |
|------|------|------|
| OSHI 7.x FFM（非 6.x JNA） | JDK 26 JEP 472 禁用 JNA | 待确认 |
| monitor/hardware/ 新包 | 不与 RocketMQ 监控耦合 | 待确认 |
| SSE 复用 SeckillSseService 模式 | 项目已有成熟 SSE 实现 | 待确认 |
| ECharts 6.x 复用 | 项目已有，无需新增依赖 | 待确认 |
| 接口式采集服务设计 | TDD 要求，OSHI 可 mock | 待确认 |

---

*STATE.md updated: 2026-05-09 — v1.8 初始化，4 个阶段待规划*
