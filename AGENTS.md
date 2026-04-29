# AGENTS.md - 智能编码代理指南

本文件为 AI 编码代理提供项目规范和代码风格指南。

## 交互要求

1. **全程思考使用中文**
2. 当前操作系统是windows，调用shell时，**禁止生成bash命令**，而是使用**PowerShell 7 命令**：`pwsh -Command "your command"`
3. 回复简洁直接，不确定的主动询问
4. 当前只能使用MiniMax-M2.7模型，禁止调用gpt系列模型

---

## 项目概述

| 模块 | 技术栈 | 版本 |
|------|--------|------|
| **app-vue** | Vue 3.5 + Vite 8 + TypeScript | 0.0.0 |
| **springboot** | Spring Boot 4.0.5 + Java 21 | 0.0.1-SNAPSHOT |

**核心功能**：系统管理、知识库、秒杀系统、审计日志

---

## 目录结构

```
vibe_coding/
├── app-vue/                    # Vue 3 前端
│   ├── src/
│   │   ├── api/               # API 层（request.ts, types.ts, auth.ts, user.ts...）
│   │   ├── components/        # 组件
│   │   ├── views/             # 页面（auth/, dashboard/, system/, audit/, business/, seckill/）
│   │   ├── router/            # 路由 + 导航守卫
│   │   └── store/             # Pinia 状态
│   └── vite.config.js         # 含 /api 代理到 localhost:8080
│
├── springboot/                 # Spring Boot 后端
│   ├── src/main/java/cn/coderstory/springboot/
│   │   ├── config/           # CorsConfig, SecurityConfig, WebConfig
│   │   ├── security/          # JWT 认证
│   │   ├── controller/        # REST 控制器
│   │   ├── service/impl/      # Service 实现
│   │   ├── mapper/            # MyBatis Plus Mapper
│   │   ├── entity/            # 数据实体
│   │   ├── vo/                # 视图对象
│   │   ├── aspect/            # AOP 切面（审计日志）
│   │   └── exception/         # 异常处理
│   └── src/main/resources/
│       ├── application.yaml
│       └── db/migration/      # Flyway 迁移脚本
│
├── docs/                       # 项目文档
│   ├── seckill/               # 秒杀系统文档
│   └── browser-automation-guide.md  # 浏览器自动化指南
│
├── .opencode/                  # AI 工具配置
│   └── skills/
│       └── browser-msedge/    # msedge 自动化脚本
│
├── AGENTS.md                   # 本文件
└── README.md
```

---

## 构建与开发命令

### 前端 (app-vue)

```powershell
cd app-vue
npm install
npm run dev        # 开发模式（端口 5173）
npm run build      # 生产构建
npm run test       # 单元测试
npm run lint       # Lint 检查
```

### 后端 (springboot)

```powershell
cd springboot
.\mvnw.cmd spring-boot:run    # 运行应用
.\mvnw.cmd package            # 打包
.\mvnw.cmd package -DskipTests  # 跳过测试打包
```

---

## 前端代码风格

### 组件结构

```vue
<script setup lang="ts">
import { ref, computed } from 'vue'
import ComponentA from '@/components/ComponentA.vue'

defineProps<{ propA: string; propB?: number }>()
const emit = defineEmits<{ update: [value: number] }>()

const localVar = ref('value')
const computedValue = computed(() => localVar.value)
</script>

<template>
  <div class="container">
    <ComponentA />
    <button @click="emit('update', computedValue)">更新</button>
  </div>
</template>

<style scoped>
.container { padding: 1rem; display: flex; gap: 1rem; }
</style>
```

### 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 组件文件 | PascalCase | `UserProfile.vue` |
| Props | camelCase | `propName` |
| CSS 类 | kebab-case | `.user-profile` |
| Composables | camelCase + use 前缀 | `useAuth.js` |

### 最佳实践

- 使用 Composition API（`<script setup lang="ts">`）
- 优先使用 `@` 路径别名
- API 类型定义集中在 `api/types.ts`
- 弹窗组件添加 `lock-scroll` 和 `append-to-body`

---

## 后端代码风格

### 类结构

```java
// 接口: service/KnowledgeService.java
public interface KnowledgeService {
    KnowledgeArticle createArticle(KnowledgeArticle article, List<Long> tagIds);
}

// 实现: service/impl/KnowledgeServiceImpl.java
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {
    private final KnowledgeArticleMapper articleMapper;
    // ...
}
```

### 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 类名 | PascalCase | `UserService` |
| 方法/变量 | camelCase | `getUserById` |
| 常量 | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| Controller | `*Controller` | `UserController` |
| Service | `*Service` / `*ServiceImpl` | `UserService` / `UserServiceImpl` |
| Mapper | `*Mapper` | `UserMapper` |

### Spring Boot 约定

- Lombok：`@Data`, `@Slf4j`, `@RequiredArgsConstructor`
- 构造器注入：`@RequiredArgsConstructor` + `private final`
- 实体：`@TableName` + `@TableId(type = IdType.AUTO)` + `@TableLogic`
- 异常：`BusinessException.notFound()` / `.badRequest()` / `.conflict()`
- 响应：`ApiResponse<T>` 封装 `{code, message, data}`
- 复杂 SQL：写在 `resources/mapper/*.xml`

---

## 认证流程

1. 前端登录 → `POST /api/auth/login` → 返回 `{token, refreshToken, user}`
2. Token 存 `localStorage`，请求携带 `Authorization: Bearer <token>`
3. 后端 `JwtAuthenticationFilter` 解析 token 设置 SecurityContext
4. Token 过期（401）→ 前端自动调用 `POST /api/auth/refresh` 刷新
5. 安全白名单配置在 `application.yaml` 的 `security.whitelist`

---

## 审计日志

- 切点：`cn.coderstory.springboot.service..*`（排除 AuditService 自身）
- 方法前缀推断：`save/create/add` → 新增，`update/edit` → 编辑，`delete/remove` → 删除
- `ThreadLocal` 防止同一线程重复记录
- `@Async` 异步写入

---

## 编程规范

1. 后端 SQL 写在 mapper.xml，不在注解上
2. 复用 MyBatis Plus 特性
3. 新增表/字段必须创建 Flyway 迁移脚本
4. 前端 API 类型集中在 `api/types.ts`
5. 标签 ID 类型用 Long（不用 Integer）
6. API 参数注意空值检查，避免 NPE
7. 不用 `@SuppressWarnings` 规避问题
8. 构造函数注入依赖

---

## 技术要求

- **前端**：Node.js v20.19+ / v22.12+
- **后端**：Java 21+
- **数据库**：MySQL（`admin_system`，默认 root/123456）
- **缓存**：Redis 8.0+（秒杀必需）
- **消息队列**：RocketMQ 5.3.2（秒杀必需）
- **端口**：前端 5173，后端 8080

---

## 维护记录

- **2024**: 初始创建
- **2026-04-02**: 集成 Flyway 数据库迁移
- **2026-04-20**: 更新目录结构、知识库模块、审计系统
- **2026-04-28**: 添加秒杀系统、浏览器自动化指南
