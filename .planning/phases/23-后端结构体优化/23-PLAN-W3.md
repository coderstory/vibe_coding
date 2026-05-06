---
phase: 23
name: 后端结构体优化
wave: 3
depends_on: []
requirements: [BAC-10]
autonomous: false
files_modified:
  - "springboot/src/main/java/cn/coderstory/springboot/**/*.java"
---

# Plan: 后端结构体优化 - Wave 3 - type-first 目录重构

## Objective

将后端目录从 domain-first 重构为 type-first。涉及全部 121 个 Java 文件，10 个业务域。

## 目录映射

```
当前（domain-first）          →  目标（type-first）
{domain}/controller/          →  controller/{domain}/
{domain}/service/             →  service/{domain}/
{domain}/service/impl/        →  service/{domain}/impl/
{domain}/mapper/              →  mapper/{domain}/
{domain}/entity/              →  entity/{domain}/
{domain}/dto/                 →  dto/{domain}/
{domain}/vo/                  →  vo/{domain}/
```

## Tasks

### Task 1: 移动 shared 层

<read_first>
- 当前 shared 层文件结构
</read_first>

<action>
shared 层比较特殊，包含 aspect/config/exception/limiter/lock/security/util/vo 子包。

移动规则：
- `shared/config/*` → `config/`（配置类放一级）
- `shared/security/*` → `config/`（security 也是配置范畴）
- `shared/exception/*` → `exception/`
- `shared/util/*` → `util/`
- `shared/vo/*` → `dto/`
- `shared/aspect/*` → `aspect/`
- `shared/limiter/*` → `limiter/`
- `shared/lock/*` → `lock/`

操作：
```bash
# 对每个子包：创建目标目录，git mv，更新 package，更新 import
```

每个子包移动后编译验证。
</action>

<acceptance_criteria>
- [ ] `./gradlew.bat build -x test` 编译通过
- [ ] shared 层所有文件在新位置
- [ ] package 声明和 import 引用全部更新
</acceptance_criteria>

---

### Task 2: 移动 user/role/menu 域

<read_first>
- user、role、menu 三个域结构
</read_first>

<action>
移动三个独立域，使用 git mv + sed 更新 package 和 import：

```bash
# 每个域的操作模板：
# 1. mkdir -p controller/{domain} service/{domain}/impl mapper/{domain} entity/{domain} dto/{domain}
# 2. git mv old/controller/*.java controller/{domain}/
# 3. git mv old/service/*.java service/{domain}/
# 4. git mv old/service/impl/*.java service/{domain}/impl/
# 5. git mv old/mapper/*.java mapper/{domain}/
# 6. git mv old/entity/*.java entity/{domain}/
# 7. git mv old/dto/*.java dto/{domain}/
# 8. 用 sed 更新 package 声明
# 9. 全局搜索旧 import 并替换为新 import
# 10. 编译验证
```

三个域一起移动后编译（user 可能被其他域引用）。
</action>

<acceptance_criteria>
- [ ] `./gradlew.bat build -x test` 编译通过
- [ ] 三个域的所有文件在新位置
- [ ] 所有跨域 import 引用已更新
</acceptance_criteria>

---

### Task 3: 移动 auth/audit/monitor 域

<read_first>
- auth、audit、monitor 三个域
</read_first>

<action>
与 Task 2 相同模板。注意 audit 可能被 auth 引用（AuthService 注入 AuditService）。

移动后编译验证。
</action>

<acceptance_criteria>
- [ ] `./gradlew.bat build -x test` 编译通过
- [ ] 三个域的所有文件在新位置
- [ ] auth→audit 的跨域引用已更新
</acceptance_criteria>

---

### Task 4: 移动 order/knowledge/rocketmq 域

<read_first>
- order、knowledge、rocketmq 三个域
</read_first>

<action>
与 Task 2 相同模板。

注意：
- knowledge 域引用 ZstdUtil（将被移动到 util/）
- rocketmq 域的 mapper 文件夹在后续验证中确认

移动后编译验证。
</action>

<acceptance_criteria>
- [ ] `./gradlew.bat build -x test` 编译通过
- [ ] 三个域的所有文件在新位置
- [ ] knowledge→util 的跨域引用已更新
</acceptance_criteria>

---

### Task 5: 移动 seckill 域（复杂）

<read_first>
- seckill 域全结构（含 mq/sse/stock 子包）
</read_first>

<action>
seckill 域结构复杂，包含子包：
- seckill/controller/ → controller/seckill/
- seckill/service/ → service/seckill/
- seckill/service/impl/ → service/seckill/impl/
- seckill/mapper/ → mapper/seckill/
- seckill/entity/ → entity/seckill/
- seckill/dto/ → dto/seckill/
- seckill/vo/ → vo/seckill/
- seckill/mq/consumer/ → mq/seckill/consumer/
- seckill/mq/producer/ → mq/seckill/producer/
- seckill/sse/ → sse/seckill/
- seckill/stock/entity/ → entity/seckill/stock/
- seckill/stock/mapper/ → mapper/seckill/stock/
- seckill/stock/service/ → service/seckill/stock/
- seckill/stock/service/impl/ → service/seckill/stock/impl/

由于修改范围较大，建议拆分子步骤，每步编译验证。
</action>

<acceptance_criteria>
- [ ] `./gradlew.bat build -x test` 编译通过
- [ ] seckill 所有文件在新位置
- [ ] 跨域引用（shared/lock、shared/limiter 等）已更新
- [ ] 所有 package 声明正确
</acceptance_criteria>

---

### Task 6: 全量编译验证

<read_first>
- 所有域已移动完成
</read_first>

<action>
全量编译验证：

```bash
./gradlew.bat build -x test
```

运行测试：
```bash
./gradlew.bat test
```

功能回归验证（手动）：
- 登录 API → /api/auth/login
- 用户管理 → /api/users/...
- RocketMQ → /api/rocketmq/...
- 秒杀 → /api/seckill/...
</action>

<acceptance_criteria>
- [ ] `./gradlew.bat build -x test` 编译通过
- [ ] `./gradlew.bat test` 全部通过
- [ ] 所有 10 个域的文件均在 type-first 结构中
- [ ] 无残留的 domain-first package 声明
- [ ] 所有跨域 import 引用正确
</acceptance_criteria>

## Verification

### must_haves
1. 全部 121 个文件移动到 type-first 结构
2. 编译通过
3. 测试通过
4. 无 `import *` 残留（避免使用通配符导入）
5. 每个域移动后编译验证

### truths
- git mv 分步进行，确保每一步都可回退
- package 声明和 import 引用必须同步更新
- 使用 IDE 的 Optimize Imports 可以自动清理旧的 import
