---
plan: 22-PLAN
phase: 22
status: complete
completed: 2026-05-07
tasks:
  - id: task-1
    status: complete
    description: 清理 shared 层
    findings: 编译通过，无死代码需要删除。修复了 AuditAspect.java 缺少 Map import 的编译错误
  - id: task-2
    status: complete
    description: 清理 auth 域
    findings: 代码干净，无死代码
  - id: task-3
    status: complete
    description: 清理 user/role/menu 域
    findings: 代码干净，无死代码
  - id: task-4
    status: complete
    description: 清理 audit/order/knowledge 域
    findings: 代码干净，无死代码
  - id: task-5
    status: complete
    description: 清理 monitor/rocketmq 域
    findings: 删除 MonitorController 中未使用的局部变量 qpsKey（BAC-02）
  - id: task-6
    status: complete
    description: 清理 seckill 域（高风险）
    findings: 代码干净，所有 parseXxx 辅助方法均被 getActivityFromCache 使用，非死代码
  - id: task-7
    status: complete
    description: 全量验证
    findings: |
      - build -x test: BUILD SUCCESSFUL ✓
      - checkstyleMain: 1 个存量违规（KnowledgeServiceImpl 星号导入，非新增）
      - 零新增告警
key-files:
  modified:
    - springboot/src/main/java/cn/coderstory/springboot/shared/aspect/AuditAspect.java
    - springboot/src/main/java/cn/coderstory/springboot/monitor/controller/MonitorController.java
deviations:
  - 原计划尝试删除 ActivityServiceImpl 的 4 个 parseXxx 方法，确认被 getActivityFromCache 使用后恢复
  - 项目在 Phase 21 后代码已相当干净，实际死代码少于预期
known_issues:
  - KnowledgeServiceImpl 星号导入（AvoidStarImport）为 Phase 22 前存量问题，未修复
  - PMD/SpotBugs 未集成，checkstyle isIgnoreFailures=true
---

# Summary: 后端死代码清理

## Output

Phase 22 清理完成。后端 121 个 Java 文件经全面扫描：

- **编译修复**: AuditAspect.java 缺少 `import java.util.Map`（原代码使用 `Map<?, ?>` 模式匹配是预览特性，改为传统 instanceof + 强制转型）
- **死变量删除**: MonitorController 中的 `String qpsKey`（声明后从未读取）
- **全量验证**: 编译通过，Checkstyle 零新增告警

## Next

Phase 23 后端结构体优化可以开始。
