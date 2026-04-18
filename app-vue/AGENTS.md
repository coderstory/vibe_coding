# app-vue AGENTS.md

本目录为 Vue 3 前端专用开发指南。请务必同时阅读根目录的 `../AGENTS.md` 了解全局规则。

---

## 模块概述

| 项目 | 版本 |
|------|------|
| Vue | 3.5+ |
| Vite | 8.x |
| Node | ^20.19.0 或 >=22.12.0 |

---

## 开发命令

```powershell
# 安装依赖
npm install

# 开发模式（热重载）
npm run dev

# 生产构建
npm run build

# 预览生产构建
npm run preview

# 运行测试
npm run test

# 测试 UI
npm run test:ui

# 代码覆盖率
npm run coverage

# Lint 检查
npm run lint

# 自动修复 Lint 问题
npm run lint:fix

# 格式化代码（Prettier）
npm run format
```

---

## 技术栈

| 技术 | 用途 | 备注 |
|------|------|------|
| Vue 3.5 | 核心框架 | 组合式 API (Script Setup) |
| Pinia | 状态管理 | 替代 Vuex |
| Vue Router | 路由 | history 模式 |
| Element Plus | UI 组件库 | 图标使用 @element-plus/icons-vue |
| Axios | HTTP 客户端 | 需配合拦截器使用 |
| Vitest | 单元测试 | jsdom 环境 |

---

## 代码规范

### 组件结构（必须使用 Script Setup）

```vue
<script setup>
import { ref, computed } from 'vue'
import ComponentA from '@/components/ComponentA.vue'

defineProps({
  propA: { type: String, required: true },
  propB: { type: Number, default: 0 },
})

const emit = defineEmits(['update', 'delete'])

const localVar = ref('value')

const computedValue = computed(() => localVar.value * 2)
</script>

<template>
  <div class="container">
    <ComponentA />
    <button @click="emit('update', computedValue)">更新</button>
  </div>
</template>

<style scoped>
.container {
  padding: 1rem;
}
</style>
```

### 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 组件文件 | PascalCase | `UserProfile.vue` |
| Props (defineProps) | camelCase | `propName` |
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

---

## 目录结构

```
app-vue/src/
├── api/              # Axios API 封装
│   ├── auth.js       # 认证相关 API
│   ├── menu.js       # 菜单 API
│   ├── user.js       # 用户 API（注意：错误拦截已改进）
│   └── __tests__/    # API 单元测试
├── assets/           # 静态资源
├── components/       # Vue 组件
│   └── icons/        # 图标组件
├── router/           # Vue Router 配置
│   └── index.js      # 路由定义、守卫
├── store/            # Pinia 状态管理
│   └── user.js       # 用户状态（token、登录状态）
├── utils/            # 工具函数
├── views/            # 页面组件
│   ├── auth/         # 认证相关页面
│   │   └── Login.vue
│   ├── layout/       # 布局组件
│   │   └── Layout.vue
│   └── dashboard/     # 仪表盘
├── App.vue           # 根组件
└── main.js          # 入口文件
```

---

## 已有模式（来自代码探索）

### Axios 拦截器模式

```javascript
// src/api/user.js 示例结构
import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
})

// 请求拦截器 - 自动注入 token
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器 - 提取友好错误消息
request.interceptors.response.use(
  response => response.data,
  error => {
    const message = error.response?.data?.message || '请求失败'
    return Promise.reject(new Error(message))
  }
)

export default request
```

### Pinia Store 模式

```javascript
// src/store/user.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(null)

  const isLoggedIn = computed(() => !!token.value)

  function login(token, user) {
    this.token = token
    this.user = user
    localStorage.setItem('token', token)
    localStorage.setItem('user', JSON.stringify(user))
  }

  function logout() {
    this.token = ''
    this.user = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return { token, user, isLoggedIn, login, logout }
})
```

### 路由守卫模式

```javascript
// src/router/index.js
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
  } else {
    next()
  }
})
```

---

## 已识别的问题（反模式）

### ⚠️ 需修复项

1. **硬编码凭证** - 不要在任何文件中硬编码密码或密钥
2. **内联 SQL** - 前端不涉及，但后端 Mapper 接口中不应写 SQL（见 `../springboot/AGENTS.md`）

---

## 相关文件

- `../AGENTS.md` - 全局项目规范
- `../.planning/` - GSD 工作流规划目录
- `./package.json` - 依赖和脚本定义
- `./vite.config.js` - Vite 配置（含代理设置）
- `./eslint.config.js` - ESLint 规则
- `./.prettierrc.json` - Prettier 格式化配置

---

*最后更新：2026-04-18*
