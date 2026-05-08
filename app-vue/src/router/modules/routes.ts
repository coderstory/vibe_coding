import type { RouteRecordRaw } from 'vue-router'

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
      // 首页（需要认证）
      {
        path: '/index',
        name: 'DashboardIndex',
        component: () => import('@/views/dashboard/DashboardIndex.vue'),
        meta: { title: '首页', requiresAuth: true }
      },
      // 用户管理（需要认证）
      {
        path: '/system/user',
        name: 'UserManage',
        component: () => import('@/views/system/UserManagement.vue'),
        meta: { title: '用户管理', requiresAuth: true }
      },
      // 用户详情（需要认证）
      {
        path: '/system/user/:id',
        name: 'UserDetail',
        component: () => import('@/views/system/UserDetail.vue'),
        meta: { title: '用户详情', requiresAuth: true }
      },
      // 角色管理（需要认证）
      {
        path: '/system/role',
        name: 'RoleManage',
        component: () => import('@/views/system/RoleManage.vue'),
        meta: { title: '角色管理', requiresAuth: true }
      },
      // 菜单管理（需要认证）
      {
        path: '/system/menu',
        name: 'MenuManage',
        component: () => import('@/views/system/MenuManage.vue'),
        meta: { title: '菜单管理', requiresAuth: true }
      },
      // 审计日志（需要认证）
      {
        path: '/system/audit',
        name: 'AuditLog',
        component: () => import('@/views/audit/AuditLog.vue'),
        meta: { title: '审计日志', requiresAuth: true }
      },
      // 业务数据（需要认证）
      {
        path: '/business',
        name: 'BusinessData',
        component: () => import('@/views/business/BusinessData.vue'),
        meta: { title: '业务数据', requiresAuth: true }
      },
      // 秒杀首页（需要认证）
      {
        path: '/seckill',
        name: 'SeckillIndex',
        component: () => import('@/views/seckill/SeckillIndex.vue'),
        meta: { title: '秒杀首页', requiresAuth: true }
      },
      // 商品管理（需要认证）
      {
        path: '/seckill/goods',
        name: 'GoodsList',
        component: () => import('@/views/seckill/goods/GoodsList.vue'),
        meta: { title: '商品管理', requiresAuth: true }
      },
      // 新增商品（需要认证）
      {
        path: '/seckill/goods/add',
        name: 'GoodsAdd',
        component: () => import('@/views/seckill/goods/GoodsForm.vue'),
        meta: { title: '新增商品', requiresAuth: true }
      },
      // 编辑商品（需要认证）
      {
        path: '/seckill/goods/:id',
        name: 'GoodsEdit',
        component: () => import('@/views/seckill/goods/GoodsForm.vue'),
        meta: { title: '编辑商品', requiresAuth: true }
      },
      // 活动管理（需要认证）
      {
        path: '/seckill/activity',
        name: 'ActivityList',
        component: () => import('@/views/seckill/activity/ActivityList.vue'),
        meta: { title: '活动管理', requiresAuth: true }
      },
      // 新增活动（需要认证）
      {
        path: '/seckill/activity/add',
        name: 'ActivityAdd',
        component: () => import('@/views/seckill/activity/ActivityForm.vue'),
        meta: { title: '新增活动', requiresAuth: true }
      },
      // 编辑活动（需要认证）
      {
        path: '/seckill/activity/:id',
        name: 'ActivityEdit',
        component: () => import('@/views/seckill/activity/ActivityForm.vue'),
        meta: { title: '编辑活动', requiresAuth: true }
      },
      // 秒杀详情（需要认证）
      {
        path: '/seckill/detail/:id',
        name: 'SeckillDetail',
        component: () => import('@/views/seckill/SeckillDetail.vue'),
        meta: { title: '秒杀详情', requiresAuth: true }
      },
      // 抢购记录（需要认证）
      {
        path: '/seckill/record',
        name: 'SeckillRecord',
        component: () => import('@/views/seckill/SeckillRecord.vue'),
        meta: { title: '抢购记录', requiresAuth: true }
      },
      // 我的预约（需要认证）
      {
        path: '/seckill/reservations',
        name: 'MyReservations',
        component: () => import('@/views/seckill/MyReservations.vue'),
        meta: { title: '我的预约', requiresAuth: true }
      },
      // 秒杀购物车（需要认证）
      {
        path: '/seckill/cart',
        name: 'SeckillCart',
        component: () => import('@/views/seckill/SeckillCart.vue'),
        meta: { title: '秒杀购物车', requiresAuth: true }
      },
      // 订单列表（需要认证）
      {
        path: '/order/list',
        name: 'OrderList',
        component: () => import('@/views/order/OrderList.vue'),
        meta: { title: '订单列表', requiresAuth: true }
      },
      // 订单确认（需要认证）
      {
        path: '/order/confirm',
        name: 'OrderConfirm',
        component: () => import('@/views/order/OrderConfirm.vue'),
        meta: { title: '订单确认', requiresAuth: true }
      },
      // 监控大盘（需要认证）
      {
        path: '/monitor',
        name: 'MonitorDashboard',
        component: () => import('@/views/monitor/MonitorDashboard.vue'),
        meta: { title: '监控大盘', requiresAuth: true }
      },
      // Topic 管理（需要认证）
      {
        path: '/rocketmq/topics',
        name: 'TopicList',
        component: () => import('@/views/rocketmq/TopicList.vue'),
        meta: { title: 'Topic 管理', requiresAuth: true }
      },
      // Consumer Group 管理（需要认证）
      {
        path: '/rocketmq/consumer-groups',
        name: 'ConsumerGroupList',
        component: () => import('@/views/rocketmq/ConsumerGroupList.vue'),
        meta: { title: 'Consumer Group 管理', requiresAuth: true }
      },
      // 消息管理（需要认证）
      {
        path: '/rocketmq/messages',
        name: 'MessageList',
        component: () => import('@/views/rocketmq/MessageList.vue'),
        meta: { title: '消息管理', requiresAuth: true }
      },
      // 监控面板（需要认证）
      {
        path: '/rocketmq/dashboard',
        name: 'RocketMQDashboard',
        component: () => import('@/views/rocketmq/Dashboard.vue'),
        meta: { title: '监控面板', requiresAuth: true }
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

export default routes
