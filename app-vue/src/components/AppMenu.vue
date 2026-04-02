<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const props = defineProps({
  collapsed: {
    type: Boolean,
    default: false
  }
})

const route = useRoute()
const router = useRouter()

const defaultActive = computed(() => route.path)

const menuItems = [
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

function handleSelect(path) {
  router.push(path)
}
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
