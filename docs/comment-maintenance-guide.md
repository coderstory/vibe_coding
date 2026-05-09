# 注释维护指南

## 三层维护策略

| 层级 | 名称 | 频率 | 执行人 |
|------|------|------|--------|
| L1 | PR 同步检查 | 每次 PR | PR 作者 + Review 者 |
| L2 | TODO 定期清理 | 每月 | 值班开发者 |
| L3 | 季度漂移抽查 | 每季度 | 指定审查人 |

---

## L1：PR 同步检查

每次提交 PR 时，Review 者按 CLAUDE.md §7 检查清单逐项核对。

### 自动检查命令

```powershell
# 检查空骨架注释
grep -rn '@param\s\+\w\+\s*\*/' springboot/src/main/java/
grep -rn '@return\s*\*/' springboot/src/main/java/

# 检查被注释掉的代码块
grep -rn '^\s*//\s*\(public\|private\|protected\|import\|const\|let\|var\|function\)' springboot/src/main/java/ app-vue/src/

# 检查裸 TODO
grep -rn 'TODO[^#]' springboot/src/main/java/ app-vue/src/

# 检查 @author 标签
grep -rn '@author' springboot/src/main/java/

# 检查中英文混用（排除技术术语）
grep -rnP '[一-鿿]\w+[一-鿿]' springboot/src/main/java/ --include='*.java'
```

### PR 通过标准

- L3 层文件：类级 + 方法级 Javadoc 完整
- 无空骨架注释
- 无注释代码块
- TODO 均关联 Issue 编号
- 无 `@author` 标签
- L2 层组件/Service 有职责说明

---

## L2：TODO 定期清理

每月首个工作日执行。

### 步骤

1. **全局扫描**

   ```powershell
   # 后端
   grep -rn 'TODO' springboot/src/main/java/ --include='*.java'

   # 前端
   grep -rn 'TODO' app-vue/src/ --include='*.{ts,vue}'
   ```

2. **分类处理**

   | 模式 | 处理方式 |
   |------|----------|
   | `TODO(#N):` | 检查 Issue #N 是否仍 open，已 close 则移除 TODO |
   | `TODO:` 无编号 | 创建 Issue 补充编号，或删除（无需追踪） |
   | `TODO(#N):` 但已实现 | 直接删除 TODO 行 |

3. **记录**

   清理结果记录到 `.planning/todos/` 或项目周会纪要。

---

## L3：季度漂移抽查

每季度末执行。

### 样本抽取

- 后端：从 L3 层文件中随机选 5 个（Controller/Config/异常体系/JWT/AOP）
- 前端：从 Vue 组件中随机选 5 个（按域分层抽样）

### 检查标准

| 检查项 | 判定 |
|--------|------|
| 类级 Javadoc 描述与实际行为一致 | 阅读类实现，检查是否有新增方法未加注释 |
| 方法签名变更后 Javadoc 同步更新 | 对比 git diff 与 Javadoc 参数列表 |
| 无空骨架注释残留 | `@param`/`@return` 后无空白描述 |
| 无注释代码块 | 无 `// public`/`// private`/`// function` 等 |
| L0-L3 层级合规 | 对照 CLAUDE.md L0-L3 判定表 |

### 评分标准

- 通过率 = 通过检查项 / 总检查项 × 100%
- ≥ 80%：通过，记录结果
- < 80%：创建 Issue，安排专项修复

### 首次抽查

- **范围**：Phase 28 中补充注释的 Controller 和 Config 类
- **时间**：2026-08-01
- **负责人**：Git log 中贡献最多的开发者

---

## CLAUDE.md 注释规则参考

所有规则以 CLAUDE.md 中「注释规范」章节为准，本文档仅提供操作流程说明。

---

*Created: 2026-05-09 — v1.7 Phase 30*
