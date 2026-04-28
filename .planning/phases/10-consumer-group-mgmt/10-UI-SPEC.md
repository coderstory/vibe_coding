---
phase: 10
slug: consumer-group-mgmt
status: draft
shadcn_initialized: false
preset: none
created: 2026-04-28
---

# Phase 10 — UI Design Contract

> Visual and interaction contract for frontend phases. Generated based on Phase 9 patterns and Phase 10 context.

---

## Design System

| Property | Value |
|----------|-------|
| Tool | none |
| Preset | not applicable |
| Component library | Element Plus |
| Icon library | @element-plus/icons-vue |
| Font | Inter, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif |

---

## Spacing Scale

Declared values (must be multiples of 4):

| Token | Value | Usage |
|-------|-------|-------|
| xs | 4px | Icon gaps, inline padding |
| sm | 8px | Compact element spacing |
| md | 16px | Default element spacing |
| lg | 24px | Section padding |
| xl | 32px | Layout gaps |
| 2xl | 48px | Major section breaks |
| 3xl | 64px | Page-level spacing |

Exceptions: none

---

## Typography

| Role | Size | Weight | Line Height |
|------|------|--------|-------------|
| Body | 14px | 400 | 1.6 |
| Label | 14px | 500 | 1.4 |
| Heading | 20px | 600 | 1.3 |
| Display | 24px | 600 | 1.2 |

---

## Color

| Role | Value | Usage |
|------|-------|-------|
| Dominant (60%) | #ffffff | Background, surfaces |
| Secondary (30%) | #f5f7fa | Cards, sidebar, nav, search section |
| Accent (10%) | #409eff | Primary actions, links, active states |
| Destructive | #f56c6c | Destructive actions only |

Accent reserved for: Primary CTA buttons, active navigation items, selected states

---

## Copywriting Contract

| Element | Copy |
|---------|------|
| Primary CTA | 新建 Group (不适用，本 phase 不创建) |
| Empty state heading | 暂无 Consumer Group |
| Empty state body | 当前 RocketMQ 集群中没有 Consumer Group |
| Error state | 获取数据失败，请稍后重试 |
| Reset confirmation | 确定要重置消费位点吗？此操作不可撤销。 |
| Reset success | 位点重置成功 |
| Reset error | 位点重置失败 |

---

## Responsive Design

**Target Resolutions:**
| Resolution | Width | Target |
|------------|-------|--------|
| HD 1080p | 1920×1080 | Primary target — 完美支持 |
| QHD 2K | 2560×1440 | Primary target — 完美支持 |
| 720p and below | ≤1366px | graceful degradation |

**Breakpoints:**
| Breakpoint | Width | Behavior |
|------------|-------|----------|
| ≥1920px | 1920px+ | Full layout, all columns visible |
| 1440px–1919px | 1440px–1919px | Full layout, slight horizontal scroll allowed |
| 1280px–1439px | 1280px–1439px | Hide less critical columns (accumulatedDiff) |
| 1024px–1279px | 1024px–1279px | Search form stacks vertically, table horizontal scroll |

**Table Responsive Strategy:**
- `group`: always visible, min-width 200px
- `groupType`: always visible, 100px fixed
- `status`: always visible, 100px fixed
- `consumerCount`: always visible, 100px fixed
- `accumulatedDiff`: hidden below 1440px
- `操作列`: always visible, fixed right

**Dialog Responsive:**
- Width 600px (detail) / 500px (reset) — max-width, centered
- Below 768px: dialog becomes full-width with 16px margin

**Container Strategy:**
```css
.page-container {
  width: 100%;
  min-height: calc(100vh - var(--header-height) - var(--sidebar-height));
  overflow-x: auto;
}
```

**Viewport Units:**
- Use `vh`/`vw` for major layout calculations
- Use `px` for component-level spacing (precision required for tables)
- Use CSS Grid with `minmax()` for fluid table columns:
  ```css
  .consumer-group-table {
    display: grid;
    grid-template-columns: 60px minmax(200px, 1fr) 100px 100px 100px 120px 180px;
  }
  ```

**Font Scaling (optional enhancement):**
- Below 1280px: body font may scale down to 13px if needed
- Never scale below 12px

---

## Registry Safety

| Registry | Blocks Used | Safety Gate |
|----------|-------------|-------------|
| Element Plus (official) | el-table, el-dialog, el-form, el-input, el-select, el-button, el-pagination, el-tabs, el-date-picker, el-message, el-message-box, el-tag, el-descriptions | not required |

---

## Component Inventory

### Consumer Group List Page (src/views/rocketmq/ConsumerGroupList.vue)

**Layout:**
- Page container with white background, 20px padding, 4px border-radius
- Page title "Consumer Group 管理" at top (20px, weight 600, color #303133)
- Search section with #f5f7fa background, 20px padding, 16px margin-bottom
- No action section (no create in this phase)
- el-table with stripe border
- Pagination right-aligned at bottom

**Table Columns:**
| Column | Prop | Width | Notes |
|--------|------|-------|-------|
| 序号 | index | 60px | center aligned |
| Group 名称 | group | min-width 200px | click to open detail |
| 类型 | groupType | 100px | Tag: 广播(BROADCASTING, blue)/集群(CLUSTERING, purple) |
| 状态 | status | 100px | Tag: 正常(OK, success)/重试中(REBALANCE_NOT_INIT, warning)/离线(OFFLINE, danger) |
| 消费者数 | consumerCount | 100px | center aligned |
| 堆积量 | accumulatedDiff | 120px | center aligned, 万为单位 |
| 操作 | - | 120px | fixed right: 查看 |

**Search Form:**
- Group 名称: el-input, placeholder="输入 Group 名称搜索", width 200px
- 查询/重置 buttons

**Status Tag Mapping:**
| 状态值 | 标签文字 | 类型 | 颜色 |
|--------|----------|------|------|
| OK | 正常 | success | #67c23a |
| REBALANCE_NOT_INIT | 重试中 | warning | #e6a23c |
| OFFLINE | 离线 | danger | #f56c6c |

**Group Type Tag Mapping:**
| 类型值 | 标签文字 | 类型 | 颜色 |
|--------|----------|------|------|
| BROADCASTING | 广播 | primary | #409eff |
| CLUSTERING | 集群 | - | #8e44ad |

### Consumer Group Detail Dialog (src/views/rocketmq/ConsumerGroupDetail.vue)

**Trigger:** Table "查看" action

**Width:** 800px

**Content:** el-tabs with 3 tabs

**Tab 1: 消费进度 (ConsumeProgress)**
- Table showing per-topic consumption progress
- Columns: Topic 名称, 队列 ID, 消费位点, 存储位点, 堆积量
- Use el-table with max-height for scroll

**Tab 2: 订阅关系 (Subscription)**
- Table showing subscribed topics
- Columns: Topic 名称, 过滤表达式, 起始位置

**Tab 3: 位点信息 (CurrentOffset)**
- Card layout showing each queue's consumption status
- Display: consumer online status, last update time

**Footer Buttons:**
- 重置位点 (仅 CLUSTERING 类型可见)
- 关闭

### Reset Offset Dialog (src/views/rocketmq/ResetOffsetDialog.vue)

**Trigger:** "重置位点" button in detail dialog

**Width:** 500px

**Form Fields:**
| Field | Type | Validation | Notes |
|-------|------|------------|-------|
| Topic | el-select | required | dropdown of subscribed topics |
| 重置时间 | el-date-picker | required | datetime picker, supports date and time selection |
| 强制重置 | el-checkbox | optional | force reset even if consumers online |

**Constraints:**
- Only show for CLUSTERING type groups
- BROADCASTING groups show tooltip: "广播模式不支持位点重置"

**Footer:** 取消 / 确定 buttons

---

## Interaction Patterns

### List Loading
1. Show v-loading on table
2. On error: ElMessage.error with friendly message
3. Empty state: show empty placeholder with illustration

### View Detail Flow
1. Click Group name or "查看" → detail dialog opens with first tab active
2. Tab switching for different information views
3. "重置位点" button visible only for CLUSTERING type
4. Dialog close → return to list

### Reset Offset Flow
1. Click "重置位点" → reset dialog opens
2. Select Topic from dropdown (subscribed topics only)
3. Select datetime for reset target
4. Optional: check "强制重置"
5. Click "确定" → POST /api/rocketmq/consumer-groups/{group}/reset-offset
6. Success: ElMessage.success, both dialogs close, list reloads
7. Error: ElMessage.error with server message (e.g., "广播模式不支持位点重置")

---

## Checker Sign-Off

- [ ] Dimension 1 Copywriting: PASS
- [ ] Dimension 2 Visuals: PASS
- [ ] Dimension 3 Color: PASS
- [ ] Dimension 4 Typography: PASS
- [ ] Dimension 5 Spacing: PASS
- [ ] Dimension 6 Registry Safety: PASS
- [ ] Dimension 7 Responsive Design: PASS

**Approval:** pending