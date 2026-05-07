# Pitfalls Research

**Domain:** 代码注释与文档工程（对现有项目追加注释）
**Researched:** 2026-05-07
**Confidence:** HIGH

## Critical Pitfalls

### Pitfall 1: 注释与代码"漂移"（Comment Drift）

**What goes wrong:**
注释描述的行为与实际代码不符。初级形式是参数列表对不上、返回值描述错误。严重时注释说"允许空值"但代码抛 NPE，或者注释说"线程安全"但实际没有同步。新人读到的是注释，信任注释然后踩坑。

**Why it happens:**
- 注释与代码之间**没有编译期或运行期检查**——修改代码时注释不会报错
- 大型 Java 项目中，独立注释修改（Independent Comment Changes）占了所有注释变更的约 **15.5%**（Wang et al., ACM TOSEM 2023），即每 6 次注释修改就有 1 次是专门修复漂移问题
- 对已有项目追补注释时，开发者倾向于"写一次就不管了"，后续代码修改不会同步回头看注释

**How to avoid:**
1. **同 PR 原则**：任何代码修改的 PR 中，涉及到的注释必须同步更新——在 PR Review Checklist 中加入"注释同步检查"
2. **AI 辅助审查**：利用 CoCC（Code-Comment Consistency）类工具，在 CI 中自动检测代码-注释不一致
3. **Javadoc 的 `@param`/`@return` 必须与签名同步**：方法签名改了，对应 Javadoc 必须修改。可以在 Checkstyle 中配置 `JavadocMethod` 检查强制一致性
4. **CR 阶段人工抽查**：每次 Code Review 随机选 2-3 个注释，对照代码验证其准确性

**Warning signs:**
- `@param name` 在 Javadoc 中写的是 `name` 但在代码中已经重命名为 `username`
- 注释提到"三种情况"但 switch/case 有四个分支
- 方法签名变了但 Javadoc 完全没动（Git blame 显示注释最后修改时间远早于代码本身）

**Phase to address:**
Phase 1（注释工程执行阶段）的 PR 审查环节，以及 Phase 2（注释保持同步）的 CI/Checkstyle 配置阶段。

---

### Pitfall 2: 过度注释（Over-Commenting / Noise Comments）

**What goes wrong:**
注释泛滥导致信号噪声比下降。典型情况：
```typescript
// 设置名称
this.name = name
// 设置年龄
this.age = age
```
或者自动生成的空 Javadoc 骨架：
```java
/**
 * @param id
 * @return
 */
```
这些注释占据视觉空间，迫使读者在理解代码之外还要过滤无意义信息。

**Why it happens:**
- 很多团队将"注释覆盖率"作为 KPI，导致开发者为了数字面面俱到
- IDE 自动补全 Javadoc 骨架（IDEA 的 `/** + Enter`）会产生空模板，开发者不填写内容就走了
- 对已有项目追注释时，缺乏"什么该注释、什么不该注释"的决策框架

**How to avoid:**
1. **制定"注释层级"策略**（详见 FEATURES.md）：
   - 配置/模块/类级别：必须注释（解释职责边界）
   - 公开 API 方法：必须 Javadoc/JSDoc（解释契约，非实现）
   - 复杂逻辑块：按需注释（解释 Why，不是 What）
   - 简单的 getter/setter/字段赋值：绝对不注释
2. **不允许空 Javadoc 骨架**——在 Checkstyle 中配置 `JavadocStyle` 和 `JavadocMethod` 禁止空的 `{@inheritDoc}` 和空 `@param`/`@return`
3. **注释审查标准**：如果一条注释只是重复了代码本身表达的信息，删掉它

**Warning signs:**
- 一个文件中有 30% 以上的行包含行尾注释（`// xxx`）
- Javadoc 中 `@param` 和 `@return` 只有一个单词，没有上下文解释
- 字段上的注释是 "// 用户名" 这种显而易见的内容

**Phase to address:**
Phase 1 的注释风格指南定义阶段（先定标准再执行）。

---

### Pitfall 3: "注释即清洁剂"——注释掩盖烂代码（False Confidence / Deodorant Comments）

**What goes wrong:**
开发者写了让代码"看起来合理"的长篇注释，然后用"已有注释说明"作为不重构的借口。Martin Fowler 在 *Refactoring* 中说得明确：**"Comments often are used as a deodorant. It's surprising how often you look at thickly commented code and notice that the comments are there because the code is bad."**

更隐蔽的形式：注释说"这看起来复杂但算法就是这样，别动"，实际上代码完全可以简化，注释成了拒绝改变的保护伞。

**Why it happens:**
- 给烂代码加注释比重构烂代码容易得多（成本低、风险小、即时满足感强）
- 团队缺乏重构文化，倾向于用文档化来掩盖设计问题
- "先注释，后面再重构"成为永远不执行的承诺

**How to avoid:**
1. **Fowler 原则**：当你觉得"这段代码需要一个注释来解释"时，先尝试重构它。如果重构后注释变得多余，那就删掉注释
2. **追注释时同步做小重构**：给已有项目加注释时，遇到"如果不加长注释就看不懂"的代码，先考虑能否重构成更清晰的形式
3. **区分"业务原因"和"代码原因"**：注释应该解释业务约束、历史决策、外部限制，而不是补偿糟糕的命名和混乱的逻辑
4. **Code Review 时留意"注释保护"模式**：看到一个大段注释 + 复杂方法，问"能不能拆成小方法让代码说话？"

**Warning signs:**
- 方法体超过 50 行，开头有一段 5+ 行的注释解释"这段代码在做什么"
- 变量名是单字母（`a`、`b`、`c`）或拼音，但注释说"a 是数量"
- 同一个方法中注释和代码行数的比例超过 1:3

**Phase to address:**
Phase 1 开始前的"注释哲学培训"（或团队共识讨论），以及 Phase 1+ 持续 Code Review。

---

### Pitfall 4: 团队摩擦与"自行车棚"效应（Bike-shedding on Commenting Standards）

**What goes wrong:**
加注释的任务看起来"简单易懂"，导致每个人都有自己的偏好，review 时争论不休：
- "注释应该用中文还是英文？"
- "Javadoc 用 `/** */` 还是 `//`？"
- "getter 要不要加注释？"
- "每行要不要写句号？"

这些争论消耗了大量时间，但对产品质量的提升微乎其微。而真正重要的问题（业务逻辑是否正确、边界条件是否覆盖）反而没人注意。

**Why it happens:**
- Parkinson's Law of Triviality：所有人都觉得自己对"写注释"有资格发言，但没人觉得对"消息队列事务边界"有资格评论
- 团队没有一个预先确定、全员同意的注释规范

**How to avoid:**
1. **预先确定注释规范，不在 review 中讨论**：在 Phase 1 开始前花 30 分钟开会确定标准，写入 CLAUDE.md 或 CONTRIBUTING.md。Review 时只 check 是否遵循既定标准，不重新讨论标准本身
2. **使用 Conventional Comments 标记法**：给 review 评论加前缀 —— `nit:`（琐碎问题，可忽略）、`blocking:`（必须修改）、`question:`（纯疑问）、`suggestion:`（可选建议）。这样作者知道哪些是必须改的，哪些只是个人偏好
3. **自动化能自动化的**：用 Checkstyle/ESLint 规则强制注释格式，不需要人工 review。比如 `@param` 必须有描述、禁止空 Javadoc、行尾注释长度限制等
4. **默认采用项目已有风格**：本项目的后端已有一些 `// 登录/登出/新增/编辑/删除` 风格的行尾注释，前端已有 `/** */` 的 JSDoc 注释。追注释时与已有风格保持一致即可

**Warning signs:**
- PR 中超过 3 条 comment 是关于注释格式/语言/风格的
- 有人要求全项目统一某种特定注释模板
- Code Review 中"注释应该用句号结尾"之类的问题占用了 15 分钟讨论

**Phase to address:**
Phase 0（注释标准定义）——在写任何注释之前先达成共识。

---

### Pitfall 5: 工具链问题——Javadoc/JSDoc 构建中断

**What goes wrong:**
添加或修改 Javadoc 后构建失败，常见原因：
- Java 11+ 的 Javadoc 工具将未解析的符号（unresolved symbol）视为硬错误——如果依赖没配完整，Javadoc 生成直接失败
- 错误的 HTML 标签嵌套（`<` 未转义、未闭合的 `<code>`）导致生成异常
- `@link` 引用了不存在的类或方法
- JSDoc 的 TypeScript 类型引用不完整导致 lint 报错

**Why it happens:**
- 追注释时开发者不运行 `./gradlew.bat build` 验证就提交
- JDK 11+ 的 Javadoc 工具比旧版本严格得多，不再容忍错误
- 开发者不清楚 Gradle 的 Javadoc 任务配置（是否配置了 `failOnError`）

**How to avoid:**
1. **Gradle 配置 `failOnError = false` + `failOnWarnings = false`**（在 `springboot/build.gradle.kts` 中配置 Javadoc 任务）避免构建中断。同时单独开启一个可选任务用于严格的文档检查
2. **前置验证**：在提交前运行 `./gradlew.bat javadoc` 确保生成成功。本项目的构建流程已包含 `build` 任务，会触发 Javadoc
3. **`@link` 使用全限定类名**减少跨模块引用问题
4. **前端 JSDoc**：TypeScript 项目中确保 JSDoc 引用的类型存在，不要在 JSDoc 中使用运行时未导入的类型

**Warning signs:**
- `./gradlew.bat build` 在添加注释后出现 `javadoc: warning - Tag @link: reference not found`
- `javadoc: error - broken link` 导致构建失败
- `.github/workflows/` 中的 CI 配置没有排除 Javadoc 检查

**Phase to address:**
Phase 1 的验证阶段——修改 Gradle 配置 + 提交前需运行 `javadoc` 任务确认无报错。

---

### Pitfall 6: 注释维护成本被严重低估（Maintenance Burden Blindness）

**What goes wrong:**
项目规划时认为"加注释是一次性工作"，忽略了注释的长期维护成本。每条注释都变成了技术债务的组成部分——当代码修改时，注释需要同步更新。如果没有人负责维护，注释迅速过时，从资产变为负债。

PRESTI 研究（arXiv:2309.06020）表明，文档型技术债务（代码注释和提交信息中的 SATD）的平均偿还成本为 72.7 行代码变更/条。一个 10 万行代码的项目中，如果随意添加 2000 条注释，隐含的长期维护成本相当于 **145,400 行变更**。

**Why it happens:**
- 管理层看到"注释"就认为是纯资产，看不到当代码变化时注释变成负资产的拐点
- 估算时只算"写注释的时间"，不算"以后每次修改代码时更新注释的时间"
- 追赶度注释时更容易忽略维护成本——因为注释不是自己写的，更新时更难发现哪些需要改

**How to avoid:**
1. **为注释分配维护预算**：每条注释应视作 5-15 分钟/年 的维护成本（取决于复杂度和变更频率）。当一个模块的代码频繁变更时，注释要尽可能少而精
2. **严格限制注释量**：追注释不是"越多越好"。目标应该是让关键路径上的代码可理解，而不是 100% 覆盖率。根据 ACM TOSEM 研究，没有注释指南的项目更可能出现过时注释
3. **只在稳定接口和复杂逻辑加详细注释**：频繁变更的函数体内部，注释尽量少；稳定不变的公共 API 可以详细注释
4. **Diff-based 审查**：在 review 代码变更时，强制要求审查注释是否也需要更新。如果注释没变但代码变了，要么注释不需要改（验证一下），要么注释已经过时了

**Warning signs:**
- 同一个人在一个函数上加注释的频率高于修改该函数的频率
- 项目的 git log 显示"rename method"类的提交中没有伴随注释更新
- 新人问的问题与已有注释的内容不一致（注释已过时）

**Phase to address:**
Phase 1 规划阶段——设置注释维护预算。Phase 2+ 通过 PR 审查持续执行。

---

### Pitfall 7: 中文注释 vs 英文注释的混乱（Comment Language Inconsistency）

**What goes wrong:**
同一个项目中中英混用，没有统一标准。典型表现：
- 某个文件的 Javadoc 用英文，另一个文件的用中文
- 类注释用中文，方法注释用英文，行内注释又用中文
- 某个变量名是拼音注释（`// 用户状态`），另一个是英文注释（`// user status`）
- 英文注释语法错误拼写错误，比中文更难理解

**Why it happens:**
- 团队没有明确讨论过注释语言策略
- 早期开发者用英文（因为教科书和开源项目都用英文），后来加入的成员用中文（因为沟通效率更高）
- IDE 的 Javadoc 模板默认生成英文占位符

**How to avoid:**
1. **明确本项目的注释语言标准**：本项目是**纯中国团队**，业务文档用中文，评审用中文，CLAUDE.md 本身也是中文。建议采用 **中文注释策略**：
   - 类/接口 Javadoc → 中文
   - 方法 Javadoc → 中文
   - 复杂逻辑行内注释 → 中文
   - 技术术语保留英文（`API`、`REST`、`NPE`、`RocketMQ`、`Redis`）
   - 代码标识符（变量名、方法名、类名）保持英文（这是代码本身，不是注释）
2. **在 CLAUDE.md 中添加注释语言规范**（此项目已明确"默认语言：中文"）
3. **不要强制翻译已有注释**：现有代码中已有一些英文行尾注释和中文 Javadoc，追注释时**与已有风格保持一致**，不在本次里程碑中强制统一语言
4. **编码统一 UTF-8**：中文注释必须使用 UTF-8 编码，确保跨平台兼容

**Warning signs:**
- 同一个文件中一半注释中文一半英文
- 英文注释有明显语法错误或拼写错误
- 团队内部讨论时说的是中文，但写注释时莫名切换到英文

**Phase to address:**
Phase 0 的规范定义阶段。如果不在此阶段确定，Phase 1 执行时会出现大量不一致。

---

### Pitfall 8: "注释错觉"——注释很多就以为文档够了（Documentation Theater）

**What goes wrong:**
追注释任务完成后产生了"我们已经加完注释了"的错觉。但实际上：
- 高层的架构决策（为什么用 Redis 而不是本地缓存、为什么事务消息这么设计）没有被记录
- 模块之间的边界契约没有被文档化
- new 成员仍然需要口头问别人才能理解系统设计

代码注释只回答了"这段代码怎么用"，但没有回答"为什么系统是这样设计的"。

**Why it happens:**
- 把"加了注释"等同于"文档化完成了"
- 只关注了代码层的微观注释，忽略了架构层、模块层、配置层的宏观文档
- 追注释的任务范围定义得太窄（只包括代码注释，不包括 ADR、README、config 注释）

**How to avoid:**
1. **三类文档都要覆盖**（在 Phase 1 中明确 scope）：
   - **代码注释**：类/方法/复杂逻辑（本 milestone 的重点，已有规划）
   - **配置文件注释**：YAML/Gradle/ESLint 配置项的含义（已在 PROPOSAL 中，但不要遗漏）
   - **架构级文档**：`docs/` 下的设计文档、README、这次暂不包含但需要在 Phase 2 中规划
2. **代码注释不说 "Why Architecture"**：代码注释不需要解释"为什么系统是三层架构"，但需要解释"为什么这个方法用 MergeSort 而不是 QuickSort"
3. **在 README 中明确注释范围**：标明哪些有注释、哪些没有注释，避免新人以为全项目都有注释

**Warning signs:**
- 项目有大量代码注释，但新成员仍然需要花 2 周才能上手
- 配置项没有任何注释说明（本项目已有分层的 config 文件，但需要检查是否缺少注释）
- 找不到任何关于"为什么这里用 RocketMQ 事务消息"的说明

**Phase to address:**
Phase 0 的范围定义阶段。本里程碑需要明确"我们注释什么、不注释什么"。

---

### Pitfall 9: TODO/FIXME 注释泛滥（Unmanaged Debt Annotations）

**What goes wrong:**
追注释过程中，开发者发现代码问题时会顺手写上 `// TODO` 或 `// FIXME`。这些标记如果没有管理机制，会无限期留在代码中，最终变成"墙上的涂鸦"——所有人看到但没人处理。

**Why it happens:**
- 给项目加注释时难免发现代码瑕疵，顺手写 TODO 的冲动很大
- 没有工单追踪的 TODO 等于没写——它不存在于任何人的 backlog 中
- 现有的代码中几乎看不到 TODO/FIXME（搜索结果只有 1 处出现 `TODO`），说明团队之前管理较好，但要警惕新加注释时引入 TODO 泛滥

**How to avoid:**
1. **TODO 必须有 Issue 编号**：不允许裸写 `// TODO: fix this`，必须写成 `// TODO: ISSUE-142 临时处理前端传入的大写 ID`。没有 Issue 的 TODO 不应该存在
2. **CI 检测 + 计数**：在 CI 中统计 TODO/FIXME 数量，超过阈值（例如 5 个）时给出警告。Gradle 中可以用一个自定义 task 扫描 `// TODO` 并且不包含 Issue 编号的
3. **追注释阶段禁止引入新 TODO**：发现的问题用 Issue 跟踪，不在注释中夹带。注释工程的目标是减少认知负荷，不是增加新的待办项
4. **定期清理**：每个 milestone 结束时运行 TODO 扫描，将 TODO 转化为 Issue 或删除已经过期的

**Warning signs:**
- 一个 PR 中加了 3 个以上 TODO
- TODO 没有关联 Issue 编号
- 同一个 TODO 在代码中存在超过 6 个月还没有被处理

**Phase to address:**
Phase 1 的执行规范中定义 TODO 使用规则。Phase 2 的 CI 配置中加入 TODO 扫描。

---

### Pitfall 10: 注释分散在多个位置（Scattered Documentation）

**What goes wrong:**
同一个概念的解释出现在多个地方——Javadoc 中解释了一遍 `@param status` 的含义，`application.yaml` 中又解释了一遍 `app.status`，API 文档中再解释一遍 `status` 字段。当业务含义变了时，三个地方只更新了一个。

**Why it happens:**
- 没有"单一信息源"的认知
- 注释是随着开发过程被动添加的，不是设计好的
- Java 后端 + Vue 前端 + YAML 配置三个领域，各自有各自的注释习惯

**How to avoid:**
1. **边界原则**：配置文件的注释解释配置项的含义和取值范围；后端代码的注释解释业务逻辑；前端代码的注释解释 UI 行为和 API 调用方式。同一个"状态码"的解释不要重复三次，但也不是用 `@link` 跨域引用
2. **如果必须重复，加一句"见 X 处的解释"**：配置文件中可以写 `# status 取值说明见 OrderStatusEnum 的 Javadoc`
3. **"API 文档"层**：如果有 REST API 文档（Swagger/SpringDoc），代码注释应与之互补而不是重复。Javadoc 不用写"这个接口返回用户列表"，那是 API 文档的事

**Warning signs:**
- 配置文件中有一大段业务逻辑说明
- 前端和后端代码中注释描述了同一个概念但措辞不一致
- Swagger/Knife4j 文档中的描述与代码 Javadoc 描述有出入

**Phase to address:**
Phase 0 的注释范围定义阶段。明确各层注释的职责边界。

---

## Technical Debt Patterns

| Shortcut | Immediate Benefit | Long-term Cost | When Acceptable |
|----------|-------------------|----------------|-----------------|
| 写 `// TODO` 代替创建 Issue | 即时记录不留上下文 | TODO 永远不被处理，变成永久噪音 | 永远不可接受（必须有关联 Issue 编号） |
| 用注释解释烂代码（deodorant comment） | 避免重构风险 | 注释过时后反而误导，烂代码持续腐化 | 永远不可接受（应优先重构，至少记录 Issue） |
| 自动生成空 Javadoc 骨架后不填充 | 快速满足"覆盖率"KPI | 空模板作为噪声存在中位数 431 天，降低信号比 | 永远不可接受 |
| 追注释时跳过配置文件和测试文件 | 减少工作量，更聚焦 | 配置项含义不明，新人反复踩坑 | 短期暂缺可接受（需在后续迭代补上） |
| 全项目统一中/英文注释语言（一次性） | 风格一致 | 翻译已有注释成本高，可能引入新错误 | 本 milestone 可接受"与已有风格保持一致"而非强求统一 |

## Integration Gotchas

| Integration | Common Mistake | Correct Approach |
|-------------|----------------|------------------|
| Javadoc + Gradle `build` | 修改 Javadoc 后不运行 `build` 直接提交 | 提交前运行 `./gradlew.bat javadoc` 确认无报错 |
| JSDoc + TypeScript | JSDoc 中引用了未导入的类型 | 确保类型存在且已导入，或使用 `@type` 在 JSDoc 中声明 |
| Checkstyle + Javadoc | Checkstyle 规则太严格导致构建失败 | 分层级配置：公共 API 强制 Javadoc，私有方法可选 |
| ESLint + JSDoc | ESLint 的 `require-jsdoc` 规则对 `.vue` 文件中的 `<script>` 块不适用 | 先确认规则范围，对 `.vue` 文件单独配置 |

## Performance Traps

| Trap | Symptoms | Prevention | When It Breaks |
|------|----------|------------|----------------|
| 注释中嵌入了大段示例数据或 JSON | 文件体积膨胀，编译/加载变慢 | 示例数据放到外部文件或用 `@see` 引用 | 大型项目（10万+行）时的影响明显 |
| Javadoc 中频繁使用 `{@link}` 跨模块引用 | Javadoc 生成变慢，模块间耦合增加 | 限制跨模块的 `@link`，用纯文本代替 | 模块多的大型项目 |

## Security Mistakes

涉及代码注释的安全问题不多，但有一个常见问题：

| Mistake | Risk | Prevention |
|---------|------|------------|
| 在注释中泄露敏感信息（数据库密码、API Key、内部 IP、认证流程细节） | 代码被截图或公开后敏感信息暴露 | CI 中扫描注释中的敏感模式（password=、secret=、http://192.168.），使用 git-secrets 或 truffleHog |
| 注释中写"这部分绕过权限检查" | 恶意开发者可直接利用 | 注释只描述"做了什么"，不描述"刻意绕过了什么"。绕过本身就应该有 Issue 跟踪 |

## UX Pitfalls

| Pitfall | User Impact | Better Approach |
|---------|-------------|-----------------|
| 配置项无注释 | 新开发者需要翻阅代码才能知道配置项含义 | 每个配置项至少有一行注释说明取值范围和用途 |
| API 返回字段无注释 | 前端需要问后端才知道字段含义 | VO/DTO 的每个字段加注释（本项目已有部分实体字段注释，补全即可） |
| 注释用英文写但语法错误 | 比不写还难理解，读者需要解码"Chinglish" | 中国团队直接用中文，避免半吊子英文 |

## "Looks Done But Isn't" Checklist

- [ ] **后端 Controller 注释**: 确认每个 Controller 有类级 Javadoc（说明路由前缀和职责），每个公开方法至少有一行注释。不能只写方法名不做解释
- [ ] **配置文件注释**: 确认 `config/` 下每个 YAML 文件的主要属性有注释。尤其是 `datasource.yaml`（连接池参数含义）、`business.yaml`（业务开关含义）
- [ ] **枚举类注释**: 枚举值必须有注释说明业务含义。枚举类本身要有 Javadoc
- [ ] **VO/DTO 字段注释**: 确认所有 VO/DTO 的字段有注释（已有部分，补全缺失的）。特别是 `AuditLog` 和 `ActivityDetailVO` 这种跨模块传递的对象
- [ ] **前端组件 Props/Emits**: 确认每个 `defineProps` 和 `defineEmits` 的接口有注释。Vue 模板中的复杂计算逻辑有行内注释
- [ ] **路由守卫注释**: `router/guards.ts` 中的守卫逻辑必须有注释（认证检查、角色检查、token 刷新）
- [ ] **Store 注释**: Pinia store 的 state/getter/action 必须有注释。特别是涉及异步操作和缓存策略的
- [ ] **测试文件注释**: 测试类和方法不需要长篇 Javadoc，但测试数据的分块注释和测试意图的单行注释很重要
- [ ] **Gradle 脚本注释**: `build.gradle.kts`、`libs.versions.toml` 中的非直观配置（特殊插件配置、依赖排除、编码设置）必须有注释

## Recovery Strategies

当注释工程中出现上述问题时，如何补救：

| Pitfall | Recovery Cost | Recovery Steps |
|---------|---------------|----------------|
| 注释与代码不一致（漂移） | MEDIUM | 在涉及该文件的 PR 中同步修复，不需要专门跑一遍全项目检查 |
| TODO 泛滥 | LOW | 批量扫描 `// TODO`，对无 Issue 编号的创建 Issue 后补编号，无法关联的直接删除 |
| 注释风格不统一 | HIGH（全量） | 不建议全量重来。只在新文件和新 PR 中强制统一风格，逐步收敛 |
| 空 Javadoc 骨架 | MEDIUM | 运行脚本扫描空 Javadoc 或空 `@param`/`@return`，逐文件填充或删除 |
| 注释泄露敏感信息 | HIGH | 立即修复，修订 git history 移除（如有公开仓库风险） |
| 注释量过多（过度注释） | LOW | 只删除"代码已经清楚表达"的冗余注释，保留有信息量的 |

## Pitfall-to-Phase Mapping

| Pitfall | Prevention Phase | Verification |
|---------|------------------|--------------|
| 注释漂移 | Phase 1（PR 审查加入"注释同步检查"） | 随机抽查 5 个文件，比对 `git blame` 的注释和代码修改时间 |
| 过度注释 | Phase 0（注释风格指南定义） | 审查时标记"这段注释是否只是复述代码" |
| Deodorant Comment | Phase 0（团队共识）+ Phase 1（CR 关注） | CR 时问"如果重构掉这段代码，注释还必要吗？" |
| 团队摩擦（bike-shedding） | Phase 0（共识会议 + 规范文档） | 回顾 Phase 1 的 CR 中有多少条关于风格/语言的评论 |
| 工具链构建失败 | Phase 1（Gradle Javadoc 任务验证） | 提交前 `./gradlew.bat javadoc` 零错误 |
| 维护成本被低估 | Phase 1 规划（设维护预算） | 季度回顾：注释过时比例通过抽查估算 |
| 中英混用 | Phase 0（语言策略确定） | CI 脚本扫描统计注释语言分布 |
| Documentation Theater | Phase 0（范围定义明确哪些注释、哪些不注释） | 新成员上手时间是否有改善 |
| TODO 泛滥 | Phase 1（规范：TODO 必须关联 Issue） | CI TODO 计数检查 |
| 注释分散/重复 | Phase 0（各层职责边界） | 同一个概念在跨层注释中是否一致 |

## Sources

- Wang et al., "Suboptimal Comments in Java Projects: From Independent Comment Changes to Commenting Practices", ACM Transactions on Software Engineering and Methodology, 2023. https://dl.acm.org/doi/full/10.1145/3546949
- Martin Fowler, *Refactoring: Improving the Design of Existing Code* — "Comments as deodorant" concept
- Huang et al., "Are your comments outdated? Toward automatically detecting code-comment consistency", Journal of Software: Evolution and Process, 2024. https://onlinelibrary.wiley.com/doi/abs/10.1002/smr.2718 (CoCC 工具)
- Liu, Xia, Lo et al., "Just-In-Time Obsolete Comment Detection and Update", IEEE Transactions on Software Engineering, 2023. https://ink.library.smu.edu.sg/sis_research/7769/ (CUP² 框架)
- Li et al., "PRESTI: Predicting Repayment Effort of Self-Admitted Technical Debt", arXiv:2309.06020, 2023. https://arxiv.org/html/2309.06020v3
- Google Documentation Best Practices. https://raw.githubusercontent.com/emacsmirror/google-c-style/master/docguide/best_practices.md
- SSW Consulting, "Leave explanatory notes for non-standard code". https://www.ssw.com.au/rules/leave-explanatory-notes-for-non-standard-code
- Hillel Wayne, "The Myth of Self-Documenting Code". https://buttondown.com/hillelwayne/archive/the-myth-of-self-documenting-code
- Microsoft (Jared Parsons), "Code is not self documenting". https://learn.microsoft.com/ga-ie/archive/blogs/jaredpar/code-is-not-self-documenting
- "Code Smell 224 - Deodorant Comments", Level Up Coding (间接引用 Martin Fowler)
- Gradle Javadoc configuration issues — multiple discuss.gradle.org threads
- "Code review antipatterns", Simon Tatham. https://www.chiark.greenend.org.uk/~sgtatham/quasiblog/code-review-antipatterns/

## 本文件对 Roadmap 的指导

基于上述陷阱，Phase 1 之前的 **Phase 0（注释规范定义）** 必须在架构上先于注释实际编写，内容包括：

0. **注释语言策略**：确定中文为主、英文技术术语保留
1. **注释层级**：什么必须注释、什么禁止注释、什么按需注释
2. **TODO 管理**：必须关联 Issue 编号
3. **Javadoc 规范**：使用模板、禁止空骨架、`@param/@return` 必须有描述
4. **Gradle 构建**：配置 `failOnError = false` + 可选严格检查任务
5. **CR 规则**：注释同步检查 + Conventional Comments 前缀

如果跳过 Phase 0 直接进入大规模注释编写，pitfall 4（团队摩擦）和 pitfall 7（语言混乱）将是最先爆发的问题。

---
*Pitfalls research for: 代码注释常见错误（Vue + Spring Boot 已有项目补注释）*
*Researched: 2026-05-07*
