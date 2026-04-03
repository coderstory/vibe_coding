# Ocean Breeze Admin - 夏日海滩风主题路线图

> **创建日期:** 2026-04-03  
> **项目:** v1.1 夏日海滩风主题修复与完善  
> **目标:** 完成海滩主题的 CSS 动画效果和组件样式覆盖

---

## 阶段总览

| 阶段 | 名称 | 任务数 | 优先级 |
|------|------|--------|--------|
| Phase 1 | 核心组件样式修复 | 11 | HIGH |
| Phase 2 | 动画基础设施搭建 | 5 | MEDIUM |
| Phase 3 | 动画效果集成 | 4 | MEDIUM |
| Phase 4 | 毛玻璃与无障碍优化 | 6 | LOW |

---

## Phase 1: 核心组件样式修复

**目标:** 修复弹窗显示问题，调整颜色配置，完成高优先级 Element Plus 组件样式覆盖

**依赖:** 无

**文件修改:**
- `app-vue/src/assets/themes/enterprise-theme.css`

### 任务 1.1: 修复阳光金色配色 (UI-06)

**问题:** `--sun: #fbbf24` 过亮，应改为琥珀色 `#d97706`

- [ ] 将 `--sun: #fbbf24` 改为 `--sun: #d97706`
- [ ] 将 `--sun-light: #fcd34d` 改为 `--sun-light: #fbbf24`
- [ ] 检查所有使用 `#fbbf24` 的地方并替换

### 任务 1.2: 修复 el-dialog 弹窗样式 (UI-05)

**问题:** 弹窗显示样式不完整，需要完整覆盖

- [ ] 添加 `.el-dialog__headerbtn` (关闭按钮) 样式
- [ ] 添加 `.el-dialog__footer` 样式
- [ ] 添加 `.el-overlay` 遮罩层样式 (`background: rgba(30, 58, 138, 0.5)`)
- [ ] 添加 `.el-message-box` 样式覆盖
- [ ] 验证 dialog 圆角、阴影、背景完整性

### 任务 1.3: 完成 el-card 卡片样式 (COMP-01)

**文件:** `enterprise-theme.css`

- [ ] 添加 `.el-card` 完整样式 (header, body, footer)
- [ ] 添加卡片悬停效果 (轻微上浮 + 阴影增强)
- [ ] 添加斑马纹样式变体

### 任务 1.4: 完成 el-tree 树形组件样式 (COMP-02)

**文件:** `enterprise-theme.css`

- [ ] 添加 `.el-tree-node__content` 样式 (背景、悬停)
- [ ] 添加 `.el-tree-node__label` 样式
- [ ] 添加 `.el-tree-node__expand-icon` 样式
- [ ] 添加选中状态样式 (沙滩色背景)
- [ ] 添加禁用状态样式

### 任务 1.5: 完成 el-switch 开关组件样式 (COMP-03)

**文件:** `enterprise-theme.css`

- [ ] 覆盖 `--el-switch-off-color` 为海洋蓝色系
- [ ] 覆盖 `--el-switch-on-color` 为琥珀色系
- [ ] 添加开关过渡动画
- [ ] 添加 disabled 状态样式

### 任务 1.6: 完成 el-radio 单选框样式 (COMP-04)

**文件:** `enterprise-theme.css`

- [ ] 添加 `.el-radio__input.is-checked` 样式
- [ ] 添加 `.el-radio__inner` 样式 (海洋蓝边框)
- [ ] 添加 `.el-radio__label` 选中态样式
- [ ] 添加 disabled 状态样式

### 任务 1.7: 完成 el-date-picker 日期选择器样式 (COMP-05)

**文件:** `enterprise-theme.css`

- [ ] 添加 `.el-date-picker` 面板样式
- [ ] 添加 `.el-date-table` 单元格样式
- [ ] 添加当前日期、选中日期、悬停状态
- [ ] 添加 `.el-picker-panel__footer` 样式 (底部按钮)

### 任务 1.8: 完成中优先级组件样式 (COMP-06 ~ COMP-09)

**文件:** `enterprise-theme.css`

- [ ] **el-empty:** 添加空状态图标和文字样式
- [ ] **el-link:** 添加链接颜色和悬停效果
- [ ] **el-message-box:** 添加确认弹框样式
- [ ] **el-loading:** 添加加载指示器样式

### 任务 1.9: 优化表格行悬停效果 (ANIM-01)

**文件:** `enterprise-theme.css`

- [ ] 增强 `.el-table__row:hover > td` 过渡效果
- [ ] 添加沙滩色渐变背景动画
- [ ] 添加 `transform: translateX(2px)` 位移效果

### 任务 1.10: 优化按钮悬停动画 (ANIM-02)

**文件:** `enterprise-theme.css`

- [ ] 增强所有按钮类型的悬停效果
- [ ] 添加 `translateY(-2px)` 上浮
- [ ] 添加阴影扩散动画

### 任务 1.11: 优化表单标签样式

**文件:** `enterprise-theme.css`

- [ ] 修复 `.el-form-item__label::before` 颜色为琥珀色
- [ ] 添加必填标记样式

---

## Phase 2: 动画基础设施搭建

**目标:** 创建模块化 CSS 文件结构和动画开关控制系统

**依赖:** Phase 1 完成

**文件变更:**
- 创建 `app-vue/src/assets/themes/animations/` 目录
- 创建 4 个模块化 CSS 文件
- 创建 `useAnimationToggle.js` composable

### 任务 2.1: 创建动画目录结构

**创建文件:**
- `app-vue/src/assets/themes/animations/_keyframes.css`
- `app-vue/src/assets/themes/animations/_wave.css`
- `app-vue/src/assets/themes/animations/_bubble.css`
- `app-vue/src/assets/themes/animations/_utilities.css`

### 任务 2.2: 迁移并增强 Keyframes (_keyframes.css)

**内容:**
- [ ] 迁移 `wave` 动画 (修复为无缝循环)
- [ ] 迁移 `float` 动画
- [ ] 迁移 `pulse` 动画
- [ ] 添加 `bubble-rise` 动画 (气泡上升)
- [ ] 添加 `shimmer` 动画
- [ ] 添加 `wave-float` 动画 (组合)

### 任务 2.3: 创建海浪动画模块 (_wave.css)

**内容:**
- [ ] 创建 `.wave-container` 容器样式
- [ ] 创建 `.wave-layer` 多层海浪样式 (3层，不同透明度)
- [ ] 创建 `.wave-cubic` 波浪形状样式
- [ ] 添加响应式配置 (移动端简化)

### 任务 2.4: 创建气泡动画模块 (_bubble.css)

**内容:**
- [ ] 创建 `.bubble-container` 气泡容器样式
- [ ] 创建 `.bubble` 基础气泡样式 (尺寸、位置、动画)
- [ ] 创建 8-12 个气泡位置变体
- [ ] 添加随机延迟类 `.bubble-delay-{1-6}`

### 任务 2.5: 创建动画开关系统

**创建文件:**
- `app-vue/src/composables/useAnimationToggle.js`

**内容:**
```javascript
// composable 功能:
- animationsEnabled: ref (响应式状态)
- toggleAnimations(): 切换函数
- setAnimations(value): 设置函数
- 监听 prefers-reduced-motion 媒体查询
- 监听系统主题变化
```

**CSS 变量:**
```css
:root {
  --animation-enabled: 1;
  --wave-animation-duration: 8s;
  --bubble-animation-duration: 6s;
}
```

---

## Phase 3: 动画效果集成

**目标:** 将海浪和气泡动画集成到布局组件中

**依赖:** Phase 2 完成

**文件修改:**
- `app-vue/src/views/layout/Layout.vue`
- `app-vue/src/components/AppHeader.vue`
- `app-vue/src/assets/themes/enterprise-theme.css`

### 任务 3.1: 在 Layout.vue 集成海浪背景

**文件:** `app-vue/src/views/layout/Layout.vue`

- [ ] 导入 wave CSS 模块
- [ ] 添加 `.wave-container` 到侧边栏背景
- [ ] 配置 3 层海浪 (不同动画速度)
- [ ] 设置 z-index 层级

### 任务 3.2: 在 Layout.vue 集成气泡效果

**文件:** `app-vue/src/views/layout/Layout.vue`

- [ ] 导入 bubble CSS 模块
- [ ] 添加 `.bubble-container` 到页面容器
- [ ] 配置 8-12 个气泡元素
- [ ] 添加随机延迟

### 任务 3.3: 在 AppHeader.vue 添加动画开关

**文件:** `app-vue/src/components/AppHeader.vue`

- [ ] 导入 `useAnimationToggle` composable
- [ ] 在顶栏添加切换按钮 (图标)
- [ ] 连接 toggleAnimations 函数
- [ ] 添加 tooltip 提示

### 任务 3.4: 添加路由切换过渡动画

**文件:** `app-vue/src/assets/themes/enterprise-theme.css`

- [ ] 添加 Vue transition 样式 (fade, slide)
- [ ] 添加 `.fade-enter-active`, `.fade-leave-active`
- [ ] 添加 `.slide-enter-active`, `.slide-leave-active`

---

## Phase 4: 毛玻璃与无障碍优化

**目标:** 增强 glassmorphism 效果，添加无障碍支持

**依赖:** Phase 3 完成

**文件修改:**
- `app-vue/src/assets/themes/enterprise-theme.css`
- `app-vue/src/assets/themes/animations/_glass.css` (新建)

### 任务 4.1: 创建毛玻璃效果模块 (_glass.css)

**创建文件:** `app-vue/src/assets/themes/animations/_glass.css`

**内容:**
- [ ] 创建 `.glass` 基础类
- [ ] 创建 `.glass-card` 卡片毛玻璃类
- [ ] 创建 `.glass-dialog` 对话框毛玻璃类
- [ ] 创建 `.glass-header` 顶栏毛玻璃类
- [ ] 添加 `-webkit-backdrop-filter` 前缀
- [ ] 添加 `@supports` 兼容性检测

### 任务 4.2: 应用毛玻璃效果到关键组件

**文件:** `enterprise-theme.css`

- [ ] 将 `.macos-card` 改为使用 `.glass-card`
- [ ] 将 `.el-dialog` 添加毛玻璃效果
- [ ] 将 `.el-header` (AppHeader) 添加毛玻璃效果
- [ ] 增强 `.el-dropdown-menu` 毛玻璃效果

### 任务 4.3: 优化移动端毛玻璃性能

**文件:** `app-vue/src/assets/themes/animations/_glass.css`

- [ ] 添加 `@media (max-width: 768px)` 断点
- [ ] 减少移动端模糊半径 (blur(4px) 而非 10px)
- [ ] 添加移动端性能优化注释

### 任务 4.4: 完成低优先级组件样式 (COMP-10 ~ COMP-13)

**文件:** `enterprise-theme.css`

- [ ] **el-divider:** 分隔线颜色和样式
- [ ] **el-avatar:** 头像圆形边框和悬停效果
- [ ] **el-badge:** 徽章颜色和动画
- [ ] **el-progress:** 进度条渐变色

### 任务 4.5: 添加 prefers-reduced-motion 支持 (ANIM-04)

**文件:** `enterprise-theme.css`

- [ ] 添加 `@media (prefers-reduced-motion: reduce)` 媒体查询
- [ ] 禁用所有动画 (animation: none)
- [ ] 保留基本颜色和布局过渡

### 任务 4.6: 添加 z-index 层级审查

**文件:** `enterprise-theme.css`

- [ ] 审查所有 z-index 值
- [ ] 记录层级文档注释
- [ ] 确保 modal/overlay 在正确层级

---

## 执行顺序

```
Phase 1 (11 tasks) → Phase 2 (5 tasks) → Phase 3 (4 tasks) → Phase 4 (6 tasks)
     ↓                      ↓                      ↓               ↓
  独立执行              依赖 Phase 1         依赖 Phase 2      依赖 Phase 3
```

---

## 质量标准

每个阶段完成后需验证:
- [ ] 页面无 console error
- [ ] 动画流畅 (60fps)
- [ ] 暗色模式下无异常
- [ ] `prefers-reduced-motion` 生效
- [ ] 响应式布局正常

---

## 风险与缓解

| 风险 | 级别 | 缓解措施 |
|------|------|----------|
| CSS 特异性冲突 | MEDIUM | 使用 `.beach-theme` 包裹选择器 |
| 动画性能问题 | MEDIUM | 使用 GPU 加速属性 (transform, opacity) |
| Element Plus 版本升级破坏样式 | LOW | 锁定 Element Plus 版本 |
| 移动端毛玻璃性能 | MEDIUM | 减少模糊半径，添加 @supports 检测 |

---

*路线图创建完成: 2026-04-03*
*等待用户批准后开始执行*
