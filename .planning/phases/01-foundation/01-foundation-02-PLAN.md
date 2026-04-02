---
phase: 01-foundation
plan: 02
type: execute
wave: 2
depends_on: [01-foundation-01]
files_modified:
  - app-vue/package.json
  - app-vue/src/router/index.js
  - app-vue/src/api/auth.js
  - app-vue/src/store/user.js
  - app-vue/src/views/Login.vue
  - app-vue/src/views/Layout.vue
  - app-vue/src/components/AppMenu.vue
  - app-vue/src/components/AppTabs.vue
  - app-vue/src/components/AppHeader.vue
  - app-vue/src/main.js
autonomous: true
requirements:
  - UI-01
  - UI-02
  - MENU-01
  - MENU-03
  - MENU-04
  - MENU-05
user_setup: []
must_haves:
  truths:
    - 登录页面设计精美，符合企业级审美
    - 管理后台主界面布局合理（左侧菜单 + 顶部用户栏 + 内容区）
    - 系统左侧显示菜单导航，支持多级展开
    - 页面支持页签式多任务切换
    - 顶部显示当前登录用户信息和登出按钮
  artifacts:
    - path: "app-vue/src/views/Login.vue"
      provides: "登录页面，符合 UI-SPEC.md 设计规范"
    - path: "app-vue/src/views/Layout.vue"
      provides: "管理后台主布局，包含侧边栏、头部、内容区"
    - path: "app-vue/src/components/AppMenu.vue"
      provides: "左侧菜单组件，支持多级展开"
    - path: "app-vue/src/components/AppTabs.vue"
      provides: "页签切换组件"
    - path: "app-vue/src/components/AppHeader.vue"
      provides: "顶部用户栏组件，显示用户名和登出按钮"
    - path: "app-vue/src/router/index.js"
      provides: "路由配置，包含登录守卫"
  key_links:
    - from: "router/index.js"
      to: "store/user.js"
      via: "路由守卫检查用户登录状态"
    - from: "Login.vue"
      to: "api/auth.js"
      via: "调用登录 API"
    - from: "Layout.vue"
      to: "AppMenu.vue, AppTabs.vue, AppHeader.vue"
      via: "组合布局组件"
---

<objective>
实现前端认证 UI 和管理后台布局。包括登录页面、管理后台布局、菜单导航、页签切换、顶部用户栏。
</objective>

<execution_context>
@D:/Data/桌面/vibe coding/.opencode/get-shit-done/workflows/execute-plan.md
</execution_context>

<context>
@.planning/phases/01-foundation/01-foundation-CONTEXT.md
@.planning/phases/01-foundation/01-foundation-RESEARCH.md
@.planning/phases/01-foundation/01-foundation-UI-SPEC.md
@./AGENTS.md

<interfaces>
<!-- 前端接口定义 -->

登录 API (app-vue/src/api/auth.js):
```javascript
// 登录
export function login(username, password) {
  return axios.post('/api/auth/login', { username, password })
}

// 登出
export function logout() {
  return axios.post('/api/auth/logout')
}

// 获取当前用户
export function getCurrentUser() {
  return axios.get('/api/auth/current')
}
```

路由配置 (app-vue/src/router/index.js):
- 公开路由: /login
- 受保护路由: /dashboard, /system/*
- 路由守卫: 检查 localStorage token，无token跳转登录

用户 store (app-vue/src/store/user.js):
```javascript
{
  token: string | null,
  user: { id, username, name, roleId } | null,
  isLoggedIn: boolean
}
```

UI-SPEC.md 设计规范:
- 登录页: 卡片居中，400px宽度，圆角8px，输入框间距16px
- 管理后台: Header 60px，左侧菜单200px(收起64px)，内容区背景#F5F7FA
- 颜色: 主色#409EFF，背景#FFFFFF/#F5F7FA
- 字体: Body 14px, Heading 18px, Display 28px
</interfaces>
</context>

<tasks>

<task type="auto">
  <name>Task 1: 安装前端依赖</name>
  <files>app-vue/package.json</files>
  <read_first>app-vue/package.json</read_first>
  <action>
安装以下依赖:

```bash
npm install element-plus vue-router axios jwt-decode @element-plus/icons-vue
```

更新 package.json 添加依赖版本:
- element-plus: ^2.x
- vue-router: ^4.x
- axios: ^1.x
- jwt-decode: ^4.x
- @element-plus/icons-vue: 最新版本
  </action>
  <verify>
<automated>grep -q "element-plus" app-vue/package.json && echo "PASS" || echo "FAIL"</automated>
  </verify>
  <done>Element Plus, vue-router, axios, jwt-decode, @element-plus/icons-vue 已安装</done>
</task>

<task type="auto">
  <name>Task 2: 配置 main.js 引入 Element Plus</name>
  <files>app-vue/src/main.js</files>
  <read_first>app-vue/src/main.js</read_first>
  <action>
更新 main.js:

```javascript
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

// 引入 Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// 引入图标
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const app = createApp(App)

// 注册所有图标组件
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(router)
app.use(ElementPlus)

app.mount('#app')
```
  </action>
  <verify>
<automated>grep -q "ElementPlus" app-vue/src/main.js && echo "PASS" || echo "FAIL"</automated>
  </done>
  <done>main.js 已配置 Element Plus 和图标</done>
</task>

<task type="auto">
  <name>Task 3: 创建认证 API 模块</name>
  <files>app-vue/src/api/auth.js</files>
  <read_first>app-vue/src/main.js</read_first>
  <action>
创建 app-vue/src/api/auth.js:

```javascript
import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器 - 添加 token
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res
    } else {
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  error => {
    return Promise.reject(error)
  }
)

export function login(username, password) {
  return request.post('/auth/login', { username, password })
}

export function logout() {
  return request.post('/auth/logout')
}

export function getCurrentUser() {
  return request.get('/auth/current')
}

export default request
```
  </action>
  <verify>
<automated>ls app-vue/src/api/auth.js && echo "PASS"</automated>
  </verify>
  <done>auth.js 已创建，包含 login, logout, getCurrentUser 方法</done>
</task>

<task type="auto">
<name>Task 4: 创建用户状态管理</name>
  <files>app-vue/src/store/user.js</files>
  <read_first>app-vue/src/api/auth.js</read_first>
  <action>
创建 app-vue/src/store/user.js:

```javascript
import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { login as apiLogin, logout as apiLogout, getCurrentUser } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(null)
  
  const isLoggedIn = computed(() => !!token.value)
  
  async function login(username, password) {
    const res = await apiLogin(username, password)
    token.value = res.data.token
    user.value = res.data.user
    localStorage.setItem('token', token.value)
    localStorage.setItem('user', JSON.stringify(user.value))
  }
  
  async function logout() {
    try {
      await apiLogout()
    } catch (e) {
      // 忽略登出错误
    }
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }
  
  async function fetchCurrentUser() {
    if (!token.value) return null
    try {
      const res = await getCurrentUser()
      user.value = res.data
      localStorage.setItem('user', JSON.stringify(user.value))
      return user.value
    } catch (e) {
      logout()
      return null
    }
  }
  
  function initFromStorage() {
    const savedUser = localStorage.getItem('user')
    if (savedUser) {
      try {
        user.value = JSON.parse(savedUser)
      } catch (e) {
        user.value = null
      }
    }
  }
  
  return {
    token,
    user,
    isLoggedIn,
    login,
    logout,
    fetchCurrentUser,
    initFromStorage
  }
})
```
  </action>
  <verify>
<automated>ls app-vue/src/store/user.js && echo "PASS"</automated>
  </verify>
  <done>user.js 已创建，包含用户状态管理和登录/登出方法</done>
</task>

<task type="auto">
<name>Task 5: 创建路由配置和守卫</name>
  <files>app-vue/src/router/index.js</files>
  <read_first>app-vue/src/store/user.js</read_first>
  <action>
创建 app-vue/src/router/index.js:

```javascript
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard/index',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'index',
        name: 'DashboardIndex',
        component: () => import('@/views/DashboardIndex.vue'),
        meta: { title: '首页', requiresAuth: true }
      },
      {
        path: 'system/user',
        name: 'UserManage',
        component: () => import('@/views/system/UserManage.vue'),
        meta: { title: '用户管理', requiresAuth: true }
      },
      {
        path: 'system/role',
        name: 'RoleManage',
        component: () => import('@/views/system/RoleManage.vue'),
        meta: { title: '角色管理', requiresAuth: true }
      },
      {
        path: 'audit',
        name: 'AuditLog',
        component: () => import('@/views/AuditLog.vue'),
        meta: { title: '审计日志', requiresAuth: true }
      },
      {
        path: 'business',
        name: 'BusinessData',
        component: () => import('@/views/BusinessData.vue'),
        meta: { title: '业务数据', requiresAuth: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  userStore.initFromStorage()
  
  const requiresAuth = to.meta.requiresAuth !== false
  
  if (requiresAuth && !userStore.isLoggedIn) {
    next('/login')
  } else if (to.path === '/login' && userStore.isLoggedIn) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
```
  </action>
  <verify>
<automated>ls app-vue/src/router/index.js && echo "PASS"</automated>
  </verify>
  <done>router/index.js 已创建，包含路由配置和登录守卫</done>
</task>

<task type="auto">
<name>Task 6: 创建登录页面 Login.vue</name>
  <files>app-vue/src/views/Login.vue</files>
  <read_first>app-vue/src/router/index.js</read_first>
  <action>
创建 app-vue/src/views/Login.vue (UI-01 登录页面):

```vue
<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const loginForm = reactive({
  username: '',
  password: ''
})

const loading = ref(false)
const loginFormRef = ref(null)

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

async function handleLogin() {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      await userStore.login(loginForm.username, loginForm.password)
      ElMessage.success('登录成功')
      router.push('/dashboard')
    } catch (error) {
      ElMessage.error(error.message || '用户名或密码错误')
    } finally {
      loading.value = false
    }
  })
}

function handleKeydown(e) {
  if (e.key === 'Enter') {
    handleLogin()
  }
}
</script>

<template>
  <div class="login-container" @keydown="handleKeydown">
    <div class="login-card">
      <div class="login-header">
        <h1 class="login-title">管理系统</h1>
        <p class="login-subtitle">欢迎登录</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="rules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-button"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
}

.login-card {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-title {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 8px 0;
}

.login-subtitle {
  font-size: 14px;
  color: #606266;
  margin: 0;
}

.login-form {
  margin-top: 24px;
}

.login-form :deep(.el-input__wrapper) {
  padding: 4px 12px;
}

.login-button {
  width: 100%;
  height: 40px;
  font-size: 16px;
}
</style>
```

按照 UI-SPEC.md 设计规范: 卡片400px, 圆角8px, 标题28px, 输入框间距16px
  </action>
  <verify>
<automated>ls app-vue/src/views/Login.vue && echo "PASS"</automated>
  </verify>
  <done>Login.vue 已创建，符合 UI-SPEC.md 设计规范</done>
</task>

<task type="auto">
<name>Task 7: 创建管理后台布局 Layout.vue</name>
  <files>app-vue/src/views/Layout.vue</files>
  <read_first>app-vue/src/views/Login.vue</read_first>
  <action>
创建 app-vue/src/views/Layout.vue (UI-02 管理后台主界面):

```vue
<script setup>
import { ref } from 'vue'
import AppMenu from '@/components/AppMenu.vue'
import AppHeader from '@/components/AppHeader.vue'
import AppTabs from '@/components/AppTabs.vue'

const collapsed = ref(false)

function toggleCollapse() {
  collapsed.value = !collapsed.value
}
</script>

<template>
  <el-container class="layout-container">
    <!-- 左侧菜单 -->
    <el-aside :width="collapsed ? '64px' : '200px'" class="layout-aside">
      <div class="logo">
        <span v-if="!collapsed">管理系统</span>
        <span v-else>M</span>
      </div>
      <AppMenu :collapsed="collapsed" />
    </el-aside>
    
    <el-container>
      <!-- 顶部头部 -->
      <el-header class="layout-header">
        <div class="header-left">
          <el-button text @click="toggleCollapse">
            <el-icon><Fold v-if="!collapsed" /><Expand v-else /></el-icon>
          </el-button>
        </div>
        <AppHeader />
      </el-header>
      
      <!-- 页签栏 -->
      <AppTabs />
      
      <!-- 内容区域 -->
      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <keep-alive>
            <component :is="Component" />
          </keep-alive>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout-container {
  height: 100vh;
}

.layout-aside {
  background: #304156;
  transition: width 0.3s;
  overflow-x: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #263445;
  color: #fff;
  font-size: 18px;
  font-weight: 600;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 16px;
}

.header-left {
  display: flex;
  align-items: center;
}

.layout-main {
  background: #f5f7fa;
  padding: 16px;
  min-height: calc(100vh - 100px);
}
</style>
```

按照 UI-SPEC.md: Header 60px, 侧边栏 200px/64px, 内容区背景 #F5F7FA
  </action>
  <verify>
<automated>ls app-vue/src/views/Layout.vue && echo "PASS"</automated>
  </verify>
  <done>Layout.vue 已创建，符合 UI-SPEC.md 设计规范</done>
</task>

<task type="auto">
<name>Task 8: 创建左侧菜单组件 AppMenu.vue</name>
  <files>app-vue/src/components/AppMenu.vue</files>
  <read_first>app-vue/src/views/Layout.vue</read_first>
  <action>
创建 app-vue/src/components/AppMenu.vue (MENU-01, MENU-03):

```vue
<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const props = defineProps({
  collapsed: {
    type: Boolean,
    default: false
  }
})

const route = useRoute()
const router = useRouter()

const defaultActive = computed(() => route.path)

const menuItems = [
  {
    path: '/dashboard/index',
    title: '首页',
    icon: 'House'
  },
  {
    path: '/dashboard/system',
    title: '系统管理',
    icon: 'Setting',
    children: [
      {
        path: '/dashboard/system/user',
        title: '用户管理',
        icon: 'User'
      },
      {
        path: '/dashboard/system/role',
        title: '角色管理',
        icon: 'Key'
      }
    ]
  },
  {
    path: '/dashboard/audit',
    title: '审计日志',
    icon: 'Document'
  },
  {
    path: '/dashboard/business',
    title: '业务数据',
    icon: 'Folder'
  }
]

function handleSelect(path) {
  router.push(path)
}
</script>

<template>
  <el-menu
    :default-active="defaultActive"
    :collapse="collapsed"
    class="app-menu"
    background-color="#304156"
    text-color="#bfcbd9"
    active-text-color="#409eff"
    @select="handleSelect"
  >
    <template v-for="item in menuItems" :key="item.path">
      <el-sub-menu v-if="item.children" :index="item.path">
        <template #title>
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </template>
        <el-menu-item
          v-for="child in item.children"
          :key="child.path"
          :index="child.path"
        >
          <el-icon><component :is="child.icon" /></el-icon>
          <span>{{ child.title }}</span>
        </el-menu-item>
      </el-sub-menu>
      
      <el-menu-item v-else :index="item.path">
        <el-icon><component :is="item.icon" /></el-icon>
        <span>{{ item.title }}</span>
      </el-menu-item>
    </template>
  </el-menu>
</template>

<style scoped>
.app-menu {
  border-right: none;
}

.app-menu:not(.el-menu--collapse) {
  width: 200px;
}

.app-menu .el-menu-item,
.app-menu .el-sub-menu__title {
  height: 50px;
  line-height: 50px;
}

.app-menu .el-icon {
  margin-right: 8px;
}
</style>
```

支持多级菜单展开，MENU-01 和 MENU-03
  </action>
  <verify>
<automated>ls app-vue/src/components/AppMenu.vue && echo "PASS"</automated>
  </verify>
  <done>AppMenu.vue 已创建，支持多级菜单展开</done>
</task>

<task type="auto">
<name>Task 9: 创建页签切换组件 AppTabs.vue</name>
  <files>app-vue/src/components/AppTabs.vue</files>
  <read_first>app-vue/src/components/AppMenu.vue</read_first>
  <action>
创建 app-vue/src/components/AppTabs.vue (MENU-04):

```vue
<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const tabs = ref([
  { path: '/dashboard/index', title: '首页' }
])

const activeTab = ref('/dashboard/index')

// 监听路由变化，自动添加页签
watch(() => route.path, (newPath) => {
  if (newPath.startsWith('/dashboard/') && newPath !== '/dashboard') {
    const title = route.meta?.title || newPath.split('/').pop()
    const existing = tabs.value.find(t => t.path === newPath)
    if (!existing) {
      tabs.value.push({ path: newPath, title })
    }
    activeTab.value = newPath
  }
}, { immediate: true })

function handleTabClick(path) {
  router.push(path)
}

function handleTabClose(path) {
  if (path === '/dashboard/index') return
  
  const index = tabs.value.findIndex(t => t.path === path)
  tabs.value.splice(index, 1)
  
  if (activeTab.value === path) {
    const newTab = tabs.value[Math.max(0, index - 1)]
    router.push(newTab.path)
  }
}
</script>

<template>
  <div class="app-tabs">
    <el-tabs v-model="activeTab" type="card" @tab-click="handleTabClick">
      <el-tab-pane
        v-for="tab in tabs"
        :key="tab.path"
        :label="tab.title"
        :name="tab.path"
        :closable="tab.path !== '/dashboard/index'"
        @close="handleTabClose(tab.path)"
      />
    </el-tabs>
  </div>
</template>

<style scoped>
.app-tabs {
  background: #fff;
  padding: 0 16px;
  border-bottom: 1px solid #e4e7ed;
}

.app-tabs :deep(.el-tabs__header) {
  margin: 0;
}

.app-tabs :deep(.el-tabs__item) {
  height: 40px;
  line-height: 40px;
}
</style>
```
  </action>
  <verify>
<automated>ls app-vue/src/components/AppTabs.vue && echo "PASS"</automated>
  </verify>
  <done>AppTabs.vue 已创建，支持页签式多任务切换</done>
</task>

<task type="auto">
<name>Task 10: 创建顶部用户栏组件 AppHeader.vue</name>
  <files>app-vue/src/components/AppHeader.vue</files>
  <read_first>app-vue/src/components/AppTabs.vue</read_first>
  <action>
创建 app-vue/src/components/AppHeader.vue (MENU-05):

```vue
<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const username = computed(() => userStore.user?.name || userStore.user?.username || '未登录')
const avatar = computed(() => userStore.user?.name?.charAt(0) || 'U')

async function handleCommand(command) {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      
      await userStore.logout()
      router.push('/login')
    } catch (e) {
      // 用户取消
    }
  } else if (command === 'profile') {
    // TODO: 跳转到个人中心
  }
}
</script>

<template>
  <div class="app-header">
    <div class="header-user">
      <el-dropdown @command="handleCommand">
        <div class="user-info">
          <el-avatar :size="32" class="user-avatar">
            {{ avatar }}
          </el-avatar>
          <span class="username">{{ username }}</span>
          <el-icon><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>
              个人中心
            </el-dropdown-item>
            <el-dropdown-item command="logout" divided>
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<style scoped>
.app-header {
  display: flex;
  align-items: center;
}

.header-user {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar {
  background: #409eff;
  color: #fff;
}

.username {
  font-size: 14px;
  color: #303133;
}
</style>
```

显示当前登录用户名和个人中心/退出登录下拉菜单
  </action>
  <verify>
<automated>ls app-vue/src/components/AppHeader.vue && echo "PASS"</automated>
  </verify>
  <done>AppHeader.vue 已创建，显示用户名和登出按钮</done>
</task>

<task type="auto">
<name>Task 11: 创建占位页面组件</name>
  <files>app-vue/src/views/DashboardIndex.vue, app-vue/src/views/system/UserManage.vue, app-vue/src/views/system/RoleManage.vue, app-vue/src/views/AuditLog.vue, app-vue/src/views/BusinessData.vue</files>
  <read_first>app-vue/src/router/index.js</read_first>
  <action>
创建占位页面 (空实现，后续 Phase 完善):

1. app-vue/src/views/DashboardIndex.vue:
```vue
<template>
  <div class="page-content">
    <h2>首页</h2>
  </div>
</template>

<style scoped>
.page-content {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
}
</style>
```

2. app-vue/src/views/system/UserManage.vue:
```vue
<template>
  <div class="page-content">
    <h2>用户管理</h2>
  </div>
</template>

<style scoped>
.page-content {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
}
</style>
```

3. app-vue/src/views/system/RoleManage.vue:
```vue
<template>
  <div class="page-content">
    <h2>角色管理</h2>
  </div>
</template>

<style scoped>
.page-content {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
}
</style>
```

4. app-vue/src/views/AuditLog.vue:
```vue
<template>
  <div class="page-content">
    <h2>审计日志</h2>
  </div>
</template>

<style scoped>
.page-content {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
}
</style>
```

5. app-vue/src/views/BusinessData.vue:
```vue
<template>
  <div class="page-content">
    <h2>业务数据</h2>
  </div>
</template>

<style scoped>
.page-content {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
}
</style>
```
  </action>
  <verify>
<automated>ls app-vue/src/views/DashboardIndex.vue && echo "PASS"</automated>
  </verify>
  <done>占位页面已创建，路由可正常访问</done>
</task>

<task type="auto">
<name>Task 12: 配置 Vite 代理和 CORS</name>
  <files>app-vue/vite.config.js</files>
  <read_first>app-vue/vite.config.js</read_first>
  <action>
更新 vite.config.js 添加代理配置:

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```
  </action>
  <verify>
<automated>grep -q "proxy" app-vue/vite.config.js && echo "PASS" || echo "FAIL"</automated>
  </verify>
  <done>Vite 代理配置完成，前端请求 /api 转发到后端 8080 端口</done>
</task>

</tasks>

<verification>
手动验证步骤：
1. 启动 MySQL 和后端服务
2. 启动前端: `cd app-vue && npm run dev`
3. 访问 http://localhost:5173/login
4. 输入 admin/admin123 登录
5. 验证进入管理后台，显示左侧菜单、顶部用户栏、页签栏
6. 点击菜单项，验证页签添加
7. 点击退出登录，验证跳转登录页

自动验证:
- 后端测试: mvn test -Dtest=AuthControllerTest
- 前端测试: npm run build 无错误
</verification>

<success_criteria>
- [ ] 登录页面设计精美，符合 UI-SPEC.md 设计规范
- [ ] 管理后台主界面布局: 左侧菜单 + 顶部用户栏 + 内容区
- [ ] 左侧菜单支持多级展开
- [ ] 页签式多任务切换正常
- [ ] 顶部显示当前登录用户名
- [ ] 退出登录功能正常
- [ ] 未登录访问自动跳转登录页
</success_criteria>

<output>
After completion, create `.planning/phases/01-foundation/{phase}-02-SUMMARY.md`
</output>
