# AGENTS.md - 智能编码代理指南

本文件为 AI 编码代理提供项目规范、代码风格指南和开发命令。

---

## 交互要求

1. 处理所有问题时，**全程思考过程必须使用中文**（包括需求分析、逻辑拆解、方案选择、步骤推导等所有内部推理环节）。
2. 最终输出的所有回答内容（包括文字解释、代码注释、步骤说明等）**必须全部使用中文**，仅代码语法本身的英文关键词除外。
3. 操作系统为 Windows 10，生成的 shell 命令必须是 powershell 命令，不能是 sh 或 bash 命令。
4. 回复简洁直接
5. 不确定的主动询问，不要编造答案
 
---

## 项目概述

全栈 Web 应用，采用前后端分离架构：

| 模块 | 技术栈 | 版本 |
|------|--------|------|
| **app-vue** | Vue 3.5 + Vite 8 | 0.0.0 |
| **springboot** | Spring Boot 4.0.5 + Java 21 | 0.0.1-SNAPSHOT |

---

## 目录结构

```
vibe coding/
├── app-vue/                      # Vue 3 前端应用
│   ├── src/
│   │   ├── assets/              # 静态资源 (CSS, SVG)
│   │   ├── components/          # Vue 组件
│   │   │   └── icons/          # 图标组件
│   │   ├── App.vue             # 根组件
│   │   └── main.js             # 入口文件
│   ├── public/                  # 公共静态文件
│   ├── index.html              # HTML 模板
│   ├── vite.config.js          # Vite 配置
│   └── package.json            # Node 依赖
│
└── springboot/                  # Spring Boot 后端
    ├── src/
    │   ├── main/java/          # 源代码 (cn.coderstory.springboot.*)
    │   └── test/java/          # 测试代码
    ├── pom.xml                 # Maven 配置
    └── mvnw                    # Maven Wrapper
```

---

## 构建与开发命令

### 前端 (app-vue)

```bash
cd app-vue

# 安装依赖
npm install

# 开发模式 (热重载)
npm run dev

# 生产构建
npm run build

# 预览生产构建
npm run preview
```

### 后端 (springboot)

```bash
cd springboot

# 运行应用
mvnw.cmd spring-boot:run        # Windows
./mvnw spring-boot:run          # Linux/macOS

# 编译打包
mvn package

# 运行所有测试
mvn test

# 运行单个测试类
mvn test -Dtest=SpringbootApplicationTests

# 运行单个测试方法
mvn test -Dtest=SpringbootApplicationTests#contextLoads

# 跳过测试
mvn package -DskipTests
```

---

## 前端代码风格 (Vue 3)

### 组件结构

使用 `<script setup>` 组合式 API：

```vue
<script setup>
import { ref, computed } from 'vue'
import ComponentA from '@/components/ComponentA.vue'
import { formatDate } from '@/utils/format'

defineProps({
  propA: { type: String, required: true },
  propB: { type: Number, default: 0 },
})

const emit = defineEmits(['update', 'delete'])

const localVar = ref('value')

const computedValue = computed(() => localVar.value * 2)

function handleUpdate() {
  emit('update', computedValue.value)
}
</script>

<template>
  <div class="container">
    <ComponentA />
    <button @click="handleUpdate">更新</button>
  </div>
</template>

<style scoped>
.container {
  padding: 1rem;
  display: flex;
  gap: 1rem;
}
</style>
```

### 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 组件文件 | PascalCase | `UserProfile.vue` |
| Props | camelCase (defineProps) | `propName` |
| Props (模板) | kebab-case | `prop-name` |
| Composables | camelCase + use 前缀 | `useAuth.js` |
| CSS 类 | kebab-case | `.user-profile` |
| 样式作用域 | 始终添加 `scoped` | `<style scoped>` |

### 导入顺序

1. Vue 核心 (`vue`, `vue-router`)
2. 第三方库 (`axios`, `lodash`)
3. 内部组件 (`@/components/*`)
4. 内部工具 (`@/utils/*`)
5. 相对导入 (`./`, `../`)

### 最佳实践

- 使用 Composition API，避免 Options API
- 使用 `ref()` 和 `reactive()` 管理响应式状态
- 优先使用 `@` 路径别名而非相对路径
- 外部链接添加 `rel="noopener"`
- 无 TypeScript，使用 JSDoc 注释类型

---

## 后端代码风格 (Java Spring Boot)

### 类结构

```java
package cn.coderstory.springboot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findById(Long id) {
        log.info("查找用户: {}", id);
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
}
```

### 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 类名 | PascalCase | `UserService` |
| 方法/变量 | camelCase | `getUserById` |
| 常量 | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| 包名 | 全小写 | `cn.coderstory.springboot` |
| Controller | `*Controller` | `UserController` |
| Service | `*Service` | `UserService` |
| Repository | `*Repository` | `UserRepository` |

### Spring Boot 约定

- 使用 Lombok 注解 (`@Data`, `@Service`, `@Repository`)
- 构造器注入优先于字段注入
- REST 控制器使用 `@RestController`
- 使用 `@GetMapping`, `@PostMapping` 等
- 测试放在 `src/test/java/` 对应包下

### 错误处理

```java
@RestController
@RequiredArgsConstructor
public class UserController {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleError(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
```

---

## API 设计约定

### RESTful 端点

```
GET    /api/users        # 获取用户列表
GET    /api/users/{id}   # 获取单个用户
POST   /api/users        # 创建用户
PUT    /api/users/{id}   # 更新用户
DELETE /api/users/{id}   # 删除用户
```

### 响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

---

## 测试规范

### 后端测试 (JUnit 5)

```java
@SpringBootTest
class UserServiceTest {

    @Test
    void shouldFindUserById() {
        // given
        Long userId = 1L;
        
        // when
        User user = userService.findById(userId);
        
        // then
        assertNotNull(user);
        assertEquals(userId, user.getId());
    }
}
```

- 使用 `@SpringBootTest` 进行集成测试
- 使用 `@Test` 标注测试方法
- 使用 Mockito 模拟依赖
- 遵循 Given-When-Then 模式

### 前端测试 (未配置)

- 推荐 Vitest 进行单元测试
- 推荐 Playwright 进行 E2E 测试

---

## Git 工作流

### 提交规范

```
feat: 新功能
fix: 修复 bug
docs: 文档更新
style: 代码格式 (不影响功能)
refactor: 重构
test: 测试相关
chore: 构建/工具变更
```

### 提交示例

```bash
git add .
git commit -m "feat: 添加用户登录功能"
git push
```

### 注意事项

- 避免提交敏感信息 (.env, credentials)
- 提交前运行测试确保通过
- 不强制推送 main/master 分支

---

## 技术要求

- **前端**: Node.js v20.19+ 或 v22.12+
- **后端**: Java 21+
- **前端路径别名**: `@` 映射到 `src/`

 

---

## 数据库迁移 (Flyway)

### 迁移脚本位置
```
springboot/src/main/resources/db/migration/
```

### 脚本命名规范
- 格式：`V{版本号}__{描述}.sql`
- 版本号使用整数，从 1 开始递增
- 描述使用下划线分隔单词
- 示例：`V1__init.sql`、`V2__add_user_phone.sql`、`V3__create_role_table.sql`

### 迁移规则
1. **每次数据库变更必须创建新迁移脚本**，禁止直接修改已执行的脚本
2. 迁移脚本必须具有幂等性（重复执行不会报错）
3. 使用 `CREATE TABLE IF NOT EXISTS`、`ALTER TABLE ADD COLUMN IF NOT EXISTS` 等安全语句
4. 提交前确保迁移脚本在本地测试通过

### 示例迁移脚本

```sql
-- V2__add_user_phone.sql
ALTER TABLE sys_user ADD COLUMN phone VARCHAR(20) COMMENT '手机号' AFTER name;
```

### 常见问题处理
- **已有表需要 Flyway 管理**：设置 `spring.flyway.baseline-on-migrate: true`
- **迁移失败**：检查 SQL 语法，确保语句完整
- **版本冲突**：确保版本号唯一，不重复

---

## 编程规范
1. 后端的SQL写道mapper.xml中，而不是接口类上
2. 后端数据库操作尽可能复用mybatis plus的特性，比如使用basesevice简化增删改查
 

 
## 维护记录

- **2024**: 初始创建
- **改进**: 优化代码风格指南，添加更详细的测试命令和 API 设计约定
- **2026-04-02**: 集成 Flyway 数据库迁移工具，添加数据库迁移规范
