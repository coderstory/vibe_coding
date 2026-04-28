# 浏览器自动化指南

## 动态网页获取策略

### 问题描述

OpenCode 的 Playwright MCP 默认查找 Chrome 浏览器：

```
Chromium distribution 'chrome' is not found at C:\Users\coder\AppData\Local\Google\Chrome\Application\chrome.exe
```

但国内用户环境通常只有 **msedge**（Edge），没有安装 Chrome。

---

## 解决方案

### 方案一：安装 Chrome（推荐）

```powershell
# 在项目目录下执行
npm install @playwright/test
npx playwright install chrome
```

### 方案二：使用 Node.js 脚本 + msedge

当 MCP 无法使用时，用临时脚本直接调用 playwright 的 msedge channel：

```javascript
// fetch-page.js
const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({ channel: 'msedge' });
  const page = await browser.newPage();
  await page.goto('https://example.com');
  await page.waitForLoadState('networkidle');
  const content = await page.content();
  console.log(content);
  await browser.close();
})();
```

```powershell
# 执行脚本
node fetch-page.js
```

---

## 使用场景对比

| 工具 | 适用场景 | 限制 |
|------|----------|------|
| **webfetch** | 静态网页、API响应 | 无法处理动态渲染（CSR） |
| **Playwright MCP** | 动态渲染页面（需 Chrome） | 需要 Chrome 浏览器 |
| **Node.js + msedge** | 动态渲染页面（无 Chrome） | 需要临时脚本 |

---

## 动态渲染判断

以下类型页面**必须**用浏览器工具：

- Next.js / Nuxt.js 应用
- React / Vue SPA
- 需要 JavaScript 执行才显示内容
- 调用了 `document.write()` 或 innerHTML 动态注入

**简单判断**：
- 右键查看网页源代码，如果内容很少但浏览器显示很多 → 动态渲染
- 或者直接用 `webfetch` 试试，如果内容不完整 → 必须用 Playwright

---

## 自动安装脚本

如需在项目目录安装 playwright 并使用 msedge：

```powershell
# 安装 playwright
npm install playwright

# 创建 fetch 脚本
echo 'const { chromium } = require("playwright");
(async () => {
  const browser = await chromium.launch({ channel: "msedge" });
  const page = await browser.newPage();
  await page.goto(process.argv[2]);
  await page.waitForLoadState("networkidle");
  console.log(await page.content());
  await browser.close();
})();' > fetch-page.js

# 使用
node fetch-page.js https://example.com
```
