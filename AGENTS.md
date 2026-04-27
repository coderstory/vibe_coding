# AGENTS.md - 智能编码代理指南

本文件为 AI 编码代理提供项目规范、代码风格指南和开发命令。

---

## 交互要求

1. 处理所有问题时，**全程思考过程必须使用中文**（包括需求分析、逻辑拆解、方案选择、步骤推导等所有内部推理环节）。
2. 最终输出的所有回答内容（包括文字解释、代码注释、步骤说明等）**必须全部使用中文**，仅代码语法本身的英文关键词除外。
3. 操作系统为 Windows 10，生成的 shell 命令必须是 powershell 7命令，不能是 sh 或 bash 命令。
   - powershell主程序路径：`C:\Program Files\PowerShell\7\pwsh.exe`
   - **注意**：OpenCode 的 bash 工具默认调用 PowerShell 5.1，必须使用 `pwsh -Command "your command"` 格式调用 PowerShell 7
   - 示例：`pwsh -Command "Get-Date"` 而不是 `Get-Date`
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
│   │   │   ├── auth.ts             # 认证 API
│   │   │   ├── user.ts             # 用户 API
│   │   │   ├── role.ts             # 角色 API
│   │   │   ├── menu.ts             # 菜单 API
│   │   │   ├── audit.ts            # 审计日志 API
│   │   │   ├── knowledge.ts        # 知识库 API
│   │   │   └── __tests__/          # API 测试
│   │   ├── components/             # 公共组件
│   │   │   ├── AppHeader.vue       # 顶部导航栏
│   │   │   ├── AppMenu.vue         # 侧边菜单（后端动态加载）
│   │   │   ├── AppTabs.vue         # 标签页导航
│   │   │   └── knowledge/          # 知识库组件
│   │   │       ├── ArticleEditor.vue  # 文章编辑器（富文本 + 标签管理）
│   │   │       └── CategoryTree.vue  # 分类树组件
│   │   ├── composables/            # 组合式函数
│   │   │   └── useAnimationToggle.ts
│   │   ├── router/index.ts         # 路由配置 + 导航守卫
│   │   ├── store/user.ts           # Pinia 用户状态
│   │   ├── views/                  # 页面视图
│   │   │   ├── auth/Login.vue      # 登录页
│   │   │   ├── layout/Layout.vue   # 主布局
│   │   │   ├── dashboard/          # 首页
│   │   │   ├── system/             # 系统管理
│   │   │   │   ├── UserManagement.vue
│   │   │   │   ├── UserDetail.vue
│   │   │   │   ├── RoleManage.vue
│   │   │   │   └── MenuManage.vue
│   │   │   ├── audit/AuditLog.vue  # 审计日志
│   │   │   ├── business/           # 业务数据
│   │   │   │   └── BusinessData.vue  # 知识库管理
│   │   │   └── error/NotFound.vue # 404
│   │   ├── assets/                 # 静态资源 + 主题 CSS
│   │   │   └── themes/            # 主题文件
│   │   ├── App.vue
│   │   └── main.ts
│   ├── vitest.config.js            # Vitest 测试配置
│   ├── eslint.config.js
│   ├── jsconfig.json
│   └── vite.config.js              # 含 /api 代理到 localhost:8080
│
└── springboot/                      # Spring Boot 后端
    ├── src/main/java/cn/coderstory/springboot/
    │   ├── config/                 # 配置类
    │   │   ├── CorsConfig.java    # CORS 跨域配置
    │   │   ├── SecurityConfig.java # 安全配置
    │   │   └── WebConfig.java     # Web 配置
    │   ├── security/               # JWT 认证
    │   │   ├── JwtAuthenticationFilter.java
    │   │   ├── JwtTokenProvider.java
    │   │   └── PasswordEncoder.java
    │   ├── controller/             # REST 控制器
    │   │   ├── AuthController.java
    │   │   ├── UserController.java
    │   │   ├── RoleController.java
    │   │   ├── MenuController.java
    │   │   ├── AuditLogController.java
    │   │   └── KnowledgeController.java
    │   ├── service/                # 服务接口 + 实现类
    │   │   ├── impl/               # 实现类子包
    │   │   ├── KnowledgeService.java
    │   │   ├── KnowledgeServiceImpl.java
    │   │   └── ...
    │   ├── mapper/                 # MyBatis Plus Mapper 接口
    │   │   └── ...
    │   ├── entity/                 # 数据实体
    │   │   ├── User.java
    │   │   ├── Role.java
    │   │   ├── Menu.java
    │   │   ├── AuditLog.java
    │   │   ├── KnowledgeArticle.java
    │   │   ├── KnowledgeCategory.java
    │   │   ├── KnowledgeTag.java
    │   │   ├── KnowledgeFile.java
    │   │   └── KnowledgeArticleTag.java
    │   ├── vo/                     # 视图对象
    │   │   ├── ApiResponse.java
    │   │   ├── ResultResponse.java
    │   │   └── UserVO.java
    │   ├── aspect/                 # AOP 切面
    │   │   └── AuditAspect.java    # 审计日志切面
    │   ├── exception/              # 异常处理
    │   │   ├── BusinessException.java
    │   │   └── GlobalExceptionHandler.java
    │   └── util/                   # 工具类
    │       └── ZstdUtil.java       # Zstd 压缩工具
    ├── src/main/resources/
    │   ├── application.yaml        # 配置文件
    │   ├── mapper/*.xml            # MyBatis XML 映射
    │   └── db/migration/           # Flyway 迁移脚本
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
npm run test

# 运行单个测试文件
npx vitest src/api/__tests__/user.test.ts

# Lint 检查
npm run lint

# Lint 自动修复
npm run lint:fix

# 格式化和 prettier
npm run format
```

### 后端 (springboot)

```powershell
cd springboot

# 运行应用（需要本地 MySQL, 数据库 admin_system）
.\mvnw.cmd spring-boot:run

# 编译打包
.\mvnw.cmd package

# 运行所有测试
.\mvnw.cmd test

# 运行单个测试类
.\mvnw.cmd test -Dtest=SpringbootApplicationTests

# 运行单个测试方法
.\mvnw.cmd test -Dtest=SpringbootApplicationTests#contextLoads

# 跳过测试打包
.\mvnw.cmd package -DskipTests
```

---

## 前端代码风格 (Vue 3)

### 组件结构

使用 `<script setup lang="ts">` 组合式 API：

```vue
<script setup lang="ts">
import { ref, computed } from 'vue'
import ComponentA from '@/components/ComponentA.vue'

defineProps<{
  propA: string
  propB?: number
}>()

const emit = defineEmits<{
  update: [value: number]
}>()

const localVar = ref('value')

const computedValue = computed(() => localVar.value + props.propB)

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
2. 第三方库 (`axios`, `element-plus`)
3. 内部组件 (`@/components/*`)
4. 内部工具 (`@/utils/*`)
5. 相对导入 (`./`, `../`)

### 最佳实践

- 使用 Composition API，避免 Options API
- 使用 `ref()` 和 `reactive()` 管理响应式状态
- 优先使用 `@` 路径别名而非相对路径
- 外部链接添加 `rel="noopener"`
- 使用 TypeScript，类型定义集中在 `api/types.ts`
- API 调用统一通过 `api/request.ts` 的 Axios 实例
- 弹窗组件添加 `lock-scroll` 和 `append-to-body` 属性防止背景滚动

---

## 后端代码风格 (Java Spring Boot)

### 类结构

Service 层采用接口+实现模式，实现类放在 `impl/` 子包：

```java
// 接口: service/KnowledgeService.java
public interface KnowledgeService {
    KnowledgeArticle createArticle(KnowledgeArticle article, List<Long> tagIds);
    KnowledgeArticle updateArticle(Long id, KnowledgeArticle article, List<Long> tagIds);
}

// 实现: service/impl/KnowledgeServiceImpl.java
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {
    private final KnowledgeArticleMapper articleMapper;
    private final KnowledgeTagMapper tagMapper;
    private final KnowledgeArticleTagMapper articleTagMapper;
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
- 分页使用 MyBatis Plus 的 `Page<T>` + `IPage<T>`
- 异常使用 `BusinessException`（含 `badRequest`/`unauthorized`/`notFound`/`conflict` 静态方法）
- API 响应统一使用 `ApiResponse<T>` 封装，格式 `{code, message, data}`
- Mapper 的复杂 SQL 写在 `resources/mapper/*.xml` 中

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
4. Token 过期（401）时前端自动调用 `POST /api/auth/refresh` 刷新
5. 安全白名单配置在 `application.yaml` 的 `security.whitelist`

### 状态管理
- 仅一个 Pinia Store：`useUserStore`（Composition API 风格 `defineStore`）
- 持久化方式：手动读写 `localStorage` 的 `token`、`refreshToken`、`user` 三个键

---

## 审计日志系统

后端通过 AOP 切面 `AuditAspect` 自动记录写操作审计日志：
- 切点：`cn.coderstory.springboot.service..*`（排除 `AuditService` 自身）
- 根据方法名前缀推断操作类型：`save/create/add/insert` → 新增，`update/edit` → 编辑，`delete/remove` → 删除
- 使用 `ThreadLocal` 防止同一线程重复记录
- 审计日志通过 `AuditService` 异步写入（`@Async`）
- 读操作不记录审计日志

---


## API 设计约定

### RESTful 端点

```
GET    /api/users           # 获取用户列表
GET    /api/users/{id}      # 获取单个用户
POST   /api/users           # 创建用户
PUT    /api/users/{id}      # 更新用户
DELETE /api/users/{id}      # 删除用户

GET    /api/knowledge/articles      # 获取文章分页列表
GET    /api/knowledge/articles/{id} # 获取文章详情
POST   /api/knowledge/articles     # 创建文章
PUT    /api/knowledge/articles/{id} # 更新文章
DELETE /api/knowledge/articles/{id} # 删除文章

GET    /api/knowledge/categories/tree # 获取分类树
POST   /api/knowledge/categories     # 创建分类
PUT    /api/knowledge/categories/{id} # 更新分类
DELETE /api/knowledge/categories/{id} # 删除分类

GET    /api/knowledge/tags      # 获取所有标签
POST   /api/knowledge/tags      # 创建标签
DELETE /api/knowledge/tags/{id} # 删除标签
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
- 配置文件：`vitest.config.js`
- 测试文件位置：`src/**/__tests__/*.test.ts`
- 已有测试：`src/api/__tests__/user.test.ts`
- 运行命令：`npm run test`

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

```powershell
git add .
git commit -m "feat: 添加用户登录功能"
git push
```

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
- 示例：`V1__init.sql`、`V2__add_user_phone.sql`

### 迁移规则
1. **每次数据库变更必须创建新迁移脚本**，禁止直接修改已执行的脚本
2. 迁移脚本必须具有幂等性
3. 使用 `CREATE TABLE IF NOT EXISTS`、`ALTER TABLE ADD COLUMN IF NOT EXISTS` 等安全语句
4. 提交前确保迁移脚本在本地测试通过

### 示例迁移脚本

```sql
-- V2__add_user_phone.sql
ALTER TABLE sys_user ADD COLUMN phone VARCHAR(20) COMMENT '手机号' AFTER name;
```

### 常见问题处理
- **已有表需要 Flyway 管理**：设置 `spring.flyway.baseline-on-migrate: true`
- **迁移失败**：检查 SQL 语法
- **版本冲突**：确保版本号唯一

---

## 编程规范

1. 后端的 SQL 写在 mapper.xml 中，而不是接口类注解上
2. 后端数据库操作尽可能复用 MyBatis Plus 的特性
3. 新增数据库表或字段必须创建 Flyway 迁移脚本
4. 前端 API 类型定义集中在 `api/types.ts`
5. 前端组件使用 `<script setup lang="ts">`
6. 标签ID类型使用 Long，避免 Integer 可能的数据丢失
7. API 参数传递时注意空值检查，避免 NPE
8. 绝对不能使用@SuppressWarnings等方案规避问题
9. 代码需要编写必要的注释
10. 复杂业务核心使用TDD模式开发
11. 使用构造函数注入依赖

---

## 维护记录

- **2024**: 初始创建
- **2026-04-02**: 集成 Flyway 数据库迁移工具
- **2026-04-20**: 更新目录结构、补充认证流程/审计系统架构、修正代码风格与实际一致、更新前端测试配置、知识库模块完善
