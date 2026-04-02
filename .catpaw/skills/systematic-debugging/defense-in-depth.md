# 深度防御验证

## 概述

当你修复由无效数据引起的错误时，在一个地方添加验证似乎足够了。但单一检查可以被不同的代码路径、重构或模拟绕过。

**核心原则：** 在数据经过的每一层验证。使错误在结构上不可能发生。

## 为什么需要多层

单一验证："我们修复了错误"
多层防御："我们让错误不可能发生"

不同层捕获不同情况：
- 入口验证捕获大多数错误
- 业务逻辑捕获边缘情况
- 环境守卫防止特定上下文的危险
- 调试日志在其他层失败时帮助

## 四个层

### 层 1：入口点验证
**目的：** 在 API 边界拒绝明显无效的输入

```typescript
function createProject(name: string, workingDirectory: string) {
  if (!workingDirectory || workingDirectory.trim() === '') {
    throw new Error('workingDirectory 不能为空');
  }
  if (!existsSync(workingDirectory)) {
    throw new Error(`workingDirectory 不存在：${workingDirectory}`);
  }
  if (!statSync(workingDirectory).isDirectory()) {
    throw new Error(`workingDirectory 不是目录：${workingDirectory}`);
  }
  // ... 继续
}
```

### 层 2：业务逻辑验证
**目的：** 验证业务规则和边缘情况

```typescript
function initRepo(dir: string) {
  // 即使调用者验证了，也检查前置条件
  if (!existsSync(dir)) {
    throw new Error(`无法初始化仓库：目录不存在：${dir}`);
  }

  const gitDir = join(dir, '.git');
  if (existsSync(gitDir)) {
    throw new Error(`仓库已初始化：${gitDir}`);
  }

  // ... 执行
}
```

### 层 3：环境守卫
**目的：** 在危险环境中完全阻止操作

```typescript
function initRepo(dir: string) {
  // 绝对不要在系统目录中初始化
  const dangerousPaths = ['/usr', '/bin', '/etc', '/System'];
  if (dangerousPaths.some(p => dir.startsWith(p))) {
    throw new Error(`安全：拒绝在系统目录中初始化：${dir}`);
  }

  // 在生产环境中阻止测试目录
  if (process.env.NODE_ENV === 'production' && dir.includes('/test/')) {
    throw new Error(`生产：拒绝在测试目录中初始化：${dir}`);
  }

  // ... 执行
}
```

### 层 4：调试日志
**目的：** 当其他层失败时提供可见性

```typescript
function initRepo(dir: string) {
  logger.debug('初始化仓库', {
    directory: dir,
    cwd: process.cwd(),
    exists: existsSync(dir),
    timestamp: new Date().toISOString()
  });

  // ... 执行
}
```

## 完整示例

**错误：** git init 在错误目录中执行

```typescript
// 层 1：入口点
function handleCreateProject(args: string[]) {
  const workingDirectory = args[0] || findProjectRoot();

  // 入口验证
  if (!workingDirectory) {
    throw new Error('必须指定工作目录');
  }
  if (!existsSync(workingDirectory)) {
    throw new Error(`目录不存在：${workingDirectory}`);
  }

  createProject(name, workingDirectory);
}

// 层 2：业务逻辑
function createProject(name: string, workingDirectory: string) {
  // 业务规则验证
  if (workingDirectory.includes('node_modules')) {
    throw new Error('不能在 node_modules 中创建项目');
  }

  initRepo(workingDirectory);
}

// 层 3：环境守卫
function initRepo(dir: string) {
  // 安全检查
  const systemDirs = ['/usr', '/bin', '/etc'];
  if (systemDirs.some(d => dir.startsWith(d))) {
    throw new Error(`安全：拒绝在系统目录中操作`);
  }

  // ... git init
}

// 层 4：调试日志
function initRepo(dir: string) {
  logger.debug('git init', { dir, caller: new Error().stack });
  // ... git init
}
```

## 红旗

**从不：**
- 只在一个地方验证
- 假设"调用者会验证"
- 跳过层因为"这应该足够了"
- 在验证中模拟依赖

**始终：**
- 在每一层验证
- 添加调试日志
- 考虑所有代码路径
- 测试绕过单个层

## 相关

- `root-cause-tracing.md` - 找到错误的起源
- `condition-based-waiting.md` - 稳定测试
