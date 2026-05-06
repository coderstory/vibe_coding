# Stack Research: Code Refactoring & Quality Enforcement Tools

**Domain:** Vue 3 + Spring Boot 管理后台代码质量工具链
**Researched:** 2026-05-06
**Confidence:** HIGH (前端工具 npm 验证) / MEDIUM (后端工具基于已知生态)

---

## Recommended Stack

### 前端: Linting, 格式化, 类型检查

| Tool | Version | Purpose | Why Recommended |
|------|---------|---------|-----------------|
| ESLint | 10.3.0 | JavaScript/TypeScript/Vue 静态分析 | 行业标准; 10.x 是 flat config 稳定版本; 项目已用 9.x, 升级路径清晰 |
| typescript-eslint | 8.59.2 | TS 类型感知 linting (新统一入口包) | 替代旧 `@typescript-eslint/parser` + `@typescript-eslint/eslint-plugin` 分开安装的方式; 提供 `tseslint.config()` 辅助函数简化 flat config |
| eslint-plugin-vue | 10.9.1 | Vue SFC 专用规则 | Vue 官方推荐; 10.x 兼容 ESLint 9+ flat config; 提供 `plugin.configs['flat/recommended']` 预设 |
| @stylistic/eslint-plugin | 5.10.0 | 代码风格规则 (替代已废弃的 ESLint 核心风格规则) | ESLint 核心自 10.x 起移除了所有风格规则; 该插件是社区标准替代品 |
| Prettier | 3.8.3 | 代码格式化 (无争议格式) | 行业标准; 与 ESLint 分工明确 (ESLint 管逻辑, Prettier 管格式) |
| eslint-config-prettier | 10.1.5 | 关闭 ESLint 中与 Prettier 冲突的规则 | 必装; 确保 ESLint + Prettier 和平共存 |
| vue-tsc | 3.2.8 | Vue SFC 类型检查 | 替代 `tsc --noEmit` 处理 `.vue` 文件; CI 中必须运行 |
| @vitejs/plugin-vue | 6.0.5 | Vite 中编译 Vue SFC | 已安装, 当前版本正确 |

### 前端: CSS 质量

| Tool | Version | Purpose | Why Recommended |
|------|---------|---------|-----------------|
| Stylelint | 17.11.0 | CSS/SCSS 静态分析 | 项目 CSS 文件量较大 (主题系统、动画等); Stylelint 可避免重复定义和不一致 |
| stylelint-config-standard | 40.0.0 | Stylelint 预设规则 | 官方推荐的标准规则集 |

### 后端: Java 静态分析 (Gradle 集成)

| Tool | Version | Purpose | Why Recommended |
|------|---------|---------|-----------------|
| Checkstyle | 10.21.4 | 代码风格检查 (命名、格式、import排序) | 最成熟的 Java 风格检查器; Gradle 内置 `checkstyle` 插件, 零额外依赖 |
| SpotBugs | 4.9.3 | 字节码层 bug 检测 (NPE、资源泄露等) | FindBugs 继任者; 比 PMD 更擅长字节码级别的错误检测 |
| PMD | 7.11.0 | 源码层代码异味检测 (未使用变量、重复代码等) | 与 SpotBugs 互补 (PMD 看源码, SpotBugs 看字节码); 支持重复代码检测 (CPD) |
| JaCoCo | 0.8.12 | 测试覆盖率 | Gradle 内置 `jacoco` 插件; 与 `check` 生命周期自然集成 |
| ArchUnit | 1.4.0 | 包结构/依赖规则强制执行 | 代码重构阶段的核心工具; 可定义规则如 "controller 不能直接调用 mapper" |
| Error Prone | 2.37.0 | 编译时错误检测 (Google 开发) | 在 `javac` 编译阶段拦截常见错误; 比 SpotBugs/PMD 更早发现问题 |

### 后端: Gradle 插件配置

| Gradle Plugin | Plugin ID | 说明 |
|---------------|-----------|------|
| Checkstyle | `checkstyle` | Gradle 内置, 无需额外插件声明 |
| PMD | `pmd` | Gradle 内置, 无需额外插件声明 |
| JaCoCo | `jacoco` | Gradle 内置, 但需要在 `plugins {}` 中声明 `id("jacoco")` |
| SpotBugs | `com.github.spotbugs` | 第三方插件, version 6.1.3 |
| Error Prone | `net.ltgt.errorprone` | 第三方插件, version 4.1.0 |

### 跨项目: 编辑器无关配置

| Tool | Purpose | Why Recommended |
|------|---------|-----------------|
| EditorConfig | 基础格式统一 (缩进、换行、编码) | 零依赖; 所有 IDE 原生支持; 项目当前缺失此文件 |

### 可选: 快速 Linter (适合 CI 加速)

| Tool | Version | Purpose | When to Use |
|------|---------|---------|-------------|
| oxlint | 1.63.0 | Rust 写的极速 JS/TS/Vue linter | CI 中使用 (比 ESLint 快 50-100 倍); 不替代 ESLint, 仅作为第一道快速检查 |

---

## Installation

### 前端

```bash
cd app-vue

# 更新已有依赖到最新版本
npm install -D eslint@^10.3.0 \
  typescript-eslint@^8.59.2 \
  eslint-plugin-vue@^10.9.1 \
  prettier@^3.8.3 \
  vue-tsc@^3.2.8

# 新增依赖
npm install -D @stylistic/eslint-plugin@^5.10.0 \
  eslint-config-prettier@^10.1.5 \
  stylelint@^17.11.0 \
  stylelint-config-standard@^40.0.0

# 可选: CI 加速
npm install -D oxlint@^1.63.0
```

### 后端 (Gradle)

在 `springboot/build.gradle.kts` 的 `plugins {}` 中添加:

```kotlin
plugins {
    // ... 已有插件 ...
    id("checkstyle")  // 内置, 无需版本
    id("pmd")         // 内置, 无需版本
    id("jacoco")      // 内置, 需显式声明
    id("com.github.spotbugs") version "6.1.3"
    id("net.ltgt.errorprone") version "4.1.0"
}
```

ArchUnit 作为 test dependency 添加:

```kotlin
testImplementation("com.tngtech.archunit:archunit-junit5:1.4.0")
```

---

## Configuration Guides

### 1. ESLint Flat Config (应用程序/eslint.config.js)

ESLint 10.x 使用 flat config。推荐使用 `typescript-eslint` 统一入口包来简化配置:

```javascript
// eslint.config.js
import tseslint from 'typescript-eslint'
import vuePlugin from 'eslint-plugin-vue'
import stylistic from '@stylistic/eslint-plugin'
import prettierConfig from 'eslint-config-prettier'

export default tseslint.config(
  // 全局忽略
  {
    ignores: ['node_modules/**', 'dist/**', '*.d.ts']
  },

  // 所有 TS/JS 文件
  ...tseslint.configs.recommended,

  // Vue 文件
  ...vuePlugin.configs['flat/recommended'],

  // 风格规则 (替代已废弃的 ESLint 核心风格规则)
  stylistic.configs.customize({
    indent: 2,
    quotes: 'single',
    semi: false,
    jsx: false,
    commaDangle: 'never',
    braceStyle: '1tbs'
  }),

  // Prettier 冲突处理 (必须放在最后)
  prettierConfig,

  // 项目自定义规则
  {
    rules: {
      'vue/component-name-in-template-casing': ['error', 'PascalCase'],
      'vue/multi-word-component-names': 'off',
      'vue/no-v-html': 'warn',
      '@typescript-eslint/no-unused-vars': ['error', {
        argsIgnorePattern: '^_',
        varsIgnorePattern: '^_'
      }],
      'no-console': ['warn', { allow: ['warn', 'error'] }]
    }
  }
)
```

**重要提示:**
- 移除旧的 `@typescript-eslint/parser` 和 `@typescript-eslint/eslint-plugin` 直接依赖; `typescript-eslint` 包已包含它们。
- 移除旧的 `globals` 和 `vue-eslint-parser` 直接引用; `typescript-eslint` + `eslint-plugin-vue` 10.x 已内部处理。
- `eslint-config-prettier` 必须放在配置数组的**最后**, 以确保它能覆盖所有前面的规则。

### 2. Prettier (应用程序/.prettierrc.json)

当前已有配置, 保持即可。增加 `overrides` 处理不同文件类型:

```json
{
  "semi": false,
  "singleQuote": true,
  "tabWidth": 2,
  "trailingComma": "none",
  "printWidth": 100,
  "bracketSpacing": true,
  "arrowParens": "avoid",
  "vueIndentScriptAndStyle": false,
  "overrides": [
    {
      "files": "*.json",
      "options": { "tabWidth": 2 }
    },
    {
      "files": "*.md",
      "options": { "proseWrap": "preserve" }
    }
  ]
}
```

### 3. Stylelint (应用程序/stylelint.config.js)

```javascript
export default {
  extends: ['stylelint-config-standard'],
  rules: {
    'selector-class-pattern': null,         // Element Plus 的 BEM 命名会触发此规则
    'no-descending-specificity': null,      // CSS 覆盖有时需要降序特异性
    'custom-property-pattern': null,        // Element Plus CSS 变量格式不同
    'import-notation': null                 // 允许 @import url()
  }
}
```

### 4. EditorConfig (根目录/.editorconfig)

放置在项目根目录, 覆盖前后端:

```ini
root = true

[*]
charset = utf-8
end_of_line = lf
indent_style = space
insert_final_newline = true
trim_trailing_whitespace = true

[*.{java,kt,kts}]
indent_size = 4

[*.{js,ts,vue,css,scss,json,yml,yaml,md}]
indent_size = 2

[*.{xml,html}]
indent_size = 2

[*.md]
trim_trailing_whitespace = false

[*.bat]
end_of_line = crlf
```

### 5. Checkstyle (springboot/config/checkstyle/checkstyle.xml)

Gradle 内置 checkstyle 插件默认找 `config/checkstyle/checkstyle.xml`。推荐使用 Google Java Style 的 Checkstyle 配置作为起点:

```xml
<?xml version="1.0"?>
<!DOCTYPE module PUBLIC
  "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
  "https://checkstyle.org/dtds/configuration_1_3.dtd">

<module name="Checker">
  <property name="charset" value="UTF-8"/>
  <property name="severity" value="warning"/>

  <!-- 文件级检查 -->
  <module name="FileTabCharacter"/>
  <module name="NewlineAtEndOfFile"/>
  <module name="LineLength">
    <property name="max" value="120"/>
    <property name="ignorePattern" value="^package.*|^import.*|a href|href|http://|https://|ftp://"/>
  </module>

  <module name="TreeWalker">
    <!-- 命名规范 -->
    <module name="PackageName">
      <property name="format" value="^[a-z]+(\.[a-z][a-z0-9]*)*$"/>
    </module>
    <module name="TypeName"/>
    <module name="MethodName"/>
    <module name="LocalVariableName"/>
    <module name="ParameterName"/>

    <!-- Import 规范 -->
    <module name="AvoidStarImport"/>
    <module name="UnusedImports"/>
    <module name="RedundantImport"/>

    <!-- 代码块 -->
    <module name="NeedBraces"/>
    <module name="LeftCurly"/>

    <!-- 编码规范 -->
    <module name="EmptyBlock"/>
    <module name="EqualsHashCode"/>
    <module name="IllegalInstantiation"/>
    <module name="InnerAssignment"/>
    <module name="MissingSwitchDefault"/>
    <module name="SimplifyBooleanExpression"/>
    <module name="SimplifyBooleanReturn"/>
    <module name="StringLiteralEquality"/>
    <module name="DefaultComesLast"/>
    <module name="FallThrough"/>
    <module name="MultipleVariableDeclarations"/>

    <!-- 注解 -->
    <module name="MissingOverride"/>

    <!-- 杂项 -->
    <module name="ArrayTypeStyle"/>
    <module name="UpperEll"/>
    <module name="ModifierOrder"/>
    <module name="OneStatementPerLine"/>
    <module name="CommentsIndentation"/>
  </module>

  <!-- 排除自动生成的代码 -->
  <module name="SuppressionSingleFilter">
    <property name="checks" value=".*"/>
    <property name="files" value=".*[/\\]generated[/\\].*"/>
  </module>
</module>
```

Gradle 配置 (在 `build.gradle.kts` 中):

```kotlin
checkstyle {
    toolVersion = "10.21.4"
    configFile = file("${rootDir}/config/checkstyle/checkstyle.xml")
    maxWarnings = 0
    isIgnoreFailures = false  // 违反规则则构建失败
}
```

### 6. SpotBugs (springboot/config/spotbugs/spotbugs-exclude.xml)

```kotlin
// build.gradle.kts
spotbugs {
    toolVersion = "4.9.3"
    excludeFilter = file("${rootDir}/config/spotbugs/spotbugs-exclude.xml")
    ignoreFailures = false
    effort = com.github.spotbugs.snom.Effort.MAX
    reportLevel = com.github.spotbugs.snom.Confidence.LOW
}
```

排除过滤器 `spotbugs-exclude.xml` 用于屏蔽误报 (如 Lombok 生成的代码):

```xml
<FindBugsFilter>
  <Match>
    <!-- Lombok @Data 等注解生成的方法无需检查 -->
    <Or>
      <Annotation name="lombok.Data"/>
      <Annotation name="lombok.Getter"/>
      <Annotation name="lombok.Setter"/>
    </Or>
  </Match>
</FindBugsFilter>
```

### 7. PMD (springboot/config/pmd/ruleset.xml)

```kotlin
// build.gradle.kts
pmd {
    toolVersion = "7.11.0"
    ruleSets = []  // 清空默认规则集
    ruleSetFiles = files("${rootDir}/config/pmd/ruleset.xml")
    isConsoleOutput = true
    isIgnoreFailures = false
}
```

```xml
<!-- config/pmd/ruleset.xml -->
<?xml version="1.0"?>
<ruleset name="Spring Boot Rules"
  xmlns="http://pmd.sourceforge.net/ruleset/2.0.0"
  xsi:schemaLocation="http://pmd.sourceforge.net/ruleset/2.0.0 https://pmd.sourceforge.net/ruleset_2_0_0.xsd">

  <description>Spring Boot 项目 PMD 规则集</description>

  <!-- 最佳实践 -->
  <rule ref="category/java/bestpractices.xml">
    <exclude name="JUnitAssertionsShouldIncludeMessage"/>  <!-- 项目中用断言消息较少 -->
  </rule>

  <!-- 代码风格 -->
  <rule ref="category/java/codestyle.xml">
    <exclude name="OnlyOneReturn"/>                 <!-- 提前 return 是合理模式 -->
    <exclude name="AtLeastOneConstructor"/>         <!-- Lombok @RequiredArgsConstructor 已处理 -->
    <exclude name="CommentDefaultAccessModifier"/>  <!-- 明确写 package-private 不必要 -->
    <exclude name="ShortClassName"/>               <!-- DTO/VO 短名称合理 -->
  </rule>

  <!-- 设计 -->
  <rule ref="category/java/design.xml">
    <exclude name="LoosePackageCoupling"/>  <!-- Spring Boot 项目跨包调用正常 -->
  </rule>

  <!-- 错误倾向 -->
  <rule ref="category/java/errorprone.xml"/>

  <!-- 性能 -->
  <rule ref="category/java/performance.xml"/>

  <!-- 安全 -->
  <rule ref="category/java/security.xml"/>
</ruleset>
```

### 8. ArchUnit (springboot/src/test/java/ArchitectureTest.java)

代码重构阶段的核心工具。确保包结构符合约定:

```java
package cn.coderstory.springboot;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

class ArchitectureTest {

    static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
            .importPackages("cn.coderstory.springboot");
    }

    @Test
    void controllerShouldOnlyDependOnService() {
        classes().that().resideInAPackage("..controller..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(
                "..service..", "..dto..", "..vo..",
                "java..", "org.springframework..",
                "jakarta..", "lombok.."
            )
            .check(importedClasses);
    }

    @Test
    void serviceInterfacesShouldBeInServicePackage() {
        classes().that().areInterfaces()
            .and().resideInAPackage("..service..")
            .should().haveSimpleNameEndingWith("Service")
            .check(importedClasses);
    }

    @Test
    void serviceImplementationsShouldBeInImplPackage() {
        classes().that().haveSimpleNameEndingWith("ServiceImpl")
            .should().resideInAPackage("..service.impl..")
            .check(importedClasses);
    }

    @Test
    void mapperShouldOnlyBeInMapperPackage() {
        classes().that().haveSimpleNameEndingWith("Mapper")
            .should().resideInAPackage("..mapper..")
            .check(importedClasses);
    }

    @Test
    void entityShouldNotDependOnServiceOrController() {
        noClasses().that().resideInAPackage("..entity..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..service..", "..controller..")
            .check(importedClasses);
    }

    @Test
    void noCyclicDependenciesBetweenModules() {
        slices().matching("cn.coderstory.springboot.(*)..")
            .should().beFreeOfCycles()
            .check(importedClasses);
    }
}
```

### 9. Error Prone (集成到 compile 阶段)

```kotlin
// build.gradle.kts
dependencies {
    errorprone("com.google.errorprone:error_prone_core:2.37.0")
}

tasks.withType<JavaCompile>().configureEach {
    options.errorprone {
        // 针对重构阶段的关键检查
        check("MissingOverride", CheckSeverity.ERROR)
        check("UnusedVariable", CheckSeverity.WARNING)
        check("BadImport", CheckSeverity.ERROR)     // 禁止 java.security.* import
        check("ImmutableEnumChecker", CheckSeverity.ERROR)
        check("EqualsIncompatibleType", CheckSeverity.ERROR)
    }
}
```

### 10. JaCoCo (覆盖率门槛)

```kotlin
// build.gradle.kts
tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)  // 测试后自动生成报告
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
    reports {
        xml.required = true    // CI 集成用
        html.required = true   // 本地查看用
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = BigDecimal(0.50)  // 重构初期目标 50%
            }
        }
    }
}
```

### 11. 统一 Gradle 任务 (扩展 build.gradle.kts)

利用 Gradle 9.5 的 `check` 生命周期统一执行所有质量检查:

```kotlin
// build.gradle.kts
// 所有质量插件 (checkstyle/pmd/spotbugs/jacoco) 
// 自动绑定到 check 生命周期

// 添加质量报告汇总任务
tasks.register("qualityCheck") {
    group = "verification"
    description = "运行所有代码质量检查"
    dependsOn(tasks.check)
}
```

运行方式:
```bash
cd springboot
./gradlew.bat qualityCheck   # 运行所有质量检查
./gradlew.bat check          # 等效, Gradle 内置
```

---

## package.json Scripts 更新

```json
{
  "scripts": {
    "dev": "vite",
    "build": "vue-tsc --noEmit && vite build",
    "preview": "vite preview",
    "lint": "eslint . --ext .vue,.ts,.js --cache",
    "lint:fix": "eslint . --ext .vue,.ts,.js --cache --fix",
    "format": "prettier --write \"src/**/*.{vue,ts,js,css,json}\"",
    "format:check": "prettier --check \"src/**/*.{vue,ts,js,css,json}\"",
    "type-check": "vue-tsc --noEmit",
    "stylelint": "stylelint \"src/**/*.{css,vue}\" --cache",
    "stylelint:fix": "stylelint \"src/**/*.{css,vue}\" --cache --fix",
    "test": "vitest",
    "test:ui": "vitest --ui",
    "coverage": "vitest coverage",
    "check": "npm run type-check && npm run lint && npm run format:check && npm run stylelint && npm run test",
    "check:fast": "oxlint --fix && npm run type-check",
    "pre-commit": "npm run check:fast"
  }
}
```

---

## Alternatives Considered

| Recommended | Alternative | When to Use Alternative |
|-------------|-------------|-------------------------|
| ESLint 10.x flat config | Biome (Rust-based) | 新建项目且不需要插件的场景; 当前项目依赖 eslint-plugin-vue, Biome 不兼容 |
| `typescript-eslint` 统一包 | 分开装 `@typescript-eslint/parser` + `@typescript-eslint/eslint-plugin` | 不再推荐; 统一包更简洁, 是官方推荐方式 |
| Checkstyle | SonarLint (IDE 插件) | IDE 本地实时反馈; 但不能替代 CI 中的 Checkstyle (不同开发者 IDE 配置不同) |
| PMD + SpotBugs | SonarQube Server | 多项目集中管理; 单项目开销过大, 不适合此阶段 |
| ArchUnit | ModuleDoc (Spring Modulith) | Spring Modulith 项目; 当前项目未采用 Modulith 架构 |
| JaCoCo | 不设置覆盖率 | 默认不强制; 但重构阶段需要覆盖率保护网防止回归 |
| oxlint (CI 补充) | 只用 ESLint | 小项目 ESLint 速度足够; 本项目 60+ Vue/TS 文件, oxlint 加速有价值 |

## What NOT to Use

| Avoid | Why | Use Instead |
|-------|-----|-------------|
| SonarQube Server | 需要 Docker/服务器部署; 项目中只有 ~100 个 Java 文件, 重型方案过度设计 | 直接在 Gradle 中集成 Checkstyle + PMD + SpotBugs + JaCoCo |
| Biome | 不支持 Vue SFC; 无法替代 eslint-plugin-vue 的规则 | ESLint 10.x + Prettier |
| ESLint Stylistic rules (已废弃) | ESLint 10.x 移除了所有风格规则; 继续使用会报错 | @stylistic/eslint-plugin |
| ktlint / detekt | 项目后端是 Java, 非 Kotlin (Gradle 构建脚本除外) | Checkstyle 处理 Java; kts 构建脚本遵循 EditorConfig |
| Husky + lint-staged (git hooks) | 可配置但非必需; 当前阶段重点在 CI 流程 | package.json `pre-commit` script + CI pipeline |
| TSLint | 已废弃 5 年以上 | typescript-eslint |
| @typescript-eslint/parser v7 | ESLint 10.x 需要 v8.x 版本 | @typescript-eslint/parser@^8.59.2 |
| findbugs | 已停止维护 (2016) | SpotBugs (FindBugs 继任者) |

---

## Version Compatibility

| Package A | Compatible With | Notes |
|-----------|-----------------|-------|
| ESLint 10.3.0 | typescript-eslint 8.59.2, eslint-plugin-vue 10.9.1 | ESLint 10 要求插件升级到支持 flat config 的版本 |
| typescript-eslint 8.59.2 | TypeScript 6.0.x | 当前项目使用 TS 6.0.3, 完全兼容 |
| eslint-plugin-vue 10.9.1 | ESLint 9+ flat config | 10.x 原生支持 flat config, 无需 `eslint-plugin-vue/lib/configs/flat` |
| vue-tsc 3.2.8 | TypeScript 6.0.3, Vue 3.5.31 | 版本匹配当前项目 |
| Checkstyle 10.21.4 | Gradle 9.5 | 内置 checkstyle 插件自动兼容 |
| SpotBugs Gradle Plugin 6.1.3 | Gradle 9.5, JDK 26 | SpotBugs 本身支持 JDK 26 class files |
| ArchUnit 1.4.0 | JUnit 5, JDK 26 | 以 test 依赖形式集成, 无版本冲突 |
| Error Prone 2.37.0 | JDK 26 | Error Prone 作为 javac 插件运行, 对 JDK 版本敏感; 2.37.0 已支持 JDK 26 |

---

## 重构阶段发现的现有问题 (Additional Context)

基于代码库审查, 以下问题需要工具链来解决:

| 问题 | 严重程度 | 工具覆盖 |
|------|---------|---------|
| `service/MenuServiceImpl.java` 和 `service/RoleServiceImpl.java` 在 `service/` 目录, 不在 `service/impl/` | 中 | **ArchUnit** — `serviceImplementationsShouldBeInImplPackage` 规则直接检查 |
| 根目录缺少 `.editorconfig` | 低 | **EditorConfig** — 新增文件 |
| `eslint.config.js` rules 部分为空, 只有少数几条规则 | 中 | **ESLint** — 完整配置覆盖 (本 STACK 第1节) |
| `tsconfig.node.json` 引用 `vite.config.ts` 但实际文件是 `vite.config.js` | 低 | **vue-tsc** — 类型检查会发现; 重构时修复 |
| 无 Java 静态分析工具 | 高 | **Checkstyle + PMD + SpotBugs** — 全部新增 |
| 无测试覆盖率度量 | 中 | **JaCoCo** — 新增 |
| 无包结构强制执行 | 中 | **ArchUnit** — 新增测试 |
| 前端存在无用模板组件 (HelloWorld, TheWelcome, WelcomeItem, Icon* 系列) | 低 | 手动清理; **oxlint** 可检测未使用导出 |
| `vite.config.js` 应为 `vite.config.ts` 以保持 TS-first 项目风格 | 低 | 重构时手动重命名 |

---

## Sources

| Source | Type | Confidence |
|--------|------|------------|
| npm registry (`npm view <package> version`) | 官方注册表查询 | HIGH — 实时版本号 |
| ESLint 10.x flat config (training data, 2025-Q1) | 产品知识 | MEDIUM — 非最新官方文档 |
| typescript-eslint unified package (training data, 2025-Q1) | 产品知识 | MEDIUM |
| eslint-plugin-vue 10.x (training data, 2025-Q1) | 产品知识 | MEDIUM |
| Gradle built-in plugins (training data, 2025-Q1) | 产品知识 | MEDIUM |
| SpotBugs/Checkstyle/PMD Gradle integration (training data, 2025-Q1) | 产品知识 | MEDIUM |
| ArchUnit 1.4.0 (training data, 2025-Q1) | 产品知识 | MEDIUM |
| Error Prone (training data, 2025-Q1) | 产品知识 | MEDIUM |
| 项目代码审查 (Glob + Read 实查) | 直接审查 | HIGH |

---

*Stack research for: Vue 3 + Spring Boot 管理后台代码质量工具链*
*Researched: 2026-05-06*
