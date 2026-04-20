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
├── app-vue/                          # Vue 3 前端应用
│   ├── src/
│   │   ├── api/                    # API 服务层
│   │   │   ├── request.ts          # Axios 封装（拦截器、token 刷新）
│   │   │   ├── types.ts            # 全部 API 类型定义
│   │   │   ├── auth.ts / user.ts / role.ts / menu.ts / audit.ts / knowledge.ts
│   │   │   └── __tests__/          # API 测试
│   │   ├── components/             # 公共组件
│   │   │   ├── AppHeader.vue       # 顶部导航栏
│   │   │   ├── AppMenu.vue         # 侧边菜单（后端动态加载）
│   │   │   ├── AppTabs.vue         # 标签页导航
│   │   │   └── knowledge/          # 知识库组件
│   │   ├── composables/            # 组合式函数
│   │   ├── router/index.ts         # 路由配置 + 导航守卫
│   │   ├── store/user.ts           # Pinia 用户状态
│   │   ├── views/                  # 页面视图
│   │   │   ├── auth/               # 登录页
│   │   │   ├── layout/             # 主布局（侧边栏+头部+标签页+内容区）
│   │   │   ├── dashboard/          # 首页
│   │   │   ├── system/             # 系统管理（用户/角色/菜单）
│   │   │   ├── audit/              # 审计日志
│   │   │   ├── business/           # 业务数据
│   │   │   └── error/              # 404
│   │   ├── assets/                 # 静态资源 + 主题 CSS
│   │   ├── App.vue
│   │   └── main.ts
│   ├── vitest.config.js            # Vitest 测试配置
│   ├── eslint.config.js
│   └── vite.config.js              # 含 /api 代理到 localhost:8080
│
└── springboot/                      # Spring Boot 后端
    ├── src/main/java/cn/coderstory/springboot/
    │   ├── config/                 # 配置（Security、CORS、Web）
    │   ├── security/               # JWT 认证（Filter + TokenProvider + PasswordEncoder）
    │   ├── controller/             # REST 控制器
    │   ├── service/                # 服务接口 + 实现类（impl/ 子包）
    │   ├── mapper/                 # MyBatis Plus Mapper 接口
    │   ├── entity/                 # 数据实体（@TableName + @TableLogic 逻辑删除）
    │   ├── vo/                     # 视图对象（ApiResponse、ResultResponse、UserVO）
    │   ├── aspect/                 # AOP 切面（AuditAspect 审计日志）
    │   ├── exception/              # 全局异常处理（BusinessException + GlobalExceptionHandler）
    │   └── util/                   # 工具类（ZstdUtil 文件压缩）
    ├── src/main/resources/
    │   ├── application.yaml        # 单一配置文件
    │   ├── mapper/*.xml            # MyBatis XML 映射
    │   └── db/migration/           # Flyway 迁移脚本 (V1~V7)
    └── pom.xml
```

---

## 构建与开发命令

### 前端 (app-vue)

```powershell
cd app-vue

# 安装依赖
npm install

# 开发模式 (热重载, 端口 5173)
npm run dev

# 生产构建
npm run build

# 预览生产构建
npm run preview

# 运行前端单元测试 (Vitest + jsdom)
npx vitest

# 运行单个测试文件
npx vitest src/api/__tests__/user.test.ts

# Lint 检查
npx eslint src/
```

### 后端 (springboot)

```powershell
cd springboot

# 运行应用（需要本地 MySQL, 数据库 admin_system）
mvnw.cmd spring-boot:run

# 编译打包
mvnw.cmd package

# 运行所有测试
mvnw.cmd test

# 运行单个测试类
mvnw.cmd test -Dtest=SpringbootApplicationTests

# 运行单个测试方法
mvnw.cmd test -Dtest=SpringbootApplicationTests#contextLoads

# 跳过测试打包
mvnw.cmd package -DskipTests
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
- 使用 TypeScript (`<script setup lang="ts">`)，类型定义集中在 `api/types.ts`
- API 调用统一通过 `api/request.ts` 的 Axios 实例，不要直接使用 axios

---

## 后端代码风格 (Java Spring Boot)

### 类结构

Service 层采用接口+实现模式，实现类放在 `impl/` 子包：

```java
// 接口: service/UserService.java
public interface UserService {
    IPage<User> getUserPage(Page<User> page, String username, ...);
    UserVO getUserById(Long id);
    boolean saveUser(User user, String rawPassword);
}

// 实现: service/impl/UserServiceImpl.java
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    // ...
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
| Service 接口 | `*Service` | `UserService` |
| Service 实现 | `*ServiceImpl` | `UserServiceImpl` |
| Mapper | `*Mapper` | `UserMapper` |
| 实体 | `*` (与表名对应) | `User`, `KnowledgeArticle` |

### Spring Boot 约定

- 使用 Lombok 注解 (`@Data`, `@Slf4j`, `@RequiredArgsConstructor`)
- 构造器注入优先于字段注入（`@RequiredArgsConstructor` + `private final`）
- REST 控制器使用 `@RestController` + `@RequestMapping("/api/xxx")`
- 实体使用 `@TableName("表名")` + `@TableId(type = IdType.AUTO)` + `@TableLogic` 逻辑删除
- 分页使用 MyBatis Plus 的 `Page<T>` + `IPage<T>`，响应格式为 `{records, total, size, current, pages}`
- 异常使用 `BusinessException`（含 `badRequest`/`unauthorized`/`notFound`/`conflict` 静态方法），由 `GlobalExceptionHandler` 统一处理
- API 响应统一使用 `ApiResponse<T>` 封装，格式 `{code, message, data}`
- Mapper 的复杂 SQL 写在 `resources/mapper/*.xml` 中，不在接口注解上

### 错误处理

```java
// 抛出业务异常
throw BusinessException.notFound("用户不存在");
throw BusinessException.badRequest("密码不能为空");
throw BusinessException.conflict("用户名已存在");

// 全局异常处理器自动捕获，返回 ApiResponse 格式
```

---

## 前后端连接与认证

### 代理配置
前端开发服务器（5173）通过 Vite proxy 将 `/api` 请求代理到后端（8080）。

### 认证流程
1. 前端登录调用 `POST /api/auth/login`，返回 `{token, refreshToken, user}`
2. Token 存储在 `localStorage`，每次请求通过 `Authorization: Bearer <token>` 携带
3. 后端 `JwtAuthenticationFilter`（OncePerRequestFilter）解析 token 设置 SecurityContext
4. Token 过期（401）时前端自动调用 `POST /api/auth/refresh` 刷新，含并发请求队列机制
5. 安全白名单配置在 `application.yaml` 的 `security.whitelist`，目前为 `/api/auth/**` 和 `/api/knowledge/files/**`

### 状态管理
- 仅一个 Pinia Store：`useUserStore`（Composition API 风格 `defineStore`）
- 持久化方式：手动读写 `localStorage` 的 `token`、`refreshToken`、`user` 三个键

---

## 审计日志系统

后端通过 AOP 切面 `AuditAspect` 自动记录写操作审计日志：
- 切点：`cn.coderstory.springboot.service..*`（排除 `AuditService` 自身）
- 根据方法名前缀推断操作类型：`save/create/add/insert` → 新增，`update/edit` → 编辑，`delete/remove` → 删除等
- 使用 `ThreadLocal` 防止同一线程重复记录
- 审计日志通过 `AuditService` 异步写入（`@Async`）
- 读操作不记录审计日志

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
- 遵循 Given-When-Then 模式

### 前端测试 (Vitest)

- 测试框架：Vitest + jsdom 环境
- 配置文件：`vitest.config.js`，`@` 路径别名已配置
- 测试文件位置：`src/**/*.{test,spec}.{js,ts}`
- 已有测试：`src/api/__tests__/user.test.ts`（API 错误处理测试）
- 运行命令：`npx vitest`

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
- **数据库**: MySQL（数据库名 `admin_system`，本地默认 root/123456）
- **前端路径别名**: `@` 映射到 `src/`
- **后端端口**: 8080
- **前端开发端口**: 5173

 

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
1. 后端的 SQL 写在 mapper.xml 中，而不是接口类注解上
2. 后端数据库操作尽可能复用 MyBatis Plus 的特性，比如使用 `BaseMapper` / `IService` 简化增删改查
3. 新增数据库表或字段必须创建 Flyway 迁移脚本，禁止直接修改已执行的脚本
4. 前端 API 类型定义集中在 `api/types.ts`，新增类型同步更新
5. 前端组件使用 `<script setup lang="ts">`
 

 
## 维护记录

- **2024**: 初始创建
- **改进**: 优化代码风格指南，添加更详细的测试命令和 API 设计约定
- **2026-04-02**: 集成 Flyway 数据库迁移工具，添加数据库迁移规范
- **2026-04-20**: 更新目录结构、补充认证流程/审计系统架构、修正代码风格与实际一致、更新前端测试配置
