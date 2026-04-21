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
      },
      {
        path: '/seckill',
        name: 'SeckillIndex',
        component: () => import('@/views/seckill/SeckillIndex.vue'),
        meta: { title: '秒杀首页', requiresAuth: true }
      },
      {
        path: '/seckill/goods',
        name: 'GoodsList',
        component: () => import('@/views/seckill/goods/GoodsList.vue'),
        meta: { title: '商品管理', requiresAuth: true }
      },
      {
        path: '/seckill/goods/add',
        name: 'GoodsAdd',
        component: () => import('@/views/seckill/goods/GoodsForm.vue'),
        meta: { title: '新增商品', requiresAuth: true }
      },
      {
        path: '/seckill/goods/:id',
        name: 'GoodsEdit',
        component: () => import('@/views/seckill/goods/GoodsForm.vue'),
        meta: { title: '编辑商品', requiresAuth: true }
      },
      {
        path: '/seckill/activity',
        name: 'ActivityList',
        component: () => import('@/views/seckill/activity/ActivityList.vue'),
        meta: { title: '活动管理', requiresAuth: true }
      },
      {
        path: '/seckill/activity/add',
        name: 'ActivityAdd',
        component: () => import('@/views/seckill/activity/ActivityForm.vue'),
        meta: { title: '新增活动', requiresAuth: true }
      },
      {
        path: '/seckill/activity/:id',
        name: 'ActivityEdit',
        component: () => import('@/views/seckill/activity/ActivityForm.vue'),
        meta: { title: '编辑活动', requiresAuth: true }
      },
      {
        path: '/seckill/detail/:id',
        name: 'SeckillDetail',
        component: () => import('@/views/seckill/SeckillDetail.vue'),
        meta: { title: '秒杀详情', requiresAuth: true }
      },
      {
        path: '/seckill/record',
        name: 'SeckillRecord',
        component: () => import('@/views/seckill/SeckillRecord.vue'),
        meta: { title: '抢购记录', requiresAuth: true }
      },
      {
        path: '/seckill/cart',
        name: 'SeckillCart',
        component: () => import('@/views/seckill/SeckillCart.vue'),
        meta: { title: '秒杀购物车', requiresAuth: true }
      },
      {
        path: '/order/list',
        name: 'OrderList',
        component: () => import('@/views/order/OrderList.vue'),
        meta: { title: '订单列表', requiresAuth: true }
      },
      {
        path: '/order/confirm',
        name: 'OrderConfirm',
        component: () => import('@/views/order/OrderConfirm.vue'),
        meta: { title: '订单确认', requiresAuth: true }
      },
      {
        path: '/monitor',
        name: 'MonitorDashboard',
        component: () => import('@/views/monitor/MonitorDashboard.vue'),
        meta: { title: '监控大盘', requiresAuth: true }
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