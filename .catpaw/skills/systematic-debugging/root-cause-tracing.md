# 根本原因追踪

## 概述

错误通常在调用堆栈深处显现（在错误目录中 git init、在错误位置创建文件、用错误路径打开数据库）。你的本能是修复错误出现的地方，但这只是治疗症状。

**核心原则：** 通过调用链向后追踪，直到找到原始触发点，然后在源头修复。

## 何时使用

**使用当：**
- 错误发生在执行深处（不是在入口点）
- 堆栈跟踪显示长调用链
- 不清楚无效数据从哪里产生
- 需要找到哪个测试/代码触发了问题

## 追踪过程

### 1. 观察症状
```
错误：git init 在 /Users/jesse/project/packages/core 中失败
```

### 2. 找到直接原因
**什么代码直接导致这个？**

```typescript
// 在 git.ts 中
function initRepo(dir: string) {
  execSync(`git init ${dir}`); // ← 在这里失败
}
```

问：谁调用了 `initRepo`，参数是什么？

### 3. 追踪调用者
**谁调用了这个函数？**

```typescript
// 在 project.ts 中
function createProject(name: string, workingDirectory: string) {
  // ... 很多代码
  initRepo(workingDirectory); // ← 传入了什么 workingDirectory？
}
```

问：谁调用了 `createProject`，workingDirectory 从哪里来？

### 4. 继续向后追踪
重复直到找到：

```typescript
// 在 cli.ts 中
function handleCommand(args: string[]) {
  const workingDirectory = args[0] || process.cwd();
  //                                ^^^^^^^^^^^^^
  //                                当 args[0] 未定义时，这给出了错误的值
  createProject(name, workingDirectory);
}
```

**发现触发点：** 默认值逻辑错误。

### 5. 在源头修复
不要修复 `git init` 调用——修复默认值：

```typescript
// ✅ 在源头修复
function handleCommand(args: string[]) {
  const workingDirectory = args[0] || findProjectRoot(); // 正确的默认值
  createProject(name, workingDirectory);
}
```

## 真实示例

**症状：**
```
错误：ENOENT: 没有此文件或目录，打开 '/tmp/.env'
```

**追踪：**
```
1. openFile('/tmp/.env') 在 filesystem.ts 中失败
   ↓ 谁调用了 openFile？
2. loadEnvironment('/tmp') 在 config.ts 中被调用
   ↓ 谁传入了 '/tmp'？
3. getTempDir() 在 environment.ts 中返回了 '/tmp'
   ↓ 为什么是临时目录？
4. detectEnvironment() 检测到 'test' 模式并使用了临时目录
   ↓ 但这不是测试！
5. process.env.NODE_ENV = 'test' 被之前的测试设置为残留
   ✓ 找到源头：测试污染
```

**修复：** 在测试后清理环境变量，不是更改文件读取逻辑。

## 防御层

修复后，添加防御：

```typescript
// 层 1：入口验证
function handleCommand(args: string[]) {
  const workingDirectory = args[0] || findProjectRoot();
  if (!existsSync(workingDirectory)) {
    throw new Error(`工作目录不存在：${workingDirectory}`);
  }
  // ...
}

// 层 2：业务逻辑验证
function createProject(name: string, workingDirectory: string) {
  if (!workingDirectory || workingDirectory.trim() === '') {
    throw new Error('workingDirectory 不能为空');
  }
  // ...
}
```

见 `defense-in-depth.md` 了解完整的防御策略。

## 红旗

**从不：**
- 在错误显现的地方修复
- 没有追踪就假设"一定是这个原因"
- 跳过追踪因为"我知道问题在哪"
- 停止追踪在"中间人"（只是传递数据的地方）

**始终：**
- 追踪到原始触发点
- 在源头修复
- 添加防御层
- 验证修复解决了根本原因

## 快速参考

| 问题 | 追踪方向 | 修复位置 |
|------|----------|----------|
| 参数错误值 | 调用者 → 调用者 → ... | 默认值/来源 |
| 配置错误 | 读取 → 加载 → 设置 | 设置点 |
| 状态错误 | 使用 → 设置 → 初始化 | 初始化 |
| 测试污染 | 使用 → 设置 → 测试 teardown | 测试清理 |
