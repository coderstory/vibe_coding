---
phase: 23
name: 后端结构体优化
wave: 1
depends_on: [22]
requirements: [BAC-05, BAC-07, BAC-08]
autonomous: false
files_modified:
  - springboot/src/main/java/cn/coderstory/springboot/*/dto/*.java
  - springboot/src/main/java/cn/coderstory/springboot/*/vo/*.java
  - springboot/src/main/resources/mapper/*.xml
  - springboot/src/main/java/cn/coderstory/springboot/*/service/**/*.java
---

# Plan: 后端结构体优化 - Wave 1 - 清理与精简

## Objective

清理无用 DTO/VO、精简 Mapper XML、删除未引用的 Service 方法。在目录重构前完成清理，避免搬动死代码。

## Tasks

### Task 1: DTO/VO 清理

<read_first>
- springboot/src/main/java/cn/coderstory/springboot/user/dto/UserVO.java
- springboot/src/main/java/cn/coderstory/springboot/seckill/dto/SeckillRequest.java
- springboot/src/main/java/cn/coderstory/springboot/seckill/dto/SeckillResponse.java
- springboot/src/main/java/cn/coderstory/springboot/seckill/vo/ActivityDetailVO.java
- springboot/src/main/java/cn/coderstory/springboot/shared/vo/ApiResponse.java
- springboot/src/main/java/cn/coderstory/springboot/shared/vo/ResultResponse.java
</read_first>

<action>
检查每个 DTO/VO：

1. **UserVO** — 被 UserController + UserService 使用，保留。检查字段是否有冗余（如 roleName 是否已有 Role 关联）
2. **SeckillRequest/SeckillResponse** — 被 SeckillController + SeckillService 使用，保留
3. **ActivityDetailVO** — 被 SeckillActivityController + ActivityService 使用，保留
4. **ResultResponse** — 检查是否被任何类使用，与 ApiResponse 功能是否重叠。如未使用则删除

对每个待删的 DTO/VO 做全局搜索确认无反射引用后再删除。
</action>

<acceptance_criteria>
- [ ] `./gradlew.bat build -x test` 编译通过
- [ ] 所有保留的 DTO/VO 确认有实际调用者
- [ ] 删除了确定无用的 DTO/VO 类
</acceptance_criteria>

---

### Task 2: Mapper XML 精简

<read_first>
- springboot/src/main/resources/mapper/KnowledgeArticleMapper.xml
- springboot/src/main/resources/mapper/KnowledgeArticleTagMapper.xml
- springboot/src/main/resources/mapper/KnowledgeCategoryMapper.xml
- springboot/src/main/resources/mapper/MenuMapper.xml
- springboot/src/main/resources/mapper/UserMapper.xml
</read_first>

<action>
逐文件检查 Mapper XML：

1. 确认每个 `<select>/<insert>/<update>/<delete>` 在对应的 Mapper 接口中有匹配方法
2. 删除无对应 Mapper 方法的冗余 SQL 语句
3. 检查是否有重复定义的 resultMap（通过对比 ID 和 columns 判断）

对每个待删的 XML 片段做全局搜索确认无反射引用。
</action>

<acceptance_criteria>
- [ ] `./gradlew.bat build -x test` 编译通过
- [ ] 所有 Mapper XML 中的 SQL 语句都有对应的 Mapper 接口方法
- [ ] 无冗余的 resultMap 定义
</acceptance_criteria>

---

### Task 3: Service 方法清理（BAC-08）

<read_first>
- 全部 Service 实现类（10 个域）
</read_first>

<action>
对每个 Service 实现类的每个 public 方法做全局搜索：

```bash
rg "methodName" springboot/src/main/java/ --type java
```

删除条件（严格标准 D-11）：
- 该方法不被任何 Controller 调用
- 该方法不被其他 Service 调用
- 该方法不被测试类调用

注意：
- `@Override` 方法需要在接口上也同步删除
- 接口方法可能在 Controller 中被调用（通过接口引用）
- Service 之间的互相调用（如 ActivityService 调用 PreheatService）是有效的，不应删除
</action>

<acceptance_criteria>
- [ ] `./gradlew.bat build -x test` 编译通过
- [ ] 所有被删除的 Service 方法确认无任何调用者
- [ ] Controller 接口功能完整，无 404/500
</acceptance_criteria>

## Verification

### must_haves
1. 编译通过
2. 无功能回归
3. 每个删除前做全局搜索确认
