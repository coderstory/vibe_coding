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

// 完整的菜单配置（用于超级管理员或获取权限失败时）
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

// 动态菜单数据（根据用户权限过滤）
const menuItems = ref([])

// 获取用户可见菜单
async function loadUserMenus() {
  try {
    const userInfo = JSON.parse(localStorage.getItem('user') || '{}')
    
    // 如果用户信息中没有id，说明未登录，使用空菜单
    if (!userInfo || !userInfo.id) {
      menuItems.value = []
      return
    }
    
    // 如果是超级管理员(roleId=1)，显示所有菜单
    if (userInfo.roleId === 1) {
      menuItems.value = allMenuItems
      return
    }
    
    // 普通用户：调用API获取该用户的菜单权限
    const res = await getUserMenus(userInfo.id)
    const userMenus = res.data || []
    
    // 过滤完整菜单配置，只显示用户有权限的菜单
    menuItems.value = filterMenusByPermissions(allMenuItems, userMenus)
  } catch (error) {
    console.error('获取用户菜单失败', error)
    // 获取失败时显示空菜单
    menuItems.value = []
  }
}

// 根据用户权限过滤菜单
function filterMenusByPermissions(fullMenus, allowedMenus) {
  const result = []
  
  // 将允许的菜单ID转换为集合，方便查找
  const allowedIds = new Set(allowedMenus.map(m => m.id))
  
  for (const menu of fullMenus) {
    // 检查当前菜单是否在允许列表中
    const menuId = menu.id || menu.path
    const isAllowed = allowedIds.has(menuId) || allowedMenus.some(m => m.path === menu.path)
    
    if (isAllowed) {
      // 如果有子菜单，递归过滤
      if (menu.children) {
        const filteredChildren = filterMenusByPermissions(menu.children, allowedMenus)
        // 只有当有可见的子菜单时才添加父菜单
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

// 初始化时加载用户菜单
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
</style>
