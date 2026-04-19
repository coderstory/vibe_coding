<script setup lang="ts">
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

const defaultActive = computed(() => route.path)

const menuItems = ref<MenuItem[]>([])

async function loadMenus() {
  try {
    // 从后端获取菜单树
    const res = await getMenuTree()
    const menus: Menu[] = res.data || []
    // 转换后端菜单格式为前端格式
    menuItems.value = convertToMenuItems(menus)
  } catch (error) {
    console.error('获取菜单失败', error)
    menuItems.value = []
  }
}

function convertToMenuItems(menus: Menu[]): MenuItem[] {
  const menuMap = new Map<number, MenuItem>()
  const rootMenus: MenuItem[] = []

  // 先创建所有菜单项
  menus.forEach(menu => {
    // 根据路径格式判断是顶级菜单还是子菜单
    let fullPath = menu.path || ''
    if (fullPath && !fullPath.startsWith('/dashboard')) {
      if (fullPath.startsWith('/')) {
        fullPath = `/dashboard${fullPath}`
      } else {
        fullPath = `/dashboard/${fullPath}`
      }
    } else if (!fullPath) {
      fullPath = '/dashboard'
    }

    menuMap.set(menu.id, {
      id: menu.id,
      path: fullPath,
      title: menu.name,
      icon: menu.icon || 'Folder',
      parentId: menu.parentId
    })
  })

  // 构建树形结构
  menus.forEach(menu => {
    const menuItem = menuMap.get(menu.id)!
    if (menu.parentId === 0 || !menu.parentId) {
      rootMenus.push(menuItem)
    } else {
      const parent = menuMap.get(menu.parentId)
      if (parent) {
        if (!parent.children) {
          parent.children = []
        }
        parent.children.push(menuItem)
      }
    }
  })

  return rootMenus
}

function handleSelect(path: string) {
  router.push(path)
}

function getIconColor(icon: string): string {
  const colorMap: Record<string, string> = {
    House: 'icon-dashboard',
    Setting: 'icon-system',
    Document: 'icon-audit',
    Folder: 'icon-business',
    User: 'icon-user',
    Key: 'icon-role',
    Menu: 'icon-menu'
  }
  return colorMap[icon] || 'icon-settings'
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
    <template v-for="item in menuItems" :key="item.path">
      <el-sub-menu v-if="item.children && item.children.length > 0" :index="String(item.id)">
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
