import type {Router} from 'vue-router'
import {useUserStore} from '@/store/user'

/**
 * 导航守卫
 * 实现登录拦截和页面访问控制
 */
export function setupGuards(router: Router) {
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
}
