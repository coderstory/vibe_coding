# Element Plus 主题化研究 - 海滩主题完成指南

**项目:** Ocean Breeze Admin 海滩主题
**研究日期:** 2026-04-03
**Element Plus 版本:** 2.9.0
**置信度:** HIGH

---

## 执行摘要

本项目已完成海滩主题基础样式（CSS变量定义），但在业务页面中仍存在大量未主题化的Element Plus组件。本文档提供完整的主题覆盖清单、CSS选择器参考和常见踩坑指南。

---

## 一、现有覆盖分析

### 已实现 (enterprise-theme.css)

| 组件 | 覆盖状态 | 选择器 |
|------|----------|--------|
| el-table | ✅ 部分 | `.el-table`, `.el-table th`, `.el-table td` |
| el-button | ✅ 完整 | `.el-button`, `.el-button--primary` |
| el-tag | ✅ 完整 | `.el-tag`, `.el-tag--success/warning/danger/info` |
| el-alert | ✅ 完整 | `.el-alert`, `.el-alert--success/warning/danger/info` |
| el-pagination | ✅ 完整 | `.el-pagination` |
| el-dialog | ✅ 部分 | `.el-dialog`, `.el-dialog__header` |
| el-input | ✅ 完整 | `.el-input__wrapper` |
| el-select | ✅ 部分 | `.el-select__wrapper` |
| el-dropdown-menu | ✅ 完整 | `.el-dropdown-menu`, `.el-dropdown-menu__item` |
| el-tabs | ✅ 完整 | `.el-tabs__item`, `.el-tabs__active-bar` |
| el-form | ✅ 部分 | `.el-form-item__label` |

### 未覆盖/需补充

| 组件 | 优先级 | 原因 |
|------|--------|------|
| el-card | 🔴 高 | Login页面、业务数据页面使用 |
| el-tree | 🔴 高 | RoleManage权限树、CategoryTree使用 |
| el-switch | 🔴 高 | UserManagement用户状态切换 |
| el-radio | 🔴 高 | UserManagement性别选择 |
| el-date-picker | 🔴 高 | AuditLog时间范围选择 |
| el-empty | 🟡 中 | AuditLog空状态 |
| el-link | 🟡 中 | 各页面操作链接 |
| el-descriptions | 🟡 中 | 潜在使用 |
| el-loading | 🟡 中 | 全局加载状态 |
| el-message | 🟡 中 | 消息提示已定制但可优化 |
| el-message-box | 🟡 中 | 确认对话框 |
| el-textarea | 🟡 中 | RoleManage角色描述 |

---

## 二、完整组件覆盖清单

### 2.1 表单组件 (Form Components)

#### el-input / el-textarea

```css
/* 现有覆盖 */
.el-input__wrapper {
  border-radius: var(--el-border-radius-round);
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid #dbeafe;
}

/* 需补充 - textarea */
.el-textarea__inner {
  border-radius: var(--el-border-radius-base);
  border: 1px solid #dbeafe;
  background: rgba(255, 255, 255, 0.9);
}

.el-textarea__inner:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2);
}

/* 搜索区域输入框强化 */
.search-section .el-input__wrapper {
  background: #ffffff;
}
```

#### el-select

```css
/* 现有覆盖 */
.el-select__wrapper {
  border-radius: var(--el-border-radius-round);
}

/* 需补充 - 下拉选项 */
.el-select-dropdown__item {
  border-radius: var(--el-border-radius-small);
  padding: 8px 12px;
}

.el-select-dropdown__item.is-hovering {
  background: linear-gradient(135deg, #fef3c7, #fde68a);
}

.el-select-dropdown__item.is-selected {
  background: linear-gradient(135deg, #dbeafe, #bfdbfe);
  color: #1e3a8a;
  font-weight: 600;
}

.el-select-dropdown__item.is-highlighted {
  background: #eff6ff;
}

/* 选中标签样式 */
.el-select__tags {
  flex-wrap: nowrap;
  overflow-x: auto;
}
```

#### el-switch

```css
/* 海洋蓝开关 */
.el-switch {
  --el-switch-off-color: #bfdbfe;
  --el-switch-on-color: #3b82f6;
  --el-switch-border-color: #93c5fd;
}

.el-switch.is-checked .el-switch__core {
  background: linear-gradient(135deg, #3b82f6, #1e3a8a);
  border-color: #3b82f6;
}

.el-switch__core {
  border-radius: 24px;
}

.el-switch__core::after {
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
}

/* 激活状态文字 */
.el-switch__label--left,
.el-switch__label--right {
  color: #64748b;
}

.el-switch__label.is-active {
  color: #1e3a8a;
}
```

#### el-radio

```css
/* 海洋蓝单选框 */
.el-radio {
  --el-radio-font-size: 14px;
  --el-radio-text-color: #475569;
  --el-radio-input-border: 1px solid #bfdbfe;
  --el-radio-input-border-hover: #3b82f6;
  --el-radio-input-fill: #3b82f6;
}

.el-radio__input.is-checked .el-radio__inner {
  background: linear-gradient(135deg, #3b82f6, #1e3a8a);
  border-color: #3b82f6;
}

.el-radio__inner::after {
  background: #ffffff;
}

.el-radio__label {
  color: #475569;
}

.el-radio__input.is-checked + .el-radio__label {
  color: #1e3a8a;
  font-weight: 500;
}

/* Radio Group 按钮样式 */
.el-radio-group {
  display: flex;
  gap: 8px;
}

.el-radio-button__inner {
  border-radius: var(--el-border-radius-round);
  border: 1px solid #bfdbfe;
  background: #ffffff;
  color: #3b82f6;
}

.el-radio-button__original-radio:checked + .el-radio-button__inner {
  background: linear-gradient(135deg, #3b82f6, #1e3a8a);
  border-color: #3b82f6;
  color: #ffffff;
  box-shadow: none;
}
```

#### el-date-picker

```css
/* 日期选择器 */
.el-date-editor {
  --el-date-editor-width: auto;
}

.el-date-editor .el-input__wrapper {
  border-radius: var(--el-border-radius-round);
}

/* 日期面板 */
.el-date-picker {
  border-radius: 16px;
  box-shadow: var(--el-box-shadow-dark);
  border: 1px solid rgba(147, 197, 253, 0.3);
}

.el-date-picker__header {
  margin: 16px 20px;
  color: #1e3a8a;
  font-weight: 600;
}

.el-date-table th {
  color: #64748b;
  font-weight: 500;
}

.el-date-table td.available:hover {
  background: #f0f9ff;
}

.el-date-table td.today .el-date-table-cell__text {
  background: linear-gradient(135deg, #3b82f6, #1e3a8a);
  color: #ffffff;
  border-radius: 50%;
}

.el-date-table td.current:not(.disabled) .el-date-table-cell__text {
  background: linear-gradient(135deg, #3b82f6, #1e3a8a);
  color: #ffffff;
}

.el-date-table td.in-range .el-date-table-cell {
  background: #eff6ff;
}

.el-date-table td.start-date .el-date-table-cell__text,
.el-date-table td.end-date .el-date-table-cell__text {
  background: linear-gradient(135deg, #3b82f6, #1e3a8a);
  color: #ffffff;
  border-radius: 50%;
}

/* 时间选择器 */
.el-time-select {
  border-radius: 12px;
}

.el-time-panel {
  border-radius: 16px;
  box-shadow: var(--el-box-shadow-dark);
}
```

#### el-cascader (如需使用)

```css
.el-cascader__tags {
  flex-wrap: nowrap;
  overflow-x: auto;
}

.el-cascader-node {
  border-radius: var(--el-border-radius-small);
  padding: 4px 8px;
}

.el-cascader-node.is-active {
  color: #3b82f6;
  font-weight: 500;
}

.el-cascader-node.in-active-path {
  background: #eff6ff;
}
```

---

### 2.2 数据展示组件 (Data Display)

#### el-table (补充完善)

```css
/* 现有已覆盖，主要补充以下细节 */

/* 表头排序图标 */
.el-table .ascending .sort-caret.ascending,
.el-table .descending .sort-caret.descending {
  border-bottom-color: #1e3a8a;
}

/* 排序图标颜色 */
.el-table .sort-caret {
  border-color: transparent;
  border-bottom-color: #93c5fd;
}

/* 斑马纹优化 */
.el-table--striped .el-table__body tr.el-table__row--striped.current-row > td,
.el-table--striped .el-table__body tr.current-row > td {
  background: linear-gradient(135deg, #fef3c7, #fde68a);
}

/* 选中行 */
.el-table__body tr.current-row > td {
  background: #fef3c7 !important;
}

/* 展开行 */
.el-table__expand-icon {
  color: #3b82f6;
  font-size: 14px;
}

/* 固定列阴影 */
.el-table__fixed {
  box-shadow: 4px 0 8px rgba(30, 58, 138, 0.05);
}

.el-table__fixed-right {
  box-shadow: -4px 0 8px rgba(30, 58, 138, 0.05);
}

/* 加载中 */
.el-table__body-wrapper.is-scrolling-none {
  /* 防止滚动时重排 */
}

/* 空数据 */
.el-table__empty-text {
  color: #64748b;
  padding: 48px 0;
  font-size: 15px;
}

.el-table__empty-block {
  background: linear-gradient(180deg, #f0f9ff, #eff6ff);
}
```

#### el-card

```css
/* Login页面卡片 */
.login-card {
  border-radius: 16px;
  box-shadow: var(--el-box-shadow-dark);
  border: 1px solid rgba(147, 197, 253, 0.3);
  overflow: hidden;
}

.el-card__header {
  background: linear-gradient(135deg, #eff6ff, #dbeafe);
  border-bottom: 1px solid rgba(147, 197, 253, 0.3);
  padding: 20px 24px;
  font-weight: 600;
  color: #1e3a8a;
}

.el-card__body {
  padding: 24px;
}

/* 通用卡片 - macos风格已定义 */
.macos-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(147, 197, 253, 0.3);
  border-radius: 16px;
}

/* 业务数据页面卡片 */
.article-panel .el-card {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}
```

#### el-empty

```css
/* 空状态 */
.el-empty__image {
  width: 120px;
  height: 120px;
  opacity: 0.8;
}

.el-empty__description {
  color: #64748b;
  font-size: 14px;
  margin-top: 16px;
}

.el-empty__image svg {
  fill: #93c5fd;
}

/* 审计日志空状态 */
.audit-table + .el-empty {
  padding: 48px 0;
}
```

#### el-descriptions (如需使用)

```css
.el-descriptions__label {
  color: #64748b;
  font-weight: 500;
  background: #f8fafc;
  border-radius: var(--el-border-radius-small);
  padding: 12px 16px;
}

.el-descriptions__content {
  background: #ffffff;
  padding: 12px 16px;
  border-radius: var(--el-border-radius-small);
}

.el-descriptions--bordered .el-descriptions__cell {
  border: 1px solid #e2e8f0;
}
```

---

### 2.3 导航组件 (Navigation)

#### el-tree

```css
/* 权限树、分类树 */
.el-tree {
  --el-tree-node-hover-bg-color: #f0f9ff;
  --el-tree-text-color: #475569;
  --el-tree-expand-icon-color: #93c5fd;
}

.el-tree-node__content {
  border-radius: var(--el-border-radius-small);
  height: 36px;
  margin-bottom: 4px;
}

.el-tree-node__content:hover {
  background: linear-gradient(135deg, #f0f9ff, #eff6ff);
}

.el-tree-node.is-current > .el-tree-node__content {
  background: linear-gradient(135deg, #fef3c7, #fde68a) !important;
  color: #92400e;
  font-weight: 500;
}

.el-tree-node.is-expanded > .el-tree-node__content {
  background: transparent;
}

/* 选中高亮 */
.el-tree-node.is-current .el-tree-node__content .el-tree-node__label {
  color: #92400e;
  font-weight: 600;
}

/* 连接线 */
.el-tree > .el-tree-node__children > .el-tree-node {
  position: relative;
}

.el-tree > .el-tree-node__children > .el-tree-node::before {
  content: '';
  position: absolute;
  left: 12px;
  top: 0;
  height: 18px;
  border-left: 1px dashed #bfdbfe;
}

/* 复选框 */
.el-tree .el-checkbox__input.is-checked .el-checkbox__inner {
  background: linear-gradient(135deg, #3b82f6, #1e3a8a);
  border-color: #3b82f6;
}

.el-tree .el-checkbox__input.is-indeterminate .el-checkbox__inner {
  background: linear-gradient(135deg, #3b82f6, #1e3a8a);
  border-color: #3b82f6;
}

/* 分类树头部 */
.category-tree .tree-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #eee;
  font-weight: 600;
  color: #1e3a8a;
}

/* 权限树容器 */
.permission-tree-container {
  max-height: 400px;
  overflow-y: auto;
  padding: 8px;
}

.permission-tree-container .el-tree {
  background: transparent;
}
```

#### el-tabs (补充完善)

```css
/* 已在enterprise-theme.css覆盖，可补充 */

.el-tabs__nav-wrap::after {
  height: 1px;
  background: #e2e8f0;
}

/* 卡片样式标签页 */
.el-tabs--card > .el-tabs__header .el-tabs__nav {
  border: none;
}

.el-tabs--card > .el-tabs__header .el-tabs__item {
  border: 1px solid #e2e8f0;
  border-bottom: none;
  border-radius: 8px 8px 0 0;
  background: #f8fafc;
  margin-right: 4px;
}

.el-tabs--card > .el-tabs__header .el-tabs__item.is-active {
  background: #ffffff;
  border-bottom: 1px solid #ffffff;
  color: #3b82f6;
}

/* 胶囊样式标签页 */
.el-tabs--border-card {
  border-radius: 12px;
  box-shadow: var(--el-box-shadow);
}

.el-tabs--border-card > .el-tabs__header {
  background: linear-gradient(135deg, #eff6ff, #dbeafe);
  margin: 0;
}

.el-tabs--border-card > .el-tabs__header .el-tabs__item {
  color: #64748b;
  font-weight: 500;
}

.el-tabs--border-card > .el-tabs__header .el-tabs__item.is-active {
  color: #1e3a8a;
  background: #ffffff;
}
```

#### el-menu (侧边栏)

```css
/* 侧边栏渐变已在CSS变量定义 */
.el-menu {
  --el-menu-bg-color: linear-gradient(180deg, #1e3a8a 0%, #3b82f6 100%);
  --el-menu-text-color: rgba(255, 255, 255, 0.9);
  --el-menu-hover-bg-color: #fef3c7;
  --el-menu-active-color: #1e3a8a;
  --el-menu-item-height: 50px;
  border: none !important;
}

.el-menu-item,
.el-sub-menu__title {
  color: rgba(255, 255, 255, 0.9);
  transition: all 0.3s;
}

.el-menu-item:hover,
.el-sub-menu__title:hover {
  background: rgba(254, 243, 199, 0.2) !important;
  color: #fef3c7;
}

.el-menu-item.is-active {
  background: linear-gradient(135deg, rgba(254, 243, 199, 0.3), rgba(253, 230, 138, 0.2)) !important;
  color: #fef3c7 !important;
  font-weight: 600;
}

.el-menu-item.is-active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 60%;
  background: #fbbf24;
  border-radius: 0 4px 4px 0;
}

/* 子菜单 */
.el-sub-menu .el-menu {
  background: rgba(30, 58, 138, 0.3) !important;
}

.el-sub-menu.is-active .el-sub-menu__title {
  color: #fef3c7 !important;
}

/* 折叠状态 */
.el-menu--collapse {
  width: 64px;
}

.el-menu--popup {
  border-radius: 12px;
  box-shadow: var(--el-box-shadow-dark);
  border: 1px solid rgba(147, 197, 253, 0.3);
}
```

---

### 2.4 反馈组件 (Feedback)

#### el-message

```css
/* 消息提示已通过CSS变量部分覆盖 */
.el-message {
  --el-message-bg-color: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  box-shadow: var(--el-box-shadow-dark);
  border: 1px solid rgba(147, 197, 253, 0.3);
  backdrop-filter: blur(10px);
}

.el-message--success {
  background: linear-gradient(135deg, #d1fae5, #a7f3d0);
  border-color: #6ee7b7;
}

.el-message--warning {
  background: linear-gradient(135deg, #fef3c7, #fde68a);
  border-color: #fcd34d;
}

.el-message--error {
  background: linear-gradient(135deg, #fee2e2, #fecaca);
  border-color: #fca5a5;
}

.el-message--info {
  background: linear-gradient(135deg, #dbeafe, #bfdbfe);
  border-color: #93c5fd;
}
```

#### el-message-box

```css
/* 确认对话框 */
.el-message-box {
  border-radius: 16px;
  box-shadow: var(--el-box-shadow-dark);
  border: 1px solid rgba(147, 197, 253, 0.3);
  padding: 24px;
}

.el-message-box__header {
  padding-bottom: 16px;
  border-bottom: 1px dashed rgba(147, 197, 253, 0.5);
}

.el-message-box__title {
  color: #1e3a8a;
  font-weight: 600;
  font-size: 18px;
}

.el-message-box__content {
  padding: 24px 0;
  color: #475569;
}

.el-message-box__message {
  color: #475569;
}

.el-message-box__input {
  padding-top: 16px;
}

.el-message-box__input .el-input__wrapper {
  border-radius: var(--el-border-radius-base);
}

.el-message-box__footer {
  padding-top: 16px;
  border-top: 1px dashed rgba(147, 197, 253, 0.5);
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
```

#### el-loading

```css
/* 加载状态 */
.el-loading-mask {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(4px);
}

.el-loading-spinner {
  --el-loading-spinner-size: 42px;
}

.el-loading-spinner .circular {
  width: 42px;
  height: 42px;
}

.el-loading-spinner .path {
  stroke: #3b82f6;
  stroke-width: 3;
}

.el-loading-text {
  color: #3b82f6;
  font-size: 14px;
  margin-top: 12px;
}

/* 组件内加载 */
.el-table__body-wrapper.is-scrolling-none {
  /* 配合virtual scroll优化 */
}
```

#### el-popover / el-tooltip

```css
.el-popover.el-popper {
  border-radius: 12px;
  box-shadow: var(--el-box-shadow-dark);
  border: 1px solid rgba(147, 197, 253, 0.3);
  background: rgba(255, 255, 255, 0.98);
}

.el-tooltip__popper.is-dark {
  background: linear-gradient(135deg, #1e3a8a, #3b82f6);
  border-radius: 8px;
  padding: 8px 12px;
}

.el-tooltip__popper.is-dark .el-tooltip__arrow::before {
  background: #3b82f6;
}
```

---

### 2.5 布局组件 (Layout)

#### el-header (Layout.vue)

```css
/* 头部玻璃效果 */
.el-header {
  --el-header-bg-color: rgba(255, 255, 255, 0.85);
  --el-header-text-color: #1e293b;
  --el-header-height: 60px;
  background: var(--el-header-bg-color);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(147, 197, 253, 0.3);
  display: flex;
  align-items: center;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(30, 58, 138, 0.05);
}

/* 暗色模式 */
.dark .el-header {
  background: rgba(30, 41, 59, 0.85);
  border-bottom-color: rgba(147, 197, 253, 0.2);
}
```

#### el-main / el-aside

```css
.el-main {
  background: #f0f9ff;
  padding: 20px;
  min-height: calc(100vh - var(--el-header-height));
}

.el-aside {
  background: transparent;
}

/* 页面容器统一样式 */
.page-container {
  background: #ffffff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: var(--el-box-shadow-light);
}

.page-title {
  margin: 0 0 20px 0;
  font-size: 20px;
  font-weight: 600;
  color: #1e3a8a;
}

.search-section {
  background: #f8fafc;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 16px;
  border: 1px solid #e2e8f0;
}
```

#### el-row / el-col

```css
/* 栅格系统基本不需要覆盖，但可补充间距 */
.el-row {
  --el-row-gap: 20px;
}

.el-col {
  border-radius: var(--el-border-radius-base);
}
```

---

### 2.6 其他组件

#### el-divider

```css
.el-divider {
  --el-divider-bg-color: #e2e8f0;
  --el-divider-text-color: #64748b;
  border-radius: 4px;
}

.el-divider--horizontal {
  margin: 20px 0;
}

.el-divider__text {
  background: #ffffff;
  padding: 0 16px;
  color: #64748b;
  font-weight: 500;
}

.el-divider--vertical {
  margin: 0 16px;
}
```

#### el-avatar

```css
.el-avatar {
  --el-avatar-bg-color: #dbeafe;
  border: 2px solid #ffffff;
  box-shadow: 0 2px 8px rgba(30, 58, 138, 0.1);
}

.el-avatar--circle {
  border-radius: 50%;
}

.el-avatar--square {
  border-radius: 12px;
}
```

#### el-badge

```css
.el-badge__content {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  border: none;
  box-shadow: 0 2px 4px rgba(239, 68, 68, 0.3);
}

.el-badge__content.is-fixed {
  top: 8px;
  right: 12px;
}

.el-badge__content--danger {
  background: linear-gradient(135deg, #ef4444, #dc2626);
}
```

#### el-progress

```css
.el-progress-bar__outer {
  background: #e2e8f0;
  border-radius: 12px;
}

.el-progress-bar__inner {
  background: linear-gradient(90deg, #3b82f6, #1e3a8a);
  border-radius: 12px;
}

.el-progress__text {
  color: #475569;
  font-weight: 500;
}

.el-progress--circle .el-progress__text {
  color: #1e3a8a;
  font-weight: 600;
}
```

---

## 三、主题变量完整清单

```css
:root {
  /* === 主色调 === */
  --el-color-primary: #3b82f6;
  --el-color-primary-light-3: #60a5fa;
  --el-color-primary-light-5: #93c5fd;
  --el-color-primary-light-7: #bfdbfe;
  --el-color-primary-light-8: #dbeafe;
  --el-color-primary-light-9: #eff6ff;
  --el-color-primary-dark-2: #1d4ed8;

  /* === 语义色 === */
  --el-color-success: #10b981;
  --el-color-warning: #f59e0b;
  --el-color-danger: #ef4444;
  --el-color-info: #64748b;

  /* === 文字色 === */
  --el-text-color-primary: #1e293b;
  --el-text-color-regular: #475569;
  --el-text-color-secondary: #64748b;
  --el-text-color-placeholder: #94a3b8;
  --el-text-color-disabled: #cbd5e1;

  /* === 背景色 === */
  --el-bg-color: #ffffff;
  --el-bg-color-page: #f0f9ff;
  --el-bg-color-overlay: rgba(255, 255, 255, 0.9);

  /* === 边框色 === */
  --el-border-color: #bfdbfe;
  --el-border-color-light: #dbeafe;
  --el-border-color-lighter: #eff6ff;
  --el-border-color-extra-light: #f0f9ff;
  --el-border-color-dark: #93c5fd;
  --el-border-color-darker: #60a5fa;

  /* === 填充色 === */
  --el-fill-color: #f0f9ff;
  --el-fill-color-light: #f8fafc;
  --el-fill-color-lighter: #fafafa;
  --el-fill-color-blank: #ffffff;

  /* === 阴影 === */
  --el-box-shadow: 0 4px 16px rgba(30, 58, 138, 0.1);
  --el-box-shadow-light: 0 2px 8px rgba(30, 58, 138, 0.06);
  --el-box-shadow-lighter: 0 1px 4px rgba(30, 58, 138, 0.04);
  --el-box-shadow-dark: 0 8px 32px rgba(30, 58, 138, 0.15);

  /* === 圆角 === */
  --el-border-radius-base: 12px;
  --el-border-radius-small: 8px;
  --el-border-radius-round: 24px;
  --el-border-radius-circle: 50%;

  /* === 字体 === */
  --el-font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  --el-font-size-extra-large: 16px;
  --el-font-size-large: 14px;
  --el-font-size-base: 14px;
  --el-font-size-small: 13px;
  --el-font-size-extra-small: 12px;

  /* === 组件变量 === */
  --el-menu-bg-color: linear-gradient(180deg, #1e3a8a 0%, #3b82f6 100%);
  --el-menu-text-color: rgba(255, 255, 255, 0.9);
  --el-menu-hover-bg-color: #fef3c7;
  --el-menu-active-color: #1e3a8a;
  --el-menu-item-height: 50px;

  --el-header-bg-color: rgba(255, 255, 255, 0.85);
  --el-header-text-color: #1e293b;
  --el-header-height: 60px;

  --el-button-padding-horizontal: 24px;
  --el-button-padding-vertical: 12px;
  --el-button-font-size: 14px;

  /* === 侧边栏 === */
  --sidebar-bg: linear-gradient(180deg, #1e3a8a 0%, #3b82f6 100%);
  --sidebar-width: 240px;

  /* === 过渡 === */
  --el-transition-duration: 0.3s;
  --el-transition-function: cubic-bezier(0.4, 0, 0.2, 1);
}
```

---

## 四、常见踩坑指南

### 1. CSS选择器优先级问题

**问题:** 使用`.el-button`选择器样式不生效

**原因:** Element Plus组件使用BEM命名，且有`scoped`样式隔离

**解决:**
```css
/* 错误 - scoped限制 */
<style scoped>
.el-button { /* 不生效 */ }
</style>

/* 正确 - 使用:deep() */
<style scoped>
:deep(.el-button) { /* 生效 */ }
</style>

/* 或者在全局样式文件中定义 */
```

### 2. CSS变量覆盖不生效

**问题:** 修改`--el-color-primary`后组件颜色没变

**原因:** 某些组件有硬编码颜色或使用其他变量

**解决:**
```css
/* 需要同时覆盖关联变量 */
:root {
  --el-color-primary: #new-color;
  --el-color-primary-light-3: #new-color-light-3;
  --el-color-primary-dark-2: #new-color-dark-2;
}

/* 组件特定变量可能需要单独设置 */
.el-button--primary {
  --el-button-bg-color: #new-color;
  --el-button-border-color: #new-color;
  --el-button-hover-bg-color: #new-hover;
  --el-button-hover-border-color: #new-hover;
}
```

### 3. 渐变与纯色切换

**问题:** 按钮hover效果从渐变变成纯色

**原因:** hover状态没有定义渐变

**解决:**
```css
.el-button--primary:hover {
  background: linear-gradient(135deg, #60a5fa, #1e3a8a) !important;
}
```

### 4. 深色模式冲突

**问题:** 浅色主题样式被深色主题覆盖

**原因:** 样式文件加载顺序问题

**解决:**
```css
/* 确保浅色主题在深色主题之后加载 */
/* 或使用更高优先级的选择器 */
html:not([data-theme="dark"]) .el-button {
  background: linear-gradient(135deg, #3b82f6, #1e3a8a);
}
```

### 5. 第三方组件库冲突

**问题:** wangeditor等第三方编辑器样式与主题不协调

**原因:** 第三方组件使用独立样式系统

**解决:**
```css
/* 在ArticleEditor组件中单独处理 */
.wangEditor-theme {
  --el-border-color: #dbeafe;
  --el-border-radius-base: 12px;
}

/* 或覆盖wangeditor自身样式 */
.w-e-toolbar {
  background: #f8fafc !important;
  border-color: #e2e8f0 !important;
}

.w-e-text-container {
  border-color: #e2e8f0 !important;
}
```

### 6. 响应式样式丢失

**问题:** 移动端组件样式异常

**原因:** 响应式样式没有覆盖

**解决:**
```css
@media (max-width: 768px) {
  :root {
    --sidebar-width: 200px;
    --el-header-height: 56px;
  }

  .el-dialog {
    width: 95% !important;
    margin: 10px auto !important;
  }

  .el-table {
    font-size: 12px;
  }
}
```

### 7. 动画性能问题

**问题:** 全局transition导致性能下降

**原因:** 过度使用通配符transition

**解决:**
```css
/* 移除全局通配符过渡 */
/* 改为按组件或按类定义 */
.no-transition * {
  transition: none !important;
}

/* GPU加速关键动画 */
.transform-gpu {
  transform: translateZ(0);
  will-change: transform;
}
```

---

## 五、实施检查清单

### Phase 1: 核心组件 (必须完成)

- [ ] el-input / el-textarea
- [ ] el-select dropdown
- [ ] el-switch
- [ ] el-radio / el-radio-group
- [ ] el-date-picker
- [ ] el-tree
- [ ] el-card (Login)

### Phase 2: 完善组件 (建议完成)

- [ ] el-empty
- [ ] el-link
- [ ] el-message
- [ ] el-message-box
- [ ] el-loading
- [ ] el-popover / el-tooltip
- [ ] el-descriptions

### Phase 3: 优化组件 (可选)

- [ ] el-divider
- [ ] el-avatar
- [ ] el-badge
- [ ] el-progress
- [ ] el-skeleton

---

## 六、测试验证点

1. **登录页面**: 卡片、按钮、输入框样式一致
2. **用户管理**: 表格、表单、对话框、Switch、Radio全部主题化
3. **角色管理**: 权限树、对话框样式协调
4. **审计日志**: 日期选择器、空状态、表格样式
5. **业务数据**: 富文本编辑器、分类树样式融入主题
6. **响应式**: 768px以下断点样式正常

---

## 参考资料

- [Element Plus Theming Guide](https://element-plus.org/en-US/guide/theming.html) - HIGH confidence
- [Element Plus Component Docs](https://element-plus.org/en-US/component/overview) - HIGH confidence
- [CSS Variables Reference](https://developer.mozilla.org/en-US/docs/Web/CSS/Using_CSS_custom_properties) - HIGH confidence
