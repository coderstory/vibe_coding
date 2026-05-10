# Superpowers-ZH 中文增强版

> 本文是 CLAUDE.md Superpowers-ZH 章节的详细展开。CLAUDE.md 仅保留核心规则和速览。

## 核心规则

1. **收到任务时，先检查是否有匹配 skill** — 哪怕 1% 可能性也要检查
2. **设计先于编码** — 功能需求先用 brainstorming 做需求分析
3. **测试先于实现** — 写代码前先写测试（TDD）
4. **验证先于完成** — 声称完成前必须运行验证命令

## 可用 Skills

Skills 位于 `.claude/skills/`，每 skill 有独立 `SKILL.md`。

| Skill | 用途 |
|-------|------|
| **brainstorming** | 创造性工作前必须使用——创建功能、构建组件、添加功能或修改行为。实现前探索用户意图、需求和设计。 |
| **chinese-code-review** | 中文代码审查规范——专业严谨，符合国内团队文化。给出有效反馈。 |
| **chinese-commit-conventions** | 中文 Git 提交规范——适配国内团队的 commit message 和 changelog 自动化。 |
| **chinese-documentation** | 中文技术文档写作规范——排版、术语、结构一步到位，告别机翻味。 |
| **chinese-git-workflow** | 适配国内 Git 平台和团队习惯的工作流——Gitee、Coding、极狐 GitLab、CNB 全覆盖。 |
| **dispatching-parallel-agents** | 面对 2+ 可独立进行、无共享状态或顺序依赖的任务时使用。 |
| **executing-plans** | 有书面实现计划需在单独会话中执行并设审查检查点时使用。 |
| **finishing-a-development-branch** | 实现完成、所有测试通过、需决定如何集成工作时使用——通过结构化选项引导开发收尾。 |
| **mcp-builder** | MCP 服务器构建方法论——系统化构建生产级 MCP 工具，让 AI 助手连接外部能力。 |
| **receiving-code-review** | 收到代码审查反馈后、实施建议前使用，尤其反馈不明确或技术上有疑问时——需要技术严谨性和验证，而非敷衍附和或盲目执行。 |
| **requesting-code-review** | 完成任务、实现重要功能或合并前使用，验证工作成果是否符合要求。 |
| **subagent-driven-development** | 在当前会话中执行包含独立任务的实现计划时使用。 |
| **systematic-debugging** | 遇到任何 bug、测试失败或异常行为时使用，提出修复方案前执行。 |
| **test-driven-development** | 实现功能或修复 bug 前使用，先写测试。 |
| **using-git-worktrees** | 需开始与当前工作区隔离的功能开发或执行实现计划前使用——创建具智能目录选择和安全验证的隔离 git 工作树。 |
| **using-superpowers** | 开始任何对话时使用——确立如何查找和使用技能，要求在任何响应（包括澄清性问题）前调用 Skill 工具。 |
| **verification-before-completion** | 宣称完成、已修复或测试通过前使用，提交或创建 PR 前——必须运行验证命令并确认输出后才能声称成功；始终用证据支撑断言。 |
| **workflow-runner** | 在 Claude Code / OpenClaw / Cursor 中直接运行 agency-orchestrator YAML 工作流——无需 API key，使用当前会话 LLM 作执行引擎。用户提供 .yaml 工作流文件或要求多角色协作完成任务时触发。 |
| **writing-plans** | 有规格说明或需求用于多步骤任务时使用，动手写代码前。 |
| **writing-skills** | 创建新技能、编辑现有技能或部署前验证技能有效性时使用。 |

## 单例 Skills

以下 claude-mem 系列 skills 为 **单例模式**，每会话最多调用一次：

- `claude-mem:learn-codebase` — 学习代码库，首次进入项目时调用
- `claude-mem:make-plan` — 创建实施计划
- `claude-mem:pathfinder` — 架构审计与流程图
- `claude-mem:knowledge-agent` — 知识库构建与查询
- `claude-mem:mem-search` — 跨会话记忆搜索
- `claude-mem:babysit` — 监控 PR 直至合并
- `claude-mem:timeline-report` — 项目演变报告
- `claude-mem:smart-explore` — AST 结构化代码搜索
- `claude-mem:version-bump` — 版本发布与标签

**规则**：单例 skill 已调用后，同会话不再重复调用。再次触发相同场景时直接跳过。

## 如何使用

任务匹配某 skill 时，用 `Skill` 工具加载并严格遵循其流程。绝不用 Read 工具读 SKILL.md。

若认为哪怕 1% 可能性某 skill 适用于当前工作，必须调用检查。
