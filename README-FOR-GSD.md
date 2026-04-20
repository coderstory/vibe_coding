

## 它是怎么工作的

> **已经有现成代码库？** 先运行 `/gsd:map-codebase`。它会并行拉起多个代理分析你的技术栈、架构、约定和风险点。之后 `/gsd:new-project` 就会真正“理解”你的代码库，提问会聚焦在你打算新增的部分，规划时也会自动加载你的现有模式。

### 1. 初始化项目

```
/gsd:new-project
```

一个命令，一条完整流程。系统会：

1. **提问**：一直问到它彻底理解你的想法（目标、约束、技术偏好、边界情况）
2. **研究**：并行拉起代理调研领域知识（可选，但强烈建议）
3. **需求梳理**：提取哪些属于 v1、v2，哪些不在范围内
4. **路线图**：创建与需求映射的阶段规划

你审核并批准路线图后，就可以开始构建。

**生成：** `PROJECT.md`、`REQUIREMENTS.md`、`ROADMAP.md`、`STATE.md`、`.planning/research/`

---

### 2. 讨论阶段

```
/gsd:discuss-phase 1
```

**这是你塑造实现方式的地方。**

你的路线图里，每个阶段通常只有一两句话。这点信息不足以让系统按 *你脑中的样子* 把东西做出来。这一步的作用，就是在研究和规划之前，把你的偏好先收进去。

系统会分析该阶段，并根据要构建的内容识别灰区：

- **视觉功能**：布局、信息密度、交互、空状态
- **API / CLI**：返回格式、flags、错误处理、详细程度
- **内容系统**：结构、语气、深度、流转方式
- **组织型任务**：分组标准、命名、去重、例外情况

对每个你选择的区域，系统都会持续追问，直到你满意为止。最终产物 `CONTEXT.md` 会直接喂给后续两个步骤：

1. **研究代理会读取它**：知道该研究哪些模式（例如“用户想要卡片布局” → 去研究卡片组件库）
2. **规划代理会读取它**：知道哪些决策已经锁定（例如“已决定使用无限滚动” → 计划里就会包含滚动处理）

你在这里给出的信息越具体，系统越能构建出你真正想要的东西。跳过它，你拿到的是合理默认值；用好它，你拿到的是 *你的* 方案。

**生成：** `{phase_num}-CONTEXT.md`

---

### 3. 规划阶段

```
/gsd:plan-phase 1
```

系统会：

1. **研究**：结合你的 `CONTEXT.md` 决策，调研这一阶段该怎么实现
2. **制定计划**：创建 2-3 份原子化任务计划，使用 XML 结构
3. **验证**：将计划与需求对照检查，直到通过为止

每份计划都足够小，可以在一个全新的上下文窗口里执行。没有质量衰减，也不会出现“我接下来会更简洁一些”的退化状态。

**生成：** `{phase_num}-RESEARCH.md`、`{phase_num}-{N}-PLAN.md`

---

### 4. 执行阶段

```
/gsd:execute-phase 1
```

系统会：

1. **按 wave 执行计划**：能并行的并行，有依赖的顺序执行
2. **每个计划使用新上下文**：20 万 token 纯用于实现，零历史垃圾
3. **每个任务单独提交**：每项任务都有自己的原子提交
4. **对照目标验证**：检查代码库是否真的交付了该阶段承诺的内容



**生成：** `{phase_num}-{N}-SUMMARY.md`、`{phase_num}-VERIFICATION.md`

---

### 5. 验证工作

```
/gsd:verify-work 1
```

**这是你确认它是否真的可用的地方。**

自动化验证能检查代码存在、测试通过。但这个功能是否真的按你的预期工作？这一步就是让你亲自用。

系统会：

1. **提取可测试的交付项**：你现在应该能做到什么
2. **逐项带你验证**：“能否用邮箱登录？” 可以 / 不可以，或者描述哪里不对
3. **自动诊断失败**：拉起 debug 代理定位根因
4. **创建验证过的修复计划**：可立刻重新执行

如果一切通过，就进入下一步；如果哪里坏了，你不需要手动 debug，只要重新运行 `/gsd:execute-phase`，执行它自动生成的修复计划即可。

**生成：** `{phase_num}-UAT.md`，以及发现问题时的修复计划

---

### 6. 重复 → 发布 → 完成 → 下一个里程碑

```
/gsd:discuss-phase 2
/gsd:plan-phase 2
/gsd:execute-phase 2
/gsd:verify-work 2
/gsd:ship 2                  # 从已验证的工作创建 PR
...
/gsd:complete-milestone
/gsd:new-milestone
```

或者让 GSD 自动判断下一步：

```
/gsd:next                    # 自动检测并执行下一步
```


### 快速模式



## 为什么它有效

### 上下文工程

Claude Code 非常强大，前提是你把它需要的上下文给对。大多数人做不到。

GSD 会替你处理：

| 文件 | 作用 |
|------|------|
| `PROJECT.md` | 项目愿景，始终加载 |
| `research/` | 生态知识（技术栈、功能、架构、坑点） |
| `REQUIREMENTS.md` | 带 phase 可追踪性的 v1/v2 范围定义 |
| `ROADMAP.md` | 你要去哪里、哪些已经完成 |
| `STATE.md` | 决策、阻塞、当前位置，跨会话记忆 |
| `PLAN.md` | 带 XML 结构和验证步骤的原子任务 |
| `SUMMARY.md` | 做了什么、改了什么、已写入历史 |
| `todos/` | 留待后续处理的想法和任务 |

这些尺寸限制都是基于 Claude 在何处开始质量退化得出的。控制在阈值内，输出才能持续稳定。



## 命令

### 核心工作流

| 命令 | 作用 |
|------|------|
| `/gsd:new-project [--auto]` | 完整初始化：提问 → 研究 → 需求 → 路线图 |
| `/gsd:discuss-phase [N] [--auto] [--analyze]` | 在规划前收集实现决策（`--analyze` 增加权衡分析） |
| `/gsd:plan-phase [N] [--auto] [--reviews]` | 为某个阶段执行研究 + 规划 + 验证（`--reviews` 加载代码库审查结果） |
| `/gsd:execute-phase <N>` | 以并行 wave 执行全部计划，完成后验证 |
| `/gsd:verify-work [N]` | 人工用户验收测试 ¹ |
| `/gsd:ship [N] [--draft]` | 从已验证的阶段工作创建 PR，自动生成 PR 描述 |
| `/gsd:fast <text>` | 内联处理琐碎任务——完全跳过规划，立即执行 |
| `/gsd:next` | 自动推进到下一个逻辑工作流步骤 |
| `/gsd:audit-milestone` | 验证里程碑是否达到完成定义 |
| `/gsd:complete-milestone` | 归档里程碑并打 release tag |
| `/gsd:new-milestone [name]` | 开始下一个版本：提问 → 研究 → 需求 → 路线图 |
| `/gsd:milestone-summary` | 从已完成的里程碑产物生成项目概览，用于团队上手 |
| `/gsd:forensics` | 对失败或卡住的工作流进行事后调查 |

### 工作流（Workstreams）

| 命令 | 作用 |
|------|------|
| `/gsd:workstreams list` | 显示所有工作流及其状态 |
| `/gsd:workstreams create <name>` | 创建命名空间工作流，用于并行里程碑工作 |
| `/gsd:workstreams switch <name>` | 切换当前活跃工作流 |
| `/gsd:workstreams complete <name>` | 完成并合并工作流 |

### 多项目工作区

| 命令 | 作用 |
|------|------|
| `/gsd:new-workspace` | 创建隔离工作区，包含仓库副本（worktree 或 clone） |
| `/gsd:list-workspaces` | 显示所有 GSD 工作区及其状态 |
| `/gsd:remove-workspace` | 移除工作区并清理 worktree |

### UI 设计

| 命令 | 作用 |
|------|------|
| `/gsd:ui-phase [N]` | 为前端阶段生成 UI 设计合约（UI-SPEC.md） |
| `/gsd:ui-review [N]` | 对已实现前端代码进行 6 维视觉审计 |

### 导航

| 命令 | 作用 |
|------|------|
| `/gsd:progress` | 我现在在哪？下一步是什么？ |
| `/gsd:next` | 自动检测状态并执行下一步 |
| `/gsd:help` | 显示全部命令和使用指南 |
| `/gsd:update` | 更新 GSD，并预览变更日志 |
| `/gsd:join-discord` | 加入 GSD Discord 社区 |

### Brownfield

| 命令 | 作用 |
|------|------|
| `/gsd:map-codebase` | 在 `new-project` 前分析现有代码库 |

### 阶段管理

| 命令 | 作用 |
|------|------|
| `/gsd:add-phase` | 在路线图末尾追加 phase |
| `/gsd:insert-phase [N]` | 在 phase 之间插入紧急工作 |
| `/gsd:remove-phase [N]` | 删除未来 phase，并重编号 |
| `/gsd:list-phase-assumptions [N]` | 在规划前查看 Claude 打算采用的方案 |
| `/gsd:plan-milestone-gaps` | 为 audit 发现的缺口创建 phase |

### 代码质量

| 命令 | 作用 |
|------|------|
| `/gsd:review` | 对当前阶段或分支进行跨 AI 同行评审 |
| `/gsd:pr-branch` | 创建过滤 `.planning/` 提交的干净 PR 分支 |
| `/gsd:audit-uat` | 审计验证债务——找出缺少 UAT 的阶段 |

### 积压

| 命令 | 作用 |
|------|------|
| `/gsd:plant-seed <idea>` | 将想法存入积压停车场，留待未来里程碑 |

### 会话

| 命令 | 作用 |
|------|------|
| `/gsd:pause-work` | 在中途暂停时创建交接上下文（写入 HANDOFF.json） |
| `/gsd:resume-work` | 从上一次会话恢复 |
| `/gsd:session-report` | 生成会话摘要，包含已完成工作和结果 |

### 工具

| 命令 | 作用 |
|------|------|
| `/gsd:settings` | 配置模型 profile 和工作流代理 |
| `/gsd:set-profile <profile>` | 切换模型 profile（quality / balanced / budget / inherit） |
| `/gsd:add-todo [desc]` | 记录一个待办想法 |
| `/gsd:check-todos` | 查看待办列表 |
| `/gsd:debug [desc]` | 使用持久状态进行系统化调试 |
| `/gsd:do <text>` | 将自由文本自动路由到正确的 GSD 命令 |
| `/gsd:note <text>` | 零摩擦想法捕捉——追加、列出或提升为待办 |
| `/gsd:quick [--full] [--discuss] [--research]` | 以 GSD 保障执行临时任务（`--full` 增加计划检查和验证，`--discuss` 先补上下文，`--research` 在规划前先调研） |
| `/gsd:health [--repair]` | 校验 `.planning/` 目录完整性，带 `--repair` 时自动修复 |
| `/gsd:stats` | 显示项目统计——阶段、计划、需求、git 指标 |
| `/gsd:profile-user [--questionnaire] [--refresh]` | 从会话分析生成开发者行为档案，用于个性化响应 |



---
npm config set registry https://registry.npmmirror.com

npm install -g @playwright/cli@latest

playwright-cli install --skills

npx superpowers-zh


npm install -g @gitlawb/openclaude

https://skillhub.cn/