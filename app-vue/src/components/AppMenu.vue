<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUserMenus } from '@/api/menu'

const props = defineProps({
  collapsed: {
    type: Boolean,
    default: false
  }
})

const route = useRoute()
const router = useRouter()

const defaultActive = computed(() => route.path)

const allMenuItems = [
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

const menuItems = ref([])

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
    const userMenus = res.data || []
    menuItems.value = filterMenusByPermissions(allMenuItems, userMenus)
  } catch (error) {
    console.error('获取用户菜单失败', error)
    menuItems.value = []
  }
}

function filterMenusByPermissions(fullMenus, allowedMenus) {
  const result = []
  const allowedIds = new Set(allowedMenus.map(m => m.id))
  
  for (const menu of fullMenus) {
    const menuId = menu.id || menu.path
    const isAllowed = allowedIds.has(menuId) || allowedMenus.some(m => m.path === menu.path)
    
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

function handleSelect(path) {
  router.push(path)
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

:deep(.dark) .app-menu {
  background-color: #1d1f20 !important;
}

:deep(.dark) .app-menu .el-menu-item,
:deep(.dark) .app-menu .el-sub-menu__title {
  color: #a0a0a0 !important;
}

:deep(.dark) .app-menu .el-menu-item:hover,
:deep(.dark) .app-menu .el-sub-menu__title:hover {
  background-color: #2d2d2d !important;
}

:deep(.dark) .app-menu .el-menu-item.is-active {
  color: #409eff !important;
}
</style>
