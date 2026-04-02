---
description: Agent 工作约定 - Shell命令、Git提交、数据库配置等规范
alwaysApply: true
---

# Agent 工作约定


## 1. Git Commit 规范

### Commit Message 格式

- **简洁明了**：不要写太细，只需概括主要修改内容
- **按业务拆分**：不同业务修改应拆分为多个独立的 commit

```
# 正确示例
git commit -m "feat: 添加用户管理模块"
git commit -m "fix: 修复登录验证问题"
git commit -m "refactor: 重构工作流节点配置"

# 错误示例（过于详细）
git commit -m "feat: 在 UserController.java 第 45 行添加 getUserById 方法，修改了 UserService 接口..."
```

### Commit 拆分策略

```bash
# 按业务模块拆分提交
git add app/src/views/user/
git commit -m "feat: 用户管理页面开发"

git add service/src/main/java/com/robitcode/controller/UserController.java
git add service/src/main/java/com/robitcode/service/UserService.java
git commit -m "feat: 用户管理后端接口"

git add service/src/main/resources/mapper/UserMapper.xml
git commit -m "feat: 用户管理数据层"
```

## 2. 数据库配置

从 `service/src/main/resources/application-dev.yml` 读取数据库连接信息：

```yaml
# 数据库配置位置
spring:
  datasource:
    dynamic:
      datasource:
        master:
          url: jdbc:mysql://172.16.10.21:3306/robitcode_ywoa
          username: rc_dev_all
          password: 6TjKFG9M746S
```

**连接信息汇总：**
| 配置项 | 值 |
|--------|-----|
| 主机 | 172.16.10.21 |
| 端口 | 3306 |
| 数据库 | robitcode_ywoa |
| 用户名 | rc_dev_all |
| 密码 | 6TjKFG9M746S |

## 3. Git Add 规范

**只提交当前会话修改的文件**，不要提交外部修改：

```bash
# 正确做法：只添加本次会话修改的文件
git add app/src/views/workflow/WorkflowDesign.vue
git add service/src/main/java/com/robitcode/flowable/

# 错误做法：添加所有文件（可能包含外部修改）
git add .
git add -A
```

### 推荐的提交流程

1. 先用 `git status` 查看变更文件
2. 确认哪些是本次会话修改的文件
3. 使用 `git add <具体文件路径>` 添加指定文件
4. 按 commit 拆分策略分批提交
