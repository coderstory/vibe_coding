<script setup lang="ts">
/**
 * 侧边栏菜单组件
 * 从后端加载菜单树，支持折叠和动态路由
 */
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getMenuTree } from '@/api/menu'
import type { Menu } from '@/api/types'

interface MenuItem {
  path: string
  title: string
  icon?: string
  id?: number
  parentId?: number
  children?: MenuItem[]
}

const props = defineProps<{
  collapsed: boolean
}>()

const route = useRoute()
const router = useRouter()

// 当前激活的菜单路径
const defaultActive = computed(() => route.path)

// 菜单数据
const menuItems = ref<MenuItem[]>([])

/**
 * 加载菜单数据
 */
async function loadMenus() {
  try {
    const res = await getMenuTree()
    const menus: Menu[] = res.data || []
    menuItems.value = convertToMenuItems(menus)
  } catch (error) {
    console.error('获取菜单失败', error)
    menuItems.value = []
  }
}

/**
 * 将后端菜单转换为前端菜单格式
 */
function convertToMenuItems(menus: Menu[]): MenuItem[] {
  function convertMenu(menu: Menu): MenuItem {
    // 构建完整路径
    let fullPath = ''
    if (menu.path) {
      // 后端返回 /dashboard 作为首页，实际路由是 /index
      if (menu.path === '/dashboard') {
        fullPath = '/index'
      } else if (menu.path.startsWith('/')) {
        fullPath = menu.path
      } else {
        fullPath = `/${menu.path}`
      }
    }

    // 递归转换子菜单
    const children = menu.children?.map(child => convertMenu(child)) || []

    return {
      id: menu.id,
      path: fullPath,
      title: menu.name,
      icon: menu.icon || 'Folder',
      parentId: menu.parentId,
      children: children.length > 0 ? children : undefined
    }
  }

  // 转换所有一级菜单
  const result = menus.map(menu => convertMenu(menu))
  console.log('转换后的菜单:', result)
  return result
}

/**
 * 处理菜单选择
 */
function handleSelect(index: string) {
  if (index.startsWith('/')) {
    router.push(index)
  }
}

onMounted(() => {
  loadMenus()
})
</script>

<template>
  <el-menu
    :default-active="defaultActive"
    :collapse="collapsed"
    :router="false"
    class="app-menu"
    @select="handleSelect"
  >
    <template v-for="item in menuItems" :key="item.id">
      <!-- 有子菜单的菜单项 -->
      <el-sub-menu v-if="item.children && item.children.length > 0" :index="String(item.id)">
        <template #title>
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </template>
        <el-menu-item
          v-for="child in item.children"
          :key="child.id"
          :index="child.path"
        >
          <el-icon><component :is="child.icon" /></el-icon>
          <span>{{ child.title }}</span>
        </el-menu-item>
      </el-sub-menu>

      <!-- 无子菜单的菜单项 -->
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
  background: transparent;
}

.app-menu:not(.el-menu--collapse) {
  width: 100%;
}

/* 基础菜单项样式 */
.app-menu :deep(.el-menu-item),
.app-menu :deep(.el-sub-menu__title) {
  height: 48px;
  line-height: 48px;
  margin: 4px 10px;
  padding-left: 16px !important;
  color: rgba(255, 255, 255, 0.85);
  border-radius: 10px;
  font-weight: 500;
}

.app-menu :deep(.el-icon) {
  margin-right: 12px;
  font-size: 18px;
  color: rgba(255, 255, 255, 0.6);
}

.app-menu :deep(.el-menu-item:hover),
.app-menu :deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.app-menu :deep(.el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  font-weight: 600;
}

/* 子菜单样式 */
.app-menu :deep(.el-sub-menu .el-menu) {
  background: rgba(0, 0, 0, 0.15) !important;
}

.app-menu :deep(.el-sub-menu .el-menu-item) {
  height: 44px;
  line-height: 44px;
  margin: 2px 10px;
  padding-left: 44px !important;
  color: rgba(255, 255, 255, 0.8);
}

.app-menu :deep(.el-sub-menu .el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.app-menu :deep(.el-sub-menu .el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}
</style>