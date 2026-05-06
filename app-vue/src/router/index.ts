/**
 * 路由配置
 * 定义应用的所有路由规则和导航守卫
 */
import {createRouter, createWebHistory} from 'vue-router'
import routes from './modules/routes'
import {setupGuards} from './guards'

/**
 * 创建路由实例
 */
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 设置导航守卫
setupGuards(router)

export default router
