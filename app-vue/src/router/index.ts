/**
 * 路由配置
 * 定义应用的所有路由规则和导航守卫
 */
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store/user'

/**
 * 路由配置
 * 使用懒加载模式优化首屏加载性能
 */
const routes: RouteRecordRaw[] = [
  // 登录页 - 无需认证
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { requiresAuth: false }
  },
  // 根路径重定向
  {
    path: '/',
    redirect: '/index'
  },
  // 主布局 - 需要认证
  {
    path: '/',
    name: 'Dashboard',
    component: () => import('@/views/layout/Layout.vue'),
    redirect: '/index',
    meta: { requiresAuth: true },
    children: [
      {
        path: '/index',
        name: 'DashboardIndex',
        component: () => import('@/views/dashboard/DashboardIndex.vue'),
        meta: { title: '首页', requiresAuth: true }
      },
      {
        path: '/system/user',
        name: 'UserManage',
        component: () => import('@/views/system/UserManagement.vue'),
        meta: { title: '用户管理', requiresAuth: true }
      },
      {
        path: '/system/user/:id',
        name: 'UserDetail',
        component: () => import('@/views/system/UserDetail.vue'),
        meta: { title: '用户详情', requiresAuth: true }
      },
      {
        path: '/system/role',
        name: 'RoleManage',
        component: () => import('@/views/system/RoleManage.vue'),
        meta: { title: '角色管理', requiresAuth: true }
      },
      {
        path: '/system/menu',
        name: 'MenuManage',
        component: () => import('@/views/system/MenuManage.vue'),
        meta: { title: '菜单管理', requiresAuth: true }
      },
      {
        path: '/system/audit',
        name: 'AuditLog',
        component: () => import('@/views/audit/AuditLog.vue'),
        meta: { title: '审计日志', requiresAuth: true }
      },
      {
        path: '/business',
        name: 'BusinessData',
        component: () => import('@/views/business/BusinessData.vue'),
        meta: { title: '业务数据', requiresAuth: true }
      }
    ]
  },
  // 404 页面
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/NotFound.vue'),
    meta: { requiresAuth: false }
  }
]

/**
 * 创建路由实例
 */
const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 导航守卫
 * 实现登录拦截和页面访问控制
 */
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  userStore.initFromStorage()

  const requiresAuth = to.meta.requiresAuth !== false

  // 需要认证但未登录，重定向到登录页
  if (requiresAuth && !userStore.isLoggedIn) {
    next('/login')
  } else if (to.path === '/login' && userStore.isLoggedIn) {
    // 已登录访问登录页，重定向到首页
    next('/index')
  } else {
    next()
  }
})

export default router