---
phase: 23
name: 后端结构体优化
wave: 4
depends_on: [23-W3]
requirements: [BAC-04]
autonomous: false
files_modified: []
---

# Plan: 后端结构体优化 - Wave 4 - 工具类合并验证

## Objective

验证当前工具类（Util/Helper）是否需要合并。根据 CONTEXT.md D-13/D-14，当前仅 `ZstdUtil` 一个明确的 Util 类，shared/limiter 和 shared/lock 等是独立服务类，不属于"零散工具类"。

## Tasks

### Task 1: 验证 Util/Helper 类

<read_first>
- springboot/src/main/java/cn/coderstory/springboot/shared/util/ZstdUtil.java
</read_first>

<action>
全局搜索 Util/Helper 类：

```bash
find src/main/java -name "*Util*" -o -name "*Helper*"
```

确认结果：
- 结果应为仅 `ZstdUtil` 一个工具类
- 检查 ZstdUtil 的 compress/decompress 方法是否确实被使用（被 KnowledgeServiceImpl 使用）
- 如无其他零散工具类，关闭 BAC-04 需求
</action>

<acceptance_criteria>
- [ ] 确认无需要合并的零散工具类
- [ ] BAC-04 可标记为"已验证无需处理"
</acceptance_criteria>

## Verification

### must_haves
1. 确认项目中没有散落的 Util/Helper 类需要合并
2. 记录 BAC-04 为"已验证无需处理"
