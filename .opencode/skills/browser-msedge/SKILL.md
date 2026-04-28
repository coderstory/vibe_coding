# Browser Automation (msedge)

使用 msedge 浏览器的自动化方案，解决国内环境没有 Chrome 的问题。

## 触发条件

当需要执行以下操作时**必须**使用此 skill：
- 访问动态渲染网页（Next.js/React/Vue SPA）
- 需要 JavaScript 执行后才能获取完整内容
- 网页内容需要等待渲染

## 使用方法

### 1. 安装依赖

```powershell
# 在项目目录安装 playwright
npm install playwright
```

### 2. 执行脚本

创建并运行 fetch 脚本：

```powershell
# 创建脚本
@"
const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.launch({ channel: 'msedge' });
  const page = await browser.newPage();
  await page.goto('URL');
  await page.waitForLoadState('networkidle');
  console.log(await page.content());
  await browser.close();
})();
"@ -replace 'URL', 'YOUR_URL' | Out-File -FilePath fetch-page.js -Encoding UTF8

# 执行
node fetch-page.js
```

### 3. 常用操作

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

## 完整脚本模板

```javascript
const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({ channel: 'msedge' });
  const page = await browser.newPage();

  // 设置视口
  await page.setViewportSize({ width: 1920, height: 1080 });

  // 访问页面
  await page.goto('YOUR_URL_HERE', { waitUntil: 'networkidle' });

  // 获取内容
  const content = await page.content();
  const title = await page.title();

  console.log('=== TITLE ===');
  console.log(title);
  console.log('\n=== CONTENT ===');
  console.log(content.substring(0, 20000)); // 限制输出长度

  await browser.close();
})();
```

## 判断是否需要浏览器

| 情况 | 工具选择 |
|------|----------|
| 静态 HTML/文本响应 | `webfetch` ✅ |
| API JSON 响应 | `webfetch` ✅ |
| Next.js/React/Vue 动态页 | Playwright + msedge ✅ |
| 需要登录后的内容 | Playwright + msedge ✅ |
| 需要执行 JS 才能显示 | Playwright + msedge ✅ |

## 常见问题

**Q: node 找不到 playwright 模块**
```powershell
npm install playwright --save-dev
```

**Q: msedge channel 报错**
确保 msedge 安装路径正确，通常在：
`C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe`

**Q: 页面内容获取不完整**
尝试增加等待时间：
```javascript
await page.waitForLoadState('networkidle', { timeout: 30000 })
// 或
await page.waitForTimeout(3000) // 等待3秒
```
