# 测试反模式

**何时加载此参考：** 编写或更改测试、添加模拟或想向生产代码添加仅测试方法时。

## 概述

测试必须验证真实行为，不是模拟行为。模拟是隔离的手段，不是被测试的东西。

**核心原则：** 测试代码做什么，不是模拟做什么。

**遵循严格的 TDD 可以防止这些反模式。**

## 铁律

```
1. 永远不要测试模拟行为
2. 永远不要向生产类添加仅测试方法
3. 永远不要在不了解依赖的情况下模拟
```

## 反模式 1：测试模拟行为

**违规：**
```typescript
// ❌ 不好：测试模拟是否存在
test('渲染侧边栏', () => {
  render(<Page />);
  expect(screen.getByTestId('sidebar-mock')).toBeInTheDocument();
});
```

**为什么这是错的：**
- 你在验证模拟工作，不是组件工作
- 当模拟存在时测试通过，不存在时失败
- 告诉你关于真实行为没有任何信息

**你的人工伙伴的纠正：** "我们在测试模拟的行为吗？"

**修复：**
```typescript
// ✅ 好：测试真实行为
test('渲染侧边栏', () => {
  render(<Page />);
  expect(screen.getByRole('navigation')).toBeInTheDocument();
  expect(screen.getByText('Dashboard')).toBeVisible();
});
```

## 反模式 2：仅测试方法

**违规：**
```typescript
// ❌ 不好：为测试添加公共方法
class UserService {
  private users: User[] = [];

  // 仅存在于测试中
  public getTestUsers(): User[] {
    return this.users;
  }
}

test('用户被添加', () => {
  service.addUser(user);
  expect(service.getTestUsers()).toContain(user);
});
```

**为什么这是错的：**
- 生产代码有测试污染
- 公共 API 包含用户不应该使用的方法
- 鼓励不良设计（为什么不通过公共 API 可用？）

**你的人工伙伴的纠正：** "这个方法在生产中会被使用吗？"

**修复：**
```typescript
// ✅ 好：通过公共行为验证
test('用户被添加', () => {
  service.addUser(user);
  expect(service.getUserById(user.id)).toEqual(user);
  expect(service.userCount()).toBe(1);
});
```

## 反模式 3：模拟不了解的依赖

**违规：**
```typescript
// ❌ 不好：模拟你不理解的复杂依赖
jest.mock('./database', () => ({
  query: jest.fn().mockReturnValue({ rows: [] })
}));

test('获取用户', () => {
  // 如果数据库实际返回什么？我不知道...
  const result = service.getUsers();
  expect(result).toEqual([]);
});
```

**为什么这是错的：**
- 模拟可能与真实行为不匹配
- 测试在模拟存在时通过，但真实代码失败
- 你不了解依赖做什么

**修复：**
```typescript
// ✅ 好：先学习依赖，然后正确模拟
// 步骤 1：研究真实数据库
const realDb = new Database();
const result = await realDb.query('SELECT * FROM users');
console.log('真实数据库返回：', result);

// 步骤 2：基于真实行为模拟
jest.mock('./database', () => ({
  query: jest.fn().mockResolvedValue({
    rows: [{ id: 1, name: 'Test User' }]
  })
}));
```

## 反模式 4：部分模拟

**违规：**
```typescript
// ❌ 不好：部分模拟类
const service = new UserService();
service.validate = jest.fn().mockReturnValue(true); // 只模拟这一个方法

test('创建用户', () => {
  const result = service.createUser({ name: '' });
  expect(result.success).toBe(true);
});
```

**为什么这是错的：**
- 跳过了实际的验证逻辑
- 测试证明模拟工作，不是代码工作
- 隐藏真实代码路径中的错误

**修复：**
```typescript
// ✅ 好：模拟依赖，不是类本身
const mockDb = { save: jest.fn() };
const service = new UserService(mockDb);

test('创建用户验证', () => {
  const result = service.createUser({ name: '' });
  expect(result.success).toBe(false);
  expect(result.error).toContain('名称');
});
```

## 反模式 5：过度模拟

**违规：**
```typescript
// ❌ 不好：模拟一切
jest.mock('./logger');
jest.mock('./database');
jest.mock('./cache');
jest.mock('./validator');
jest.mock('./notifier');

test('发送通知', () => {
  // 所有依赖都是假的
  // 这测试什么？
});
```

**为什么这是错的：**
- 测试与实现解耦
- 重构可以破坏代码而不破坏测试
- 无法保证真实集成工作

**修复：**
```typescript
// ✅ 好：只模拟外部边界
const mockEmail = { send: jest.fn() };
const service = new NotificationService(
  new Logger(),     // 真实
  new Database(),   // 真实（或测试数据库）
  mockEmail         // 模拟外部服务
);
```

## 检查清单

在提交测试之前：

- [ ] 测试验证真实行为，不是模拟
- [ ] 没有仅测试方法添加到生产代码
- [ ] 所有模拟匹配已知的真实行为
- [ ] 没有部分模拟（模拟类的一部分）
- [ ] 外部边界模拟，内部使用真实代码
- [ ] 测试在模拟删除时会失败（真实行为不同）

## 何时模拟是合适的

**模拟这些：**
- 外部 API（电子邮件、支付、云服务）
- 文件系统（使用内存版本）
- 时间（使用假时钟）
- 随机性（使用固定种子）

**不要模拟这些：**
- 你自己的业务逻辑
- 数据转换
- 验证逻辑
- 任何你想测试的东西

## 如果你发现这些反模式

**立即停止。**

如果你在测试模拟：
1. 删除模拟
2. 用真实依赖编写测试
3. 如果需要隔离，模拟边界，不是被测试的逻辑

如果你添加了仅测试方法：
1. 删除方法
2. 通过公共 API 编写测试
3. 如果公共 API 不够，考虑设计问题

## 最后的警告

```
通过的测试 ≠ 工作的代码
```

测试模拟的测试总是通过。
只有测试真实行为的测试才能证明代码工作。
