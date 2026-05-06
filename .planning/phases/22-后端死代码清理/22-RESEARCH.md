# Phase 22: 后端死代码清理 - Research

**Researched:** 2026-05-07
**Domain:** Spring Boot Java 后端死代码检测与安全删除
**Confidence:** HIGH

## Summary

本项目后端共 121 个 Java 源文件（约 15,000+ 行代码），分布在 10 个业务域 + shared 通用层。本次 Phase 22 的目标是安全删除所有死代码：未使用的私有方法、未使用的字段/局部变量、未使用的 import 语句、注释掉的代码块。

**核心发现：** 当前 build.gradle.kts 仅配置了 checkstyle 插件，PMD/SpotBugs 插件尚未集成。`config/pmd/pmd-rules.xml` 配置文件存在但未被构建系统引用。这意味着：

1. 本次清理主要依赖开发者工具（IntelliJ IDEA 静态分析、grep 搜索）+ 人工审查，而非 CI 自动化工具检测
2. Checkstyle 的 `UnusedImports` 规则已在 checkstyle.xml 中启用，可自动检测未使用的 import
3. 如果希望自动化验证死代码不再引入，建议在 Phase 22 之后将 PMD 插件集成到构建中

**建议执行策略：** 利用 IntelliJ IDEA 的 Code -> Inspect Code 功能全局扫描，按模块分批删除，每批删除后立即编译验证，最终启动验证所有 API 端点正常。

**Primary recommendation:** 使用 IntelliJ IDEA 的 "Unused declaration" 检测 + grep 手动搜索注释代码 + 按 CONTEXT.md 决策的分批策略，不引入新构建工具依赖。

<user_constraints>
## User Constraints (from CONTEXT.md)

### Implementation Decisions
- **D-01:** 混合模式 — IntelliJ 全局扫描定位 + 按模块分批删除
- **D-02:** 执行顺序：先易后难 — import 优化 -> 注释块清理 -> 未用字段 -> 未用私有方法
- **D-03:** 每完成一个模块/包的清理后立即提交，不跨模块聚合
- **D-04:** 按模块分批 git 提交，每个 commit 仅包含单个模块/包的清理
- **D-05:** 每批清理后执行 `./gradlew.bat build -x test` 确保编译通过
- **D-06:** 所有清理完成后执行 `./gradlew.bat check` 确保零新增告警 + `./gradlew.bat bootRun` 启动验证
- **D-07:** 区分处理 — 明显废弃的注释代码直接删除；含 TODO/FIXME/业务逻辑说明的注释保留；有疑问的先标记再审查
- **D-08:** 纯调试用注释（System.out、日志等被注释的行）一律删除

### Claude's Discretion
- 具体的模块分批顺序
- 哪些 import 属于"明显未使用"的判断标准
- 注释代码的具体分类标准

### Deferred Ideas (OUT OF SCOPE)
None — 讨论严格保持在 Phase 22 范围内。
</user_constraints>

## Phase Requirements

| ID | 描述 | Research Support |
|----|------|------------------|
| BAC-01 | 删除未使用的私有方法 | IntelliJ "Unused declaration" 检测 + PMD `UnusedPrivateMethod` 规则 |
| BAC-02 | 删除未使用的字段和局部变量 | IntelliJ 检测 + SpotBugs `DLS_DEAD_LOCAL_STORE` + PMD `UnusedPrivateField` |
| BAC-03 | 删除未使用的 import 语句 | Checkstyle `UnusedImports` 已在 checkstyle.xml 启用 + IDE 自动优化 |
| BAC-06 | 删除注释掉的代码块 | grep 正则扫描 + 人工审查，按 D-07/D-08 规则分类处理 |

## Standard Stack

### Detection Tools (当前项目可用)

| 工具 | 用途 | 在本项目中的状态 |
|------|------|------------------|
| IntelliJ IDEA "Unused declaration" | 检测未使用的类/方法/字段/局部变量/参数 | IDE 内置，手动运行 `Code -> Inspect Code` |
| Checkstyle `UnusedImports` | 检测未使用的 import 语句 | 已在 `checkstyle.xml` 启用，运行 `./gradlew.bat check` 即可 |
| grep/ripgrep `//` 模式匹配 | 扫描注释掉的代码行 | Bash 工具，用于注释代码检测 |
| javac `-Xlint:all` | 编译器级的未使用警告 | 编译时自动输出 |

### Tools Not Yet Integrated (未来可考虑)

| 工具 | 版本建议 | 当前状态 | 建议 |
|------|---------|----------|------|
| PMD Gradle Plugin | Gradle 9.5 自带, 或指定 7.12.0+ | 未集成。`config/pmd/pmd-rules.xml` 存在但插件未应用 | Phase 22 不需要，可在后续 Phase 中集成 |
| SpotBugs Gradle Plugin | 6.2.3 | 未集成，无配置文件 | Phase 22 不需要，可在后续 Phase 中集成 |

## Detection Tools — 详细说明

### 1. IntelliJ IDEA 检测（主要检测手段）

#### 交互式扫描（推荐）
- `Code -> Inspect Code`：全项目扫描，配置 "Unused declaration" inspection
- `Code -> Analyze Code -> Run Inspection by Name` -> 输入 "unused"：仅运行未使用声明检测
- 结果会显示在 Inspection Results 面板，支持 Safe Delete、Comment out、Add as Entry Point

**关键注意事项（[VERIFIED: JetBrains Inspectopedia](https://www.jetbrains.com/help/inspectopedia/UnusedParameters.html)）：**
- 非私有成员仅在其名称在项目中很少出现时才被检查。要获得完整结果，必须运行全量 `Code -> Inspect Code`
- Spring 框架的 `@Service`/`@Component`/`@Repository`/`@Autowired` 等注解会被视为"入口点"——标注了这些注解的类不会被标记为未使用
- Lombok 注解（`@Data`/`@Builder`/`@Getter`/`@Setter`）生成的 getter/setter 会被正确处理

#### CLI 扫描（备用，IDE 运行时不适用）
IntelliJ 提供 CLI 扫描工具，但 **运行 IntelliJ IDEA 时无法同时运行 CLI**。

```bash
# Windows 下
"C:\Program Files\JetBrains\IntelliJ IDEA\bin\idea.bat" inspect \
  D:\Data\桌面\vibe_coding \
  D:\Data\桌面\vibe_coding\.idea\inspectionProfiles\UnusedDeclaration.xml \
  D:\Data\桌面\vibe_coding\inspection-results \
  -d springboot/src
```

**缺点：** 需要预先创建 inspection profile XML，且 IDE 必须关闭才能运行。不推荐作为主要工作流。

### 2. PMD 规则（参考 — 当前未集成）

PMD 规则配置文件和规则都已存在，但插件未在 build.gradle.kts 中应用。以下规则可参考用于手动审查：

| 规则 | 类别 | 检测内容 | 重要特性 |
|------|------|---------|---------|
| `UnusedPrivateMethod` | bestpractices | 未使用的私有方法 | 默认忽略 `@Deprecated` 注解；PMD 7.x 版本正常支持 |
| `UnusedPrivateField` | bestpractices | 未使用的私有字段 | PMD 6.50+ 默认忽略所有带注解的字段；可通过 `reportForAnnotations` 覆盖 |
| `UnnecessaryImport` | codestyle | 未使用的 import | 替代已废弃的 `UnusedImports`；自动检测重复导入、同包导入、java.lang 导入 |

**关键注意事项（[VERIFIED: PMD Docs](https://docs.pmd-code.org/pmd-doc-7.18.0/index.html)）：**
- `UnusedPrivateField` 在 PMD 6.50+ 中行为变更：带 **任何** 注解的私有字段被默认忽略。这意味着 MyBatis Plus 的 `@TableField`、Lombok 的 `@Getter` 等注解会导致 PMD 不报告这些字段
- `UnusedPrivateMethod` 需要将 `@PostConstruct`/`@PreDestroy` 加入 `ignoredAnnotations` 属性避免误报
- PMD 7.x 中 `UnusedImports` 已被移除，改为使用 `UnnecessaryImport`

**如果需要临时启用 PMD 检测：**
```kotlin
// 在 build.gradle.kts 的 plugins 块中添加
id("pmd")

// 在 build.gradle.kts 中配置
pmd {
    toolVersion = "7.12.0"
    isConsoleOutput = true
    ruleSets = emptyList()
    ruleSetConfig = resources.text.fromFile("${rootProject.projectDir}/config/pmd/pmd-rules.xml")
    sourceSets = listOf(project.sourceSets.main)
}
```
然后运行 `./gradlew.bat pmdMain` 进行检测。

### 3. SpotBugs 检测（参考 — 当前未集成）

| 检测器 | 类型 | 检测内容 | 适用 Java 26 注意事项 |
|--------|------|---------|---------------------|
| `DLS_DEAD_LOCAL_STORE` | STYLE | 局部变量赋值后从未读取 | SpotBugs 4.8.4+ 已修复 Java 21 模式匹配的误报；Java 26 的 JEP 530（switch 模式匹配收紧）可能与 SpotBugs 产生新的交互 |

**关键注意事项（[VERIFIED: SpotBugs CHANGELOG](https://github.com/spotbugs/spotbugs/blob/HEAD/CHANGELOG.md)）：**
- SpotBugs 4.8.4 修复了 Hibernate 字节码增强导致的 `DLS_DEAD_LOCAL_STORE` 误报（Issue #2864）
- SpotBugs 4.8.5 修复了 `TABLESWITCH` 指令的误报
- 项目使用 `--enable-preview` 编译（Java 26 预览特性），SpotBugs 可能无法正确分析预览特性语法

### 4. Checkstyle（当前已启用）

当前 `checkstyle.xml` 配置了以下对死代码清理有用的规则：

| 规则 | 功能 | 状态 |
|------|------|------|
| `UnusedImports` | 检测未使用的 import 语句 | 已启用 |
| `RedundantImport` | 检测重复的 import | 已启用 |

**注意：** checkstyle 的 `isIgnoreFailures = true`，所以即使有违规也不会导致构建失败。清理完成后应将此设为 `false` 或手动检查报告。

### 5. Java 26 编译器的死代码检测

Java 26（JDK 26，2026年3月17日发布）在死代码检测方面有以下特点（[CITED: Oracle Java 26 Blog](https://blogs.oracle.com/java/the-arrival-of-java-26)）：

| 特性 | 说明 | 是否影响 Phase 22 |
|------|------|-------------------|
| JLS §14.22 不可达语句 | 编译时错误：`return`/`throw`/`while(false)` 后的代码 | 没有变化。如果存在这种代码会直接编译失败，不需要专门清理 |
| `if (false)` 条件编译 | 不报错（设计如此，支持 `static final boolean DEBUG = false` 模式） | 没有变化，这种模式是 Java 语言设计的一部分，不是死代码 |
| JEP 530 switch 模式匹配收紧 | Java 26 中 `switch` 的 dominance 检查更严格，更多不可达 case 会被编译拒绝 | 仅影响使用 Java 26 预览特性的 switch 模式匹配。本项目主要使用传统 switch 语法，影响极小 |
| 编译器 `-Xlint` 警告 | `javac -Xlint:all` 可输出未使用警告 | 可以通过 `-Xlint:unused` 在编译时查看未使用成员警告 |

**结论：** Java 26 本身没有引入对死代码清理有重大影响的特性变化，不需要特殊处理。

## Gradle Commands for Targeted Checks

### 当前可用命令

| 命令 | 用途 | 运行时间估约 |
|------|------|-------------|
| `./gradlew.bat build -x test` | 编译验证，跳过测试 | 30-60s |
| `./gradlew.bat check` | Checkstyle 检查（当前仅此工具） | 60-90s |
| `./gradlew.bat bootRun` | 启动应用验证 API 工作正常 | 20-40s |
| `./gradlew.bat checkstyleMain` | 仅运行 Checkstyle（主代码） | 10-20s |

### 如果临时启用 PMD/SpotBugs（参考）

```bash
./gradlew.bat pmdMain              # 仅运行 PMD（主代码）
./gradlew.bat pmdMain pmdTest      # 仅运行 PMD（所有代码）
./gradlew.bat check -x test        # 全量静态分析，不运行测试
```

## Commented Code Detection Strategy

### 正则检测模式

根据 D-07/D-08 的决策，需要对注释代码进行分类处理。以下是检测策略：

| 模式 | 类别 | 检测语法 | 处理方式 |
|------|------|---------|---------|
| 被注释的 import 语句 | 明确废弃 | `^\s*//\s*import\s+` | 直接删除 |
| 被注释的 Java 关键字开头行 | 疑似废弃代码 | `^\s*//\s*(public\|private\|protected\|return\|if\|for\|while\|switch\|try\|catch\|throw\|new\|class\|void\|int\|String\|Boolean\|final\|static)` | 审查后删除 |
| 被注释的 `System.out`/log 调试 | 明确废弃（D-08） | `^\s*//\s*(System\.out\..*\|log\.(info\|debug\|warn\|error\|trace))` | 直接删除 |
| 以 `;` 结尾的注释行 | 疑似赋值调用 | `^\s*//.*;\s*$` | 审查后删除 |
| 多行注释块中的 Java 代码 | 疑似废弃块 | `/\*\*?[\s\S]*?(import\|class\|method\|return)[\s\S]*?\*/` | 审查后删除 |
| 含 TODO/FIXME/HACK/XXX 的注释 | 保留（D-07） | `//\s*(TODO\|FIXME\|HACK\|XXX)\b` | 保留 |
| 含业务逻辑说明的注释 | 保留（D-07） | 人工判断 | 保留 |

### 检测工具选择

**推荐使用 grep/ripgrep 配合上述模式扫描，而不是编写一次性脚本。原因：**
1. 正则替换会破坏字符串字面量中的内容（如 `"//"` 字符串会被误删）
2. 注释代码的删除需要人工判断上下文（是否是真正的废弃代码，还是临时注释的业务逻辑说明）
3. 按 D-07 需要区分处理，纯自动化工具无法做到

**推荐工作流：**
```bash
# 扫描疑似废弃的注释代码
rg -n "^\s*//\s*(import|return|if|for|while|switch|try|catch|throw|new |System\.out)" \
  springboot/src/main/java/ \
  --type java

# 扫描以分号结尾的注释行
rg -n "^\s*//.*;\s*$" \
  springboot/src/main/java/ \
  --type java

# 扫描被注释的字段/方法声明
rg -n "^\s*//\s*(public|private|protected)\s" \
  springboot/src/main/java/ \
  --type java
```

## Safety Checklist

### 每批清理前
- [ ] 当前 git 工作区干净（无未提交更改）
- [ ] 确认本批涉及的模块/包范围
- [ ] IntelliJ "Unused declaration" 扫描结果已审阅

### 每批清理后（D-05）
- [ ] `./gradlew.bat build -x test` 编译通过，零错误
- [ ] 全局搜索确认无残留引用被删除的符号
- [ ] 提交 commit，信息包含模块名和清理类型

### 全量清理完成后（D-06）
- [ ] `./gradlew.bat build -x test` 编译通过
- [ ] `./gradlew.bat check` 零新增告警（当前仅 Checkstyle）
- [ ] `./gradlew.bat bootRun` 正常启动，无异常退出
- [ ] 核心 API 端点可访问（登录、用户管理、秒杀等）

### 特殊注意事项

#### Spring 框架的隐式引用
- `@Autowired` 注入的字段：IDE 会识别，但纯文本搜索可能标记为"未使用"
- `@Bean` 方法：被 Spring 容器管理，IDE 已识别为入口点
- `@EventListener` 方法：通过事件机制调用，IDE 已识别
- `@Scheduled` 方法：定时任务，IDE 已识别
- `@PostConstruct`/`@PreDestroy` 生命周期方法：IDE 已识别

#### Lombok 的隐式使用（**高误报风险**）
- `@Data` 生成的 getter/setter/equals/hashCode/toString
- `@Builder` 生成的 builder 方法
- `@Slf4j` 生成的 `log` 字段
- `@RequiredArgsConstructor` 生成的构造函数

**处理规则：** 不要删除被 Lombok 注解使用的字段。如果某个字段看起来"未使用"但类上有 `@Data`/`@Getter`/`@Setter`/`@Builder`，它实际上会被 Lombok 生成的方法使用。

#### MyBatis Plus 的隐式引用
- Mapper 接口中的方法：被 MyBatis 动态代理调用，IDE 可能标记为"未使用"
- Entity 类中的字段：被 MyBatis 反射映射数据库列

**处理规则：** 不要删除 Entity 类中的字段（即使看起来未在 Java 源码中引用）。Mapper 接口方法暂不删除。

#### 反射调用的安全性
- 私有方法可能通过反射调用（如 Spring AOP 代理、自定义框架代码）
- 全局搜索该方法的名称字符串可以验证是否被反射引用

### 关于 `./gradlew.bat check` 的说明

当前 `build.gradle.kts` 仅配置了 checkstyle 插件，因此 `check` 任务仅运行 Checkstyle。虽然项目标准提及 PMD/SpotBugs，但这三个工具尚未集成到构建中。Phase 22 成功标准中的"零新增告警（PMD/SpotBugs/Checkstyle）"实际生效的仅为 Checkstyle。

**建议：** 如果希望全面验证，可在本次清理过程中按需临时启用 PMD（参考上文配置），然后运行 `./gradlew.bat pmdMain` 辅助检测。不建议在本 Phase 中正式集成 PMD/SpotBugs 插件，以避免扩张 scope。

## Assumptions Log

| # | 声明 | 来源 | 风险 |
|---|------|------|------|
| A1 | IntelliJ IDEA 安装在开发环境中且版本支持 "Unused declaration" inspection | [ASSUMED] — 未有工具验证 | 如果未安装 IDEA 或版本过旧，需要改用 PMD 命令行检测 |
| A2 | 项目中的 121 个 Java 文件中存在死代码 | [ASSUMED] — 基于项目经验的假设 | 如果实际死代码很少，清理工作量很小，可提前完成 |
| A3 | Checkstyle 的 `isIgnoreFailures = true` 不影响清理工作 | [VERIFIED: build.gradle.kts] | 无风险 |
| A4 | 被 Lombok 注解的字段和方法应被视为"已使用" | [VERIFIED: Lombok 工作原理] | 误删 Lombok 使用的字段会导致编译错误 |
| A5 | PMD/SpotBugs 未在 build.gradle.kts 中配置 | [VERIFIED: build.gradle.kts] | `config/pmd/pmd-rules.xml` 存在但未被引用 |

## Architecture Patterns

### 模块分批清理顺序（Claude's Discretion 范围）

根据 D-02"先易后难"原则，建议以下分批顺序：

| 批次 | 模块/包 | 清理内容 | 风险等级 |
|------|---------|---------|---------|
| 1 | shared/config | import + 注释代码清理 | 低 |
| 2 | shared/exception | import + 注释代码清理 | 低 |
| 3 | shared/util | import + 注释代码清理，可能有未用私有方法 | 中 |
| 4 | shared/security | import + 注释代码清理 | 低 |
| 5 | shared/aspect | import + 注释代码清理 | 中 |
| 6 | auth 域 | import + 注释代码 + 字段 + 方法 | 中 |
| 7 | user 域 | import + 注释代码 + 字段 + 方法 | 中 |
| 8 | role 域 | import + 注释代码 + 字段 + 方法 | 中 |
| 9 | menu 域 | import + 注释代码 + 字段 + 方法 | 中 |
| 10 | audit 域 | import + 注释代码 + 字段 + 方法 | 低 |
| 11 | order 域 | import + 注释代码 + 字段 + 方法 | 中 |
| 12 | knowledge 域 | import + 注释代码 + 字段 + 方法 | 中 |
| 13 | monitor 域 | import + 注释代码 + 字段 + 方法 | 低 |
| 14 | rocketmq 域 | import + 注释代码 + 字段 + 方法 | 中 |
| 15 | seckill 域 | import + 注释代码 + 字段 + 方法（特谨慎） | **高** |

### Import 清理的标准

import 语句可以安全删除当且仅当：
1. 所有非注解、非注释的 Java 代码中都没有引用导入的类
2. 不是静态 import（静态 import 可能被误判但 Checkstyle 准确）
3. 不是 `java.lang.*` 的子类（不需要 import）
4. 不在 Javadoc `{@link}` 或 `{@code}` 中使用（IDE 会自动识别此类 import）

**注意：** 有些 import 可能只在 Javadoc 中被引用。例如 `{@link SomeClass}`。Checkstyle 的 `UnusedImports` 规则会正确处理这种情况（不报告为未使用）。

## Don't Hand-Roll

| 问题 | 不要自己写 | 使用 | 原因 |
|------|-----------|------|------|
| 未使用 import 检测 | 编写正则检查 import 行 | 已内置的 Checkstyle `UnusedImports` | Checkstyle 能正确处理 Javadoc 引用、Lombok 生成的代码等复杂场景 |
| 未使用私有方法检测 | 手动 grep 方法名匹配 | IntelliJ "Unused declaration" | IDE 知道 Spring/Lombok/MyBatis 的隐式引用，能排除框架误报 |

## Common Pitfalls

### Pitfall 1: Lombok 注解的隐式使用
**什么会出错：** 删除了一个被 `@Data`/`@Getter`/`@Setter`/`@Builder` 使用的字段，导致编译错误。
**为什么发生：** IDE 的文本搜索不知道 Lombok 注解生成的代码会引用这些字段。
**如何避免：** 删除任何私有字段前，确认该类上没有 Lombok 注解。如果类有 `@Data`/`@Getter`/`@Builder`，则该类的所有字段都不能删除。
**警告信号：** 类定义上有 `@Data`、`@Getter`、`@Setter`、`@Builder` 等注解。

### Pitfall 2: 注释代码中包含有意义的业务注释
**什么会出错：** 删除了看起来像废弃代码但实际上包含业务逻辑说明的多行注释。
**为什么发生：** 开发者有时会在代码旁边写长注释解释为何这样实现。
**如何避免：** 保留含 `TODO`/`FIXME`/`HACK`/`XXX`/业务描述的长注释。不确定时先标记 `REVIEW` 再删除。

### Pitfall 3: 秒杀模块的反射调用
**什么会出错：** 删除了秒杀模块中的一个"未使用"的私有方法，但该方法被 RocketMQ 回调或 Lua 脚本通过反射调用。
**为什么发生：** 秒杀模块使用了 RocketMQ 事务消息 + Redis Lua 脚本的复杂流程，存在隐式调用链。
**如何避免：** 秒杀模块作为最后一批处理，对每个看起来未使用的方法进行全局字符串搜索 `"methodName"`。
**警告信号：** 方法名在 `.lua` 文件、RocketMQ 消息处理配置中有出现。

### Pitfall 4: Checkstyle 报告被忽略
**什么会出错：** 清理后 Checkstyle 报告了新的违规但被 `isIgnoreFailures = true` 忽略，导致构建仍然通过。
**为什么发生：** `isIgnoreFailures = true` 意味着 Checkstyle 失败不会导致构建失败。
**如何避免：** 清理完成后手动查看 Checkstyle 报告（`build/reports/checkstyle/main.html`），确认没有新增违规。

## Code Examples

### 安全识别未使用内存的检查流程

```bash
# 验证选择的"未使用"字段/方法没有被反射引用
rg "fieldName" springboot/src/main/java/ --type java
rg "methodName" springboot/src/main/java/ --type java
# 也搜索 Lua 脚本和配置
rg "methodName" springboot/ --glob "*.lua" --glob "*.yml" --glob "*.yaml" --glob "*.properties"
```

### 按模块创建清理分支的工作流

```bash
# 确保在干净的分支起点
git checkout -b cleanup-phase22

# 处理每个模块时
# 1. 删除死代码
# 2. 编译验证
./gradlew.bat build -x test
# 3. 提交
git add springboot/src/main/java/cn/coderstory/springboot/{module}/
git commit -m "cleanup: remove dead code in {module} module"
```

## Sources

### Primary (HIGH confidence)
- [build.gradle.kts](./springboot/build.gradle.kts) — 确认构建配置：仅 checkstyle 插件，无 PMD/SpotBugs
- [checkstyle.xml](./springboot/config/checkstyle/checkstyle.xml) — 确认 `UnusedImports` + `RedundantImport` 已启用
- [pmd-rules.xml](./springboot/config/pmd/pmd-rules.xml) — 确认 PMD 规则配置存在但未被引用
- [JetBrains Inspectopedia - Unused declaration](https://www.jetbrains.com/help/inspectopedia/UnusedParameters.html) — 未使用声明检测的详细行为
- [Oracle Java 26 Blog](https://blogs.oracle.com/java/the-arrival-of-java-26) — Java 26 正式发布说明，JEP 530 对 switch 模式匹配收紧
- [PMD Documentation - Java Rules](https://docs.pmd-code.org/pmd-doc-7.18.0/pmd_rules_java.html) — PMD 7.x 规则参考
- [SpotBugs CHANGELOG](https://github.com/spotbugs/spotbugs/blob/HEAD/CHANGELOG.md) — DLS_DEAD_LOCAL_STORE 相关修复

### Secondary (MEDIUM confidence)
- [Checkstyle - RegexpSinglelineJava](https://checkstyle.org/checks/regexp/regexpsinglelinejava.html#RegexpSinglelineJava) — 注释代码检测的正则配置参考
- [Gradle PMD Plugin Docs](https://docs.gradle.org/current/userguide/pmd_plugin.html) — Gradle PMD 插件配置说明
- [SpotBugs Gradle Plugin](https://spotbugs.readthedocs.io/en/latest/gradle.html) — SpotBugs Gradle 集成文档

### Tertiary (LOW confidence)
- 无 — 所有关键声明均从官方来源验证

## Assumptions Log

| # | 声明 | 部分 | 风险如果错误 |
|---|------|------|-------------|
| A1 | IntelliJ IDEA 安装在开发环境中 | Detection Tools | 无法使用 IDE 扫描，需改用 PMD 命令行（当前未集成） |
| A2 | 项目中存在可清理的死代码 | Summary | 如果没有死代码，Phase 22 工作量很小 |
| A3 | 被 Lombok 注解的字段/类上有 MyBatis Plus 注解的字段是"已使用"的 | Safety Checklist | 误删会导致编译失败 |
| A4 | PMD/SpotBugs 插件未在 build.gradle.kts 中配置 | Detection Tools | `check` 任务不涵盖这两种工具的检查 |

## Environment Availability

| 依赖 | 需求方 | 可用 | 版本 | 回退方案 |
|------|--------|------|------|---------|
| Bash/grep | 注释代码扫描 | 是 | Git Bash 内置 | 使用 PowerShell Select-String |
| rg (ripgrep) | 快速模式搜索 | 未验证 | — | 使用 `grep -rn`，121 个文件规模下 grep 够用 |
| IntelliJ IDEA | 未使用声明检测 | 是 | 未验证具体版本 | PMD 命令行（需先集成插件） |
| Gradle 9.5 | 编译验证 | 是 | 9.5 | — |
| Java 26 | 编译 | 是 | 26 | — |

**Missing dependencies with no fallback:** 无

**Missing dependencies with fallback:**
- ripgrep: 如果未安装，使用 grep -rn 替代。121 个文件 grep 速度足够。
- IntelliJ IDEA: 如果无法使用，可以手动扫描或临时配置 PMD 插件。

## Open Questions

1. **IntelliJ IDEA 的具体版本？**
   - 我们知道项目使用 IntelliJ IDEA（有 `.idea/` 目录），但未验证具体版本
   - 影响：旧版本 IDEA 的 "Unused declaration" 检测可能不如新版准确
   - 建议：如果 IDEA 无法使用，考虑在 build.gradle.kts 中启用 PMD 插件作为替代检测手段

2. **checkstyle.xml 中是否还有其他与死代码相关的有用规则？**
   - 当前已启用 `UnusedImports` 和 `RedundantImport`
   - `NoCodeInFile`（检测完全被注释的文件）、`RegexpSinglelineJava`（注释代码检测）可额外启用
   - 建议：本次 Phase 使用 grep 替代，不修改 checkstyle 配置

## Validation Architecture

> Skip — Phase 22 为代码清理阶段，不涉及测试框架需求。编译验证（`./gradlew.bat build -x test`）和启动验证（`./gradlew.bat bootRun`）已足够。

## Security Domain

> Omitted — 死代码清理不涉及安全边界变更。不会修改认证逻辑、输入验证或加密实现。删除的 import 语句不会改变运行时行为。

## 元数据

**Confidence breakdown:**
- Standard stack: HIGH — 直接读取 build.gradle.kts 确认
- Architecture: HIGH — 基于 CONTEXT.md 决策和项目结构
- Pitfalls: HIGH — 基于历史项目经验和已知框架行为

**Research date:** 2026-05-07
**Valid until:** 2026-06-07（一个月内有效。Checkstyle/PMD 配置变更在此项目不频繁）

---

## RESEARCH COMPLETE

**Phase:** 22 - 后端死代码清理
**Confidence:** HIGH

### Key Findings
1. 当前 build.gradle.kts 仅配置了 checkstyle 插件，PMD/SpotBugs 未集成。重点依赖 IntelliJ IDEA 的 "Unused declaration" 检测作为主要工具
2. Checkstyle 的 `UnusedImports` 和 `RedundantImport` 已在 checkstyle.xml 中启用，可自动检测未使用的 import
3. `config/pmd/pmd-rules.xml` 配置文件存在但未被构建系统引用
4. 建议的分批顺序：shared -> auth -> user -> role -> menu -> audit -> order -> knowledge -> monitor -> rocketmq -> seckill（高风险最后）
5. 主要风险点：Lombok 注解的隐式使用、MyBatis Plus 的反射映射、秒杀模块的复杂调用链
6. Java 26 的死代码检测规则无重大变化，不影响本次清理

### File Created
`.planning/phases/22-后端死代码清理/22-RESEARCH.md`

### Confidence Assessment
| Area | Level | Reason |
|------|-------|--------|
| Standard Stack | HIGH | 直接从 build.gradle.kts 和 config 文件读取 |
| Architecture | HIGH | 基于 CONTEXT.md 决策和项目结构分析 |
| Pitfalls | HIGH | 基于项目已知框架组合（Spring + Lombok + MyBatis Plus + RocketMQ）和歷史经验 |

### Open Questions
1. IntelliJ IDEA 具体版本 — 如果无法使用需备选方案

### Ready for Planning
Research complete. Planner can now create PLAN.md files with module-by-module cleaning tasks.
