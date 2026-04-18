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
    menuMap.set(menu.id, {
      id: menu.id,
      path: menu.path || `/dashboard/${menu.path}`,
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
    class="app-menu"
    @select="handleSelect"
  >
    <template v-for="item in menuItems" :key="item.path">
      <el-sub-menu v-if="item.children && item.children.length > 0" :index="item.path">
        <template #title>
          <el-icon :class="getIconColor(item.icon)"><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </template>
        <el-menu-item
          v-for="child in item.children"
          :key="child.path"
          :index="child.path"
        >
          <el-icon :class="getIconColor(child.icon)"><component :is="child.icon" /></el-icon>
          <span>{{ child.title }}</span>
        </el-menu-item>
      </el-sub-menu>

      <el-menu-item v-else :index="item.path">
        <el-icon :class="getIconColor(item.icon)"><component :is="item.icon" /></el-icon>
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

.app-menu .el-menu-item,
.app-menu .el-sub-menu__title {
  height: 48px;
  line-height: 48px;
  margin: 4px 10px;
  padding-left: 16px !important;
  color: rgba(255, 255, 255, 0.85);
  border-radius: 10px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 500;
}

.app-menu .el-icon {
  margin-right: 12px;
  font-size: 18px;
  vertical-align: middle;
  color: rgba(255, 255, 255, 0.6);
  transition: all 0.3s;
}

.app-menu .el-menu-item:hover,
.app-menu .el-sub-menu__title:hover {
  background: #fef3c7;
  color: #92400e;
  transform: translateX(4px);
}

.app-menu .el-menu-item:hover .el-icon,
.app-menu .el-sub-menu__title:hover .el-icon {
  color: #92400e;
  transform: scale(1.1);
}

.app-menu .el-menu-item.is-active {
  background: linear-gradient(90deg, #fef3c7 0%, #fde68a 100%);
  color: #92400e;
  font-weight: 600;
  position: relative;
  box-shadow: 0 2px 8px rgba(251, 191, 36, 0.3);
}

.app-menu .el-menu-item.is-active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 28px;
  background: linear-gradient(180deg, #d97706, #f59e0b);
  border-radius: 0 4px 4px 0;
}

.app-menu .el-menu-item.is-active .el-icon {
  color: #d97706;
}

.app-menu .el-menu-item.is-active::after {
  display: none;
}

/* Submenu styling */
.app-menu .el-sub-menu .el-menu {
  background: rgba(0, 0, 0, 0.1) !important;
  border-radius: 0 10px 10px 0;
}

.app-menu .el-sub-menu .el-menu-item {
  height: 44px;
  line-height: 44px;
  margin: 2px 10px;
  padding-left: 44px !important;
  color: rgba(255, 255, 255, 0.8);
}

.app-menu .el-sub-menu .el-menu-item:hover {
  background: #fef3c7;
  color: #92400e;
}

.app-menu .el-sub-menu .el-menu-item.is-active {
  background: linear-gradient(90deg, #fef3c7, #fde68a);
  color: #92400e;
}

.app-menu .el-sub-menu__title:hover {
  background: #fef3c7;
  color: #92400e;
}
</style>
