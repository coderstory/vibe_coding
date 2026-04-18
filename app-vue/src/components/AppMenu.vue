<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUserMenus } from '@/api/menu'
import type { Menu } from '@/api/types'

interface MenuItem {
  path: string
  title: string
  icon?: string
  id?: number
  children?: MenuItem[]
}

const props = defineProps<{
  collapsed: boolean
}>()

const route = useRoute()
const router = useRouter()

const defaultActive = computed(() => route.path)

const allMenuItems: MenuItem[] = [
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

const menuItems = ref<MenuItem[]>([])

async function loadUserMenus() {
  try {
    const userInfo = JSON.parse(localStorage.getItem('user') || '{}')

    if (!userInfo || !userInfo.id) {
      menuItems.value = []
      return
    }

    if (userInfo.roleId === 1) {
      menuItems.value = allMenuItems
      return
    }

    const res = await getUserMenus(userInfo.id)
    const userMenus: Menu[] = res.data || []
    menuItems.value = filterMenusByPermissions(allMenuItems, userMenus)
  } catch (error) {
    console.error('获取用户菜单失败', error)
    menuItems.value = []
  }
}

function filterMenusByPermissions(fullMenus: MenuItem[], allowedMenus: Menu[]): MenuItem[] {
  const result: MenuItem[] = []
  const allowedIds = new Set(allowedMenus.map(m => m.id))

  for (const menu of fullMenus) {
    const menuId = menu.id || menu.path
    const isAllowed = allowedIds.has(menuId as number) || allowedMenus.some(m => m.path === menu.path)

    if (isAllowed) {
      if (menu.children) {
        const filteredChildren = filterMenusByPermissions(menu.children, allowedMenus)
        if (filteredChildren.length > 0) {
          result.push({
            ...menu,
            children: filteredChildren
          })
        }
      } else {
        result.push(menu)
      }
    }
  }

  return result
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
    Key: 'icon-role'
  }
  return colorMap[icon] || 'icon-settings'
}

onMounted(() => {
  loadUserMenus()
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
      <el-sub-menu v-if="item.children" :index="item.path">
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
