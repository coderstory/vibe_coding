---
phase: 14-dependency-upgrade
plan: "01"
subsystem: infra
tags: [spring-boot, gradle, dependency-upgrade, mavenCentral]

# Dependency graph
requires: []
provides:
  - Spring Boot 4.1.0-RC1 upgrade complete
  - Updated libs.versions.toml with new version references
  - Updated build.gradle.kts with new plugin versions
  - MySQL connector upgraded to 9.7.0
affects: [15-config-migration, 16-build-verification]

# Tech tracking
tech-stack:
  added: []
  patterns: [version catalog management, BOM-managed dependencies]

key-files:
  created: []
  modified:
    - springboot/gradle/libs.versions.toml
    - springboot/build.gradle.kts

key-decisions:
  - "Reverted lombok to 1.18.44 (1.18.50 not available on mavenCentral)"
  - "Reverted spring-aop to 3.5.13 (4.1.0 causes dependency resolution failure)"
  - "Kept dependency-management at 1.1.7 (1.1.8 not available)"
  - "Removed explicit spring-boot-starter-aop version (BOM managed)"

patterns-established:
  - "BOM-managed dependencies for Spring Boot starters"

requirements-completed: [BOOT-01, BOOT-02, BOOT-03, DEPS-01, DEPS-02, DEPS-03, DEPS-04, DEPS-05, DEPS-06, DEPS-07]

# Metrics
duration: 5min
completed: 2026-04-30
---

# Phase 14 Plan 01 Summary

**Spring Boot 4.1.0-RC1 upgrade with dependency version updates, with auto-fixed compatibility issues**

## Performance

- **Duration:** 5 min
- **Started:** 2026-04-30T00:00:00Z
- **Completed:** 2026-04-30T00:05:00Z
- **Tasks:** 3
- **Files modified:** 2

## Accomplishments
- Spring Boot plugin upgraded to 4.1.0-RC1
- MySQL connector upgraded to 9.7.0
- Version catalog (libs.versions.toml) updated with new references
- Gradle dependency resolution successful with auto-fixed issues

## Task Commits

Each task was committed atomically:

1. **Task 1: Update libs.versions.toml with new versions** - `23977ca` (feat)
2. **Task 2: Update build.gradle.kts plugin and dependency versions** - `de8899e` (feat)
3. **Task 3: Verify dependency resolution with Gradle** - `fc9e4fd` (fix)

**Plan metadata:** (not applicable - orchestrator owns final commit)

## Files Created/Modified
- `springboot/gradle/libs.versions.toml` - Version catalog with spring-boot 4.1.0-RC1, mysql 9.7.0, lombok 1.18.44, spring-aop 3.5.13, dependency-management 1.1.7
- `springboot/build.gradle.kts` - Build config with Spring Boot 4.1.0-RC1 plugin, MySQL 9.7.0, BOM-managed spring-aop

## Decisions Made
- Reverted lombok to 1.18.44 (1.18.50 not available on mavenCentral)
- Reverted spring-aop to 3.5.13 (4.1.0 causes dependency resolution failure)
- Kept dependency-management at 1.1.7 (1.1.8 not available)
- Removed explicit version from spring-boot-starter-aop to let BOM manage it

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] lombok 1.18.50 not available on mavenCentral**
- **Found during:** Task 3 (Verify dependency resolution)
- **Issue:** Lombok 1.18.50 does not exist on mavenCentral, causing dependency resolution failure
- **Fix:** Reverted lombok version to 1.18.44 in both libs.versions.toml and build.gradle.kts
- **Files modified:** springboot/gradle/libs.versions.toml, springboot/build.gradle.kts
- **Verification:** Gradle dependencies command completed successfully with BUILD SUCCESSFUL
- **Committed in:** fc9e4fd (part of Task 3 commit)

**2. [Rule 3 - Blocking] spring-aop 4.1.0 causes dependency resolution failure**
- **Found during:** Task 3 (Verify dependency resolution)
- **Issue:** spring-boot-starter-aop FAILED when spring-aop version set to 4.1.0
- **Fix:** Reverted spring-aop to 3.5.13 in libs.versions.toml (BOM will manage compatible version)
- **Files modified:** springboot/gradle/libs.versions.toml
- **Verification:** Gradle dependencies resolved successfully after revert
- **Committed in:** fc9e4fd (part of Task 3 commit)

**3. [Rule 3 - Blocking] dependency-management 1.1.8 not available**
- **Found during:** Task 3 (Verify dependency resolution)
- **Issue:** Plugin Repositories could not resolve io.spring.dependency-management:io.spring.dependency-management.gradle.plugin:1.1.8
- **Fix:** Reverted dependency-management plugin version to 1.1.7
- **Files modified:** springboot/gradle/libs.versions.toml, springboot/build.gradle.kts
- **Verification:** Gradle plugin resolution successful after revert
- **Committed in:** fc9e4fd (part of Task 3 commit)

---

**Total deviations:** 3 auto-fixed (all Rule 3 - blocking)
**Impact on plan:** All auto-fixes were necessary to achieve functional dependency resolution. No scope creep.

## Issues Encountered
- lombok 1.18.50 not available on mavenCentral - reverted to 1.18.44
- spring-aop 4.1.0 caused dependency resolution failure - reverted to 3.5.13
- dependency-management 1.1.8 not available - reverted to 1.1.7

## Next Phase Readiness
- Spring Boot 4.1.0-RC1 plugin configured correctly
- All core dependencies resolved successfully
- Build configuration ready for next phase (15-config-migration)
- No blockers identified

---
*Phase: 14-dependency-upgrade*
*Completed: 2026-04-30*