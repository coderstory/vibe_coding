---
phase: 11
slug: msg-mgmt
status: draft
shadcn_initialized: false
preset: none
created: 2026-04-29
---

# Phase 11 — UI Design Contract

> Visual and interaction contract for frontend phases. Generated for 消息管理 (Message Management) feature.

---

## Design System

| Property | Value |
|----------|-------|
| Tool | none |
| Preset | not applicable |
| Component library | Element Plus |
| Icon library | @element-plus/icons-vue |
| Font | Inter, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif |

**Theme alignment**: 夏日海滩风 (Summer Beach Theme)
- Primary: #409eff (海洋蓝)
- Secondary: #f5f7fa (沙滩色)
- Accent: #e6a23c (琥珀色) — reserved for highlights
- Background: #ffffff

---

## Spacing Scale

Declared values (multiples of 4):

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
| Monospace (msgId/keys) | 13px | 400 | 1.4 |

---

## Color

| Role | Value | Usage |
|------|-------|-------|
| Dominant (60%) | #ffffff | Page background, dialog backgrounds |
| Secondary (30%) | #f5f7fa | Card backgrounds, table header, search section |
| Accent (10%) | #409eff | Primary buttons, active states, links |
| Destructive | #f56c6c | Delete actions, error states |

Accent reserved for: Primary CTA buttons only, active tab indicators, required field markers.

---

## Copywriting Contract

| Element | Copy |
|---------|------|
| Primary CTA | 查询消息 |
| Secondary CTA | 重置 |
| Empty state heading | 暂无消息 |
| Empty state body | 请选择 Topic 和时间范围后点击"查询消息"按钮 |
| Error state (no broker) | 未找到可用的 Broker，请检查 RocketMQ 服务状态 |
| Error state (query failed) | 查询失败：{reason}，请重试 |
| Error state (time range) | 时间范围不能超过 7 天 |
| Detail dialog title | 消息详情 - {msgId} |
| Trace dialog title | 消息轨迹 - {msgId} |
| Properties label | 属性 |
| Body label | 消息内容 |
| View full button | 查看完整内容 |
| Collapse button | 收起 |

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
| 1280px–1439px | 1280px–1439px | QueueOffset column hidden |
| 1024px–1279px | 1024px–1279px | Search form stacks vertically, table horizontal scroll |

**Dialog Responsive:**
- Width: 600px (detail) / 700px (trace) — max-width, centered
- Below 768px: dialog becomes full-width with 16px margin

**Container Strategy:**
```css
.page-container {
  width: 100%;
  min-height: calc(100vh - var(--header-height) - var(--sidebar-height));
  overflow-x: auto;
}
```

---

## Registry Safety

| Registry | Blocks Used | Safety Gate |
|----------|-------------|-------------|
| Element Plus (official) | el-table, el-dialog, el-form, el-input, el-select, el-button, el-pagination, el-date-picker, el-message, el-message-box, el-tag, el-descriptions, el-empty, el-loading | not required |

---

## Layout & Structure

### Message List Page

```
┌─────────────────────────────────────────────────────────┐
│  RocketMQ 管理 / 消息查询                    (breadcrumb) │
├─────────────────────────────────────────────────────────┤
│  搜索区域 (#f5f7fa background, 20px padding)             │
│  ┌─────────────────────────────────────────────────────┐ │
│  │ Topic: [下拉选择 ▼]   时间: [日期时间范围选择    ]  │ │
│  │                              [查询消息] [重置]      │ │
│  └─────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────┤
│  消息列表 (el-table)                                    │
│  ┌───────┬──────┬────────┬─────────────────┬───────┐   │
│  │消息ID │ Tags │ Keys   │ 时间            │操作   │   │
│  ├───────┼──────┼────────┼─────────────────┼───────┤   │
│  │0A...  │ tag1 │ key1   │ 2026-04-29 10:00│ 详情  │   │
│  │0B...  │ tag2 │ key2   │ 2026-04-29 10:05│ 详情  │   │
│  └───────┴──────┴────────┴─────────────────┴───────┘   │
│                                                         │
│  分页: [< 1 2 3 ... 10 >]  每页 100 条                  │
└─────────────────────────────────────────────────────────┘
```

### Message Detail Dialog (600px)

```
┌─────────────────────────────────────────────────┐
│  消息详情 - 0A1234567890                   [X]   │
├─────────────────────────────────────────────────┤
│  消息ID     │ 0A1234567890...                   │
│  Topic      │ my-topic                          │
│  Tags       │ tag1                               │
│  Keys       │ key1                               │
│  时间       │ 2026-04-29 10:00:00               │
│  QueueId    │ 0                                  │
│  Offset     │ 12345678                          │
├─────────────────────────────────────────────────┤
│  属性 (el-descriptions, 2-column)               │
│  ┌──────────┬──────────────────────────────────┐│
│  │ traceId  │ abc123                           ││
│  │ msgType  │ normal                           ││
│  └──────────┴──────────────────────────────────┘│
├─────────────────────────────────────────────────┤
│  消息内容                                     │
│  ┌──────────────────────────────────────────┐  │
│  │ 这是消息内容...（已截断）                │  │
│  │                                          │  │
│  │                     [查看完整内容]        │  │
│  └──────────────────────────────────────────┘  │
│                                                 │
│                              [关闭]             │
└─────────────────────────────────────────────────┘
```

### Message Trace Dialog (700px)

```
┌─────────────────────────────────────────────────────────┐
│  消息轨迹 - 0A1234567890                           [X]  │
├─────────────────────────────────────────────────────────┤
│  轨迹表格 (el-table)                                    │
│  ┌────────────────┬────────┬────────────────────────┐ │
│  │ Consumer Group │ 状态   │ 消费时间                │ │
│  ├────────────────┼────────┼────────────────────────┤ │
│  │ my-consumer-1  │ 成功   │ 2026-04-29 10:01:23    │ │
│  │ my-consumer-2  │ 成功   │ 2026-04-29 10:02:15    │ │
│  └────────────────┴────────┴────────────────────────┘ │
│                                                           │
│  说明: 轨迹数据来自 RMQ_SYS_TRACE_TOPIC                   │
│  (灰色小字, 12px, color: #909399)                         │
│                                      [关闭]               │
└─────────────────────────────────────────────────────────┘
```

---

## Component Specifications

### Search Form (el-form inline)

| Element | Specification |
|---------|---------------|
| Topic 选择 | el-select, filterable, placeholder="选择 Topic", width 200px |
| 时间范围 | el-date-picker, type="datetimerange", width 340px |
| 查询按钮 | el-button type="primary", text="查询消息" |
| 重置按钮 | el-button, text="重置" |
| Layout | flex, justify-end, gap 12px |

### Message Table (el-table)

| Column | Prop | Width | Notes |
|--------|------|-------|-------|
| 消息ID | msgId | 200px | monospace font, click to open detail |
| Tags | tags | 120px | ellipsis overflow |
| Keys | keys | 150px | monospace font, ellipsis overflow |
| 时间 | timestamp | 180px | format: YYYY-MM-DD HH:mm:ss |
| 操作 | - | 100px | "详情" button |

**Table settings:**
- stripe: disabled (clean look)
- border: true
- row-click: opens detail dialog
- v-loading: true during fetch

### Detail Dialog (el-dialog)

| Property | Value |
|----------|-------|
| Width | 600px |
| Title | 消息详情 - {msgId} |
| Modal | true |
| Close-on-click-modal | false |

**Content:**
- Basic info: el-descriptions, 2-column layout, label-width 80px
- Properties: el-descriptions below basic info
- Body: el-input type="textarea", readonly, :rows="4", max 200 chars shown with "查看完整内容" link

### Trace Dialog (el-dialog)

| Property | Value |
|----------|-------|
| Width | 700px |
| Title | 消息轨迹 - {msgId} |
| Modal | true |

**Table columns:**
| Column | Prop | Width |
|--------|------|-------|
| Consumer Group | consumerGroup | min-width 200px |
| 状态 | status | 100px (el-tag) |
| 消费时间 | consumeTime | 180px |

**Footer note:** 灰色小字说明 (12px, color #909399)

### Pagination (el-pagination)

| Property | Value |
|----------|-------|
| Page size | 100 (fixed) |
| Layout | prev, pager, next, jumper |
| Background | true |
| Align | right |

---

## Interaction Behaviors

| Action | Behavior |
|--------|----------|
| Select Topic + time range → Click 查询 | Call getMessageList API, show loading, display results |
| Click 重置 | Clear form, reset table to empty state |
| Click 消息ID or 详情 button | Open detail dialog, call getMessageDetail |
| Click 轨迹 button | Open trace dialog, call getMessageTrace |
| Time range > 7 days | Form validation fails, show error message |
| Query fails | ElMessage.error, table keeps last successful data |
| Empty result | Show el-empty with "暂无消息" heading |

---

## Validation Rules

| Field | Rule | Error Message |
|-------|------|---------------|
| Topic | 必选 | 请选择 Topic |
| 时间范围 | 必选，跨度 ≤ 7 天 | 请选择时间范围 / 时间范围不能超过 7 天 |

---

## States

### Loading State
- Table: v-loading="true", skeleton or spinner
- Query button: loading="true", text changes to "查询中..."
- Clear form controls during loading

### Empty State
- Table: el-empty with illustration
- Heading: "暂无消息"
- Body: "请选择 Topic 和时间范围后点击"查询消息"按钮"
- Pagination: hidden

### Error State
- ElMessage.error with specific error message
- Table keeps last successful data (no clear)
- Error messages:
  - No broker: "未找到可用的 Broker，请检查 RocketMQ 服务状态"
  - Query failed: "查询失败：{reason}，请重试"
  - Time range: "时间范围不能超过 7 天"

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
