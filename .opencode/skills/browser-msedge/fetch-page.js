/**
 * 浏览器自动化脚本 - 使用 msedge 获取动态网页内容
 * 用法: node fetch-page.js <URL> [output-file]
 */

const { chromium } = require('playwright');

const args = process.argv.slice(2);
const url = args[0];
const outputFile = args[1];

if (!url) {
  console.error('用法: node fetch-page.js <URL> [output-file]');
  process.exit(1);
}

(async () => {
  const browser = await chromium.launch({ channel: 'msedge' });
  const page = await browser.newPage();

  try {
    // 设置视口
    await page.setViewportSize({ width: 1920, height: 1080 });

    // 访问页面
    await page.goto(url, { waitUntil: 'networkidle', timeout: 30000 });

    // 获取完整内容
    const content = await page.content();
    const title = await page.title();

    // 构建输出
    const output = `=== TITLE: ${title} ===

=== CONTENT ===
${content}
`;

    if (outputFile) {
      const fs = require('fs');
      fs.writeFileSync(outputFile, output, 'utf8');
      console.log(`内容已保存到: ${outputFile}`);
    } else {
      console.log(output);
    }

  } catch (error) {
    console.error('获取页面失败:', error.message);
    process.exit(1);
  } finally {
    await browser.close();
  }
})();
