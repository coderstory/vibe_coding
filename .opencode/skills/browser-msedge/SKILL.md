# Browser Automation (msedge)

使用 msedge 浏览器的自动化方案，解决国内环境没有 Chrome 的问题。

## 触发条件

当需要执行以下操作时**必须**使用此 skill：
- 访问动态渲染网页（Next.js/React/Vue SPA）
- 需要 JavaScript 执行后才能获取完整内容
- 网页内容需要等待渲染

## 快速开始

### 1. 安装依赖

```powershell
npm install playwright
```

### 2. 执行脚本

```powershell
# 获取网页内容并输出到控制台
node .opencode/skills/browser-msedge/fetch-page.js <URL>

# 获取网页内容并保存到文件
node .opencode/skills/browser-msedge/fetch-page.js <URL> output.txt
```

## 脚本说明

**文件**: `.opencode/skills/browser-msedge/fetch-page.js`

| 参数 | 说明 |
|------|------|
| `<URL>` | 必填，要获取的网页地址 |
| `output.txt` | 可选，保存到的文件名 |

**输出格式**:
```
=== TITLE: 网页标题 ===

=== CONTENT ===
[完整 HTML 内容]
```

## 常用操作

```javascript
// 等待特定元素
await page.waitForSelector('.content')

// 截图
await page.screenshot({ path: 'screenshot.png', fullPage: true })

// 获取文本内容
const text = await page.textContent('body')

// 执行自定义 JS
const result = await page.evaluate(() => document.title)
```

## 判断是否需要浏览器

| 情况 | 工具选择 |
|------|----------|
| 静态 HTML/文本响应 | `webfetch` ✅ |
| API JSON 响应 | `webfetch` ✅ |
| Next.js/React/Vue 动态页 | 本脚本 + msedge ✅ |
| 需要登录后的内容 | 本脚本 + msedge ✅ |
| 需要执行 JS 才能显示 | 本脚本 + msedge ✅ |

## 常见问题

**Q: node 找不到 playwright 模块**
```powershell
npm install playwright --save-dev
```

**Q: msedge channel 报错**
确保 msedge 安装路径正确，通常在：
`C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe`

**Q: 页面内容获取不完整**
脚本默认等待 `networkidle` 状态（最多30秒），如仍不完整可修改脚本中的超时时间。
