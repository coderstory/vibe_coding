# Phase 22 Discussion Log

**Date:** 2026-05-07
**Mode:** Default (interactive)

## Areas Discussed

### 1. 检测与删除策略
- **Options:** IDE+人工审查 / 工具自动+批量 / 混合模式
- **Selection:** 混合模式 — IntelliJ 扫描定位，按模块分批处理，先易后难
- **Notes:** 执行顺序为 import → 注释块 → 字段 → 方法

### 2. 安全与回退机制
- **Options:** 按模块分批+git分段提交 / 分支整体清理+全量回归
- **Selection:** 按模块分批 + git 分段提交
- **Notes:** 每批独立 commit，方便逐模块回退

### 3. 注释代码处理规则
- **Options:** 全部删除 / 区分处理
- **Selection:** 区分处理 — 废弃代码删除，TODO/FIXME 保留，有疑问的标记审查

## Deferred Ideas

None — 讨论紧贴 Phase 22 范围。
