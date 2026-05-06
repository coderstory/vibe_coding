---
gsd_state_version: 1.0
milestone: v1.5
milestone_name: 前后端代码重构与目录整理
status: roadmap-ready
last_updated: "2026-05-06"
last_activity: 2026-05-06
progress:
  total_phases: 5
  completed_phases: 0
  total_plans: 0
  completed_plans: 0
  percent: 0
---

## Project Reference

See: .planning/PROJECT.md (updated 2026-05-06)

**Core value:** 提供清晰、高效的企业级管理后台界面，通过夏日海滩风主题营造清爽专业的视觉体验。
**Current focus:** v1.5 前后端代码重构与目录整理 — 建立代码质量工具链，按业务域重组前后端代码结构

## Current Position

Phase: 17 of 21 (基础设施搭建)
Plan: 0 of TBD
Status: Ready to plan
Last activity: 2026-05-06 — v1.5 roadmap created with 5 phases and 31 requirements

Progress: [░░░░░░░░░░] 0%

## Performance Metrics

**Velocity:**
- Total plans completed: 36 (across v1.0-v1.4)
- Average duration: N/A
- Total execution time: N/A

**By Phase:**

| Phase | Plans | Total | Avg/Plan |
|-------|-------|-------|----------|
| 1-16 (v1.0-v1.4) | 36 | completed | N/A |
| 17-21 (v1.5) | - | pending | - |

*Updated after each plan completion*

## Accumulated Context

### Decisions

Recent decisions affecting current work:

- **v1.5 scoping:** 按业务域垂直切分包结构，后端 shared + 10 个业务域，前端同步按域拆分
- **v1.5 tooling:** 集成 ArchUnit/Checkstyle/PMD/SpotBugs/JaCoCo/Error Prone 作为重构安全网
- **v1.5 constraints:** 不新增业务功能，不修改数据库 schema，仅重构不重写

Full decision log: .planning/PROJECT.md Key Decisions

### Pending Todos

None yet.

### Blockers/Concerns

- **Phase 18:** @MapperScan 通配符在包重组后能否自动覆盖新路径（需首个域迁移后立即验证）
- **Phase 20:** Vue Router 27 条懒加载路径在文件移动后可能断裂（每次移动后 npm run build 验证）
- **Phase 18:** MyBatis XML namespace 需与移动后的 Mapper 全限定名同步更新

## Deferred Items

| Category | Item | Status | Deferred At |
|----------|------|--------|-------------|
| v1.4 carryover | 敏感信息环境变量加固（JWT secret、DB 密码） | Deferred | 2026-05-06 |

## Session Continuity

Last session: 2026-05-06
Stopped at: v1.5 roadmap creation complete, ready for Phase 17 planning
Resume file: None

---

*STATE.md updated: 2026-05-06 — v1.5 roadmap initialized*
