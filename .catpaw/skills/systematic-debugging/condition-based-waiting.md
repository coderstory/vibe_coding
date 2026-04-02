# 基于条件的等待

## 概述

不稳定的测试经常用任意延迟猜测时间。这会创建竞争条件，测试在快速机器上通过，但在负载下或 CI 中失败。

**核心原则：** 等待你关心的实际条件，而不是猜测需要多长时间。

## 何时使用

**使用当：**
- 测试有任意延迟（`setTimeout`、`sleep`、`time.sleep()`）
- 测试不稳定（有时通过，负载下失败）
- 测试并行运行时超时
- 等待异步操作完成

**不要使用当：**
- 测试实际的时间行为（防抖、节流间隔）
- 如果使用任意超时，始终记录为什么需要

## 核心模式

```typescript
// ❌ 之前：猜测时间
await new Promise(r => setTimeout(r, 50));
const result = getResult();
expect(result).toBeDefined();

// ✅ 之后：等待条件
await waitFor(() => getResult() !== undefined);
const result = getResult();
expect(result).toBeDefined();
```

## 实现

### JavaScript/TypeScript

```typescript
// 通用等待函数
async function waitFor(
  condition: () => boolean | Promise<boolean>,
  options = { timeout: 5000, interval: 50 }
) {
  const start = Date.now();
  while (Date.now() - start < options.timeout) {
    if (await condition()) return;
    await new Promise(r => setTimeout(r, options.interval));
  }
  throw new Error(`条件未在 ${options.timeout}ms 内满足`);
}

// 使用示例
await waitFor(() => {
  const modal = document.querySelector('.modal');
  return modal?.classList.contains('visible');
});
```

### Python

```python
import asyncio
from datetime import datetime, timedelta

async def wait_for(condition, timeout=5.0, interval=0.05):
    """等待条件变为真"""
    start = datetime.now()
    while datetime.now() - start < timedelta(seconds=timeout):
        if await condition() if asyncio.iscoroutinefunction(condition) else condition():
            return
        await asyncio.sleep(interval)
    raise TimeoutError(f"条件未在 {timeout} 秒内满足")

# 使用示例
await wait_for(lambda: page.locator(".modal").is_visible())
```

## 常见模式

### 等待元素

```typescript
// ❌ 任意延迟
await page.waitForTimeout(1000);
await expect(page.locator('.result')).toBeVisible();

// ✅ 条件等待
await page.waitForSelector('.result', { state: 'visible' });
```

### 等待状态变化

```typescript
// ❌ 猜测时间
state.loading = true;
await new Promise(r => setTimeout(r, 100));
expect(state.loading).toBe(false);

// ✅ 等待状态
state.loading = true;
await waitFor(() => !state.loading);
```

### 等待 API 响应

```typescript
// ❌ 任意延迟
fetchData();
await new Promise(r => setTimeout(r, 500));
expect(data.value).toBeDefined();

// ✅ 等待响应
fetchData();
await waitFor(() => data.value !== undefined);
```

## 处理超时

当条件未在时间内满足时：

```typescript
try {
  await waitFor(() => element.isVisible(), { timeout: 5000 });
} catch (e) {
  // 提供调试信息
  console.log('元素状态:', element.html());
  console.log('页面状态:', page.content());
  throw e;
}
```

## 红旗

**从不：**
- 使用 `sleep(1000)` 代替等待条件
- 增加超时"只是为了让它通过"
- 在 CI 中跳过不稳定的测试
- 没有文档说明为什么要用超时

**始终：**
- 等待你关心的实际条件
- 为调试提供有意义的超时消息
- 调查为什么会不稳定
- 记录任何例外

## 相关

见 `root-cause-tracing.md` 了解跟踪深层调用堆栈中的问题。
