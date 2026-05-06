---
phase: 22
name: 后端死代码清理
wave: 1
depends_on: []
requirements: [BAC-01, BAC-02, BAC-03, BAC-06]
autonomous: true
files_modified:
  - springboot/src/main/java/cn/coderstory/springboot/shared/config/*.java
  - springboot/src/main/java/cn/coderstory/springboot/shared/exception/*.java
  - springboot/src/main/java/cn/coderstory/springboot/shared/util/*.java
  - springboot/src/main/java/cn/coderstory/springboot/shared/security/*.java
  - springboot/src/main/java/cn/coderstory/springboot/shared/aspect/*.java
  - springboot/src/main/java/cn/coderstory/springboot/auth/**/*.java
  - springboot/src/main/java/cn/coderstory/springboot/user/**/*.java
  - springboot/src/main/java/cn/coderstory/springboot/role/**/*.java
  - springboot/src/main/java/cn/coderstory/springboot/menu/**/*.java
  - springboot/src/main/java/cn/coderstory/springboot/audit/**/*.java
  - springboot/src/main/java/cn/coderstory/springboot/order/**/*.java
  - springboot/src/main/java/cn/coderstory/springboot/knowledge/**/*.java
  - springboot/src/main/java/cn/coderstory/springboot/monitor/**/*.java
  - springboot/src/main/java/cn/coderstory/springboot/rocketmq/**/*.java
  - springboot/src/main/java/cn/coderstory/springboot/seckill/**/*.java
---

# Plan: 后端死代码清理

## Objective

安全删除后端 Java 代码中所有死代码，包括：未使用的 import 语句、注释掉的代码块、未使用的字段/局部变量、未使用的私有方法。编译和测试零回归。

## Tasks

### Task 1: 清理 shared 层（config/exception/util/security/aspect）

<read_first>
- springboot/src/main/java/cn/coderstory/springboot/shared/
- 使用 IntelliJ IDEA 的 Code -> Inspect Code -> "Unused declaration" 检测结果
</read_first>

<action>
对 shared 层 5 个子包执行死代码清理：

1. **config 包** — 清理未使用的 import、注释代码块
2. **exception 包** — 清理未使用的 import、注释代码块
3. **util 包** — 清理未使用的 import、注释块、未用字段、未用私有方法（Util 类通常有未用方法）
4. **security 包** — 清理未使用的 import、注释代码块
5. **aspect 包** — 清理未使用的 import、注释代码块

清理后执行：
```bash
./gradlew.bat build -x test
```

提交：
```bash
git add springboot/src/main/java/cn/coderstory/springboot/shared/
git commit -m "cleanup: remove dead code in shared layer"
```
</action>

<acceptance_criteria>
- [ ] `./gradlew.bat build -x test` 编译通过，零错误
- [ ] shared 层无残留的注释代码块（纯调试用注释 `System.out`/log 被注释的行已删除）
- [ ] import 语句无冗余（Checkstyle UnusedImports 验证通过）
- [ ] 已确认所有 Lombok 注解（@Data/@Builder/@Slf4j 等）对应的字段未被误删
</acceptance_criteria>

---

### Task 2: 清理 auth 域

<read_first>
- springboot/src/main/java/cn/coderstory/springboot/auth/
</read_first>

<action>
清理 auth 域（controller/service 层）：

1. 删除未使用的 import
2. 删除注释掉的代码块
3. 删除未使用的字段和局部变量
4. 删除未使用的私有方法

**注意事项：** auth 涉及 JWT 认证，`JwtAuthenticationFilter` 等安全相关类中的注释可能含业务逻辑说明，按 D-07 保留 TODO/FIXME。

清理后执行：
```bash
./gradlew.bat build -x test
```

提交：
```bash
git add springboot/src/main/java/cn/coderstory/springboot/auth/
git commit -m "cleanup: remove dead code in auth domain"
```
</action>

<acceptance_criteria>
- [ ] `./gradlew.bat build -x test` 编译通过
- [ ] 无 JWT/认证相关的关键注释被误删
- [ ] import 语句无冗余
</acceptance_criteria>

---

### Task 3: 清理 user/role/menu 域

<read_first>
- springboot/src/main/java/cn/coderstory/springboot/user/
- springboot/src/main/java/cn/coderstory/springboot/role/
- springboot/src/main/java/cn/coderstory/springboot/menu/
</read_first>

<action>
清理 user、role、menu 三个域（controller/service/mapper/entity/dto 层）：

1. 删除未使用的 import
2. 删除注释掉的代码块
3. 删除未使用的字段和局部变量
4. 删除未使用的私有方法
5. **特别注意：** Entity 类中的字段通常被 MyBatis Plus 反射使用，不做删除。Mapper 接口方法暂不删除。

清理后执行：
```bash
./gradlew.bat build -x test
```

提交：
```bash
git add springboot/src/main/java/cn/coderstory/springboot/user/
git add springboot/src/main/java/cn/coderstory/springboot/role/
git add springboot/src/main/java/cn/coderstory/springboot/menu/
git commit -m "cleanup: remove dead code in user/role/menu domains"
```
</action>

<acceptance_criteria>
- [ ] `./gradlew.bat build -x test` 编译通过
- [ ] 所有 Entity 字段（被 MyBatis Plus 反射使用）未被删除
- [ ] Mapper 接口方法未被删除
- [ ] import 语句无冗余
</acceptance_criteria>

---

### Task 4: 清理 audit/order/knowledge 域

<read_first>
- springboot/src/main/java/cn/coderstory/springboot/audit/
- springboot/src/main/java/cn/coderstory/springboot/order/
- springboot/src/main/java/cn/coderstory/springboot/knowledge/
</read_first>

<action>
清理 audit、order、knowledge 三个域：

1. 删除未使用的 import
2. 删除注释掉的代码块
3. 删除未使用的字段和局部变量
4. 删除未使用的私有方法

清理后执行：
```bash
./gradlew.bat build -x test
```

提交：
```bash
git add springboot/src/main/java/cn/coderstory/springboot/audit/
git add springboot/src/main/java/cn/coderstory/springboot/order/
git add springboot/src/main/java/cn/coderstory/springboot/knowledge/
git commit -m "cleanup: remove dead code in audit/order/knowledge domains"
```
</action>

<acceptance_criteria>
- [ ] `./gradlew.bat build -x test` 编译通过
- [ ] import 语句无冗余
- [ ] 注释代码块已按 D-07/D-08 分类处理
</acceptance_criteria>

---

### Task 5: 清理 monitor/rocketmq 域

<read_first>
- springboot/src/main/java/cn/coderstory/springboot/monitor/
- springboot/src/main/java/cn/coderstory/springboot/rocketmq/
</read_first>

<action>
清理 monitor、rocketmq 两个域：

1. 删除未使用的 import
2. 删除注释掉的代码块
3. 删除未使用的字段和局部变量
4. 删除未使用的私有方法

**注意事项：** rocketmq 域涉及 Topic/Consumer Group 管理，MQ 相关的类注释可能包含配置说明，保留含业务逻辑的注释。

清理后执行：
```bash
./gradlew.bat build -x test
```

提交：
```bash
git add springboot/src/main/java/cn/coderstory/springboot/monitor/
git add springboot/src/main/java/cn/coderstory/springboot/rocketmq/
git commit -m "cleanup: remove dead code in monitor/rocketmq domains"
```
</action>

<acceptance_criteria>
- [ ] `./gradlew.bat build -x test` 编译通过
- [ ] 所有 RocketMQ 配置相关注释（含配置说明的注释）被保留
- [ ] import 语句无冗余
</acceptance_criteria>

---

### Task 6: 清理 seckill 域（高风险）

<read_first>
- springboot/src/main/java/cn/coderstory/springboot/seckill/
- 全局搜索确认删除的方法未被 Lua 脚本或 RocketMQ 回调反射引用
</read_first>

<action>
清理 seckill 域（controller/dto/entity/mapper/mq/sse/stock/service/vo）：

1. 删除未使用的 import
2. 删除注释掉的代码块
3. 删除未使用的字段和局部变量
4. 删除未使用的私有方法
5. **特谨慎处理（高风险）：**
   - 删除每个看起来未使用的私有方法前，全局搜索方法名：
     ```bash
     rg "methodName" springboot/ --type java --glob "*.lua" --glob "*.yml" --glob "*.yaml"
     ```
   - Entity 类字段不做删除（被 MyBatis Plus 反射使用）
   - Mapper 接口方法不做删除
   - MQ 消费者/生产者中的方法确认未被 RocketMQ 回调框架调用

清理后执行：
```bash
./gradlew.bat build -x test
```

提交：
```bash
git add springboot/src/main/java/cn/coderstory/springboot/seckill/
git commit -m "cleanup: remove dead code in seckill domain"
```
</action>

<acceptance_criteria>
- [ ] `./gradlew.bat build -x test` 编译通过
- [ ] 全局搜索确认：每个被删除的方法名无残留引用（java/lua/yaml/yml 文件）
- [ ] 秒杀模块的 RocketMQ 消费者/生产者类中未被删除任何可能被框架调用的方法
- [ ] import 语句无冗余
</acceptance_criteria>

---

### Task 7: 全量验证

<read_first>
- 前面 6 个 task 全部完成后执行
</read_first>

<action>
所有清理完成后执行全量验证：

```bash
# 1. 编译验证
./gradlew.bat build -x test

# 2. Checkstyle 检查（验证 import 等无新增违规）
./gradlew.bat checkstyleMain

# 3. 启动验证
./gradlew.bat bootRun
```

启动后验证：
- 登录 API 可访问
- 用户管理页面正常加载
- 秒杀相关 API 无异常

手动检查 Checkstyle 报告：
- 打开 `build/reports/checkstyle/main.html` 确认无新增违规
</action>

<acceptance_criteria>
- [ ] `./gradlew.bat build -x test` 编译通过
- [ ] `./gradlew.bat checkstyleMain` 无新增违规（或违规数量与清理前一致）
- [ ] `./gradlew.bat bootRun` 正常启动，无异常退出
- [ ] 核心 API 端点可访问（登录、用户管理、秒杀）
- [ ] 无残留的注释掉的代码块（grep 抽查）
</acceptance_criteria>

## Verification

### must_haves
1. 编译通过 — `./gradlew.bat build -x test` 零错误
2. Checkstyle 零新增违规 — `./gradlew.bat checkstyleMain` 无新增
3. 启动正常 — `./gradlew.bat bootRun` 正常启动，无异常退出
4. 全部 121 个 Java 源文件已处理，无残留死代码
5. 所有被 Lombok/MyBatis Plus/Spring 隐式引用的代码未被误删

### truths
- 死代码删除只影响编译产物，不应改变运行时行为
- 如果编译通过，所有被删除的代码确实是未使用的
- IntelliJ "Unused declaration" 检测比纯文本搜索更准确（能识别框架隐式引用）
- Checkstyle 的 `isIgnoreFailures = true` 意味着需要手动查看报告
