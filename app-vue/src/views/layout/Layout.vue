<script setup>
import { ref } from 'vue'
import AppMenu from '@/components/AppMenu.vue'
import AppHeader from '@/components/AppHeader.vue'
import AppTabs from '@/components/AppTabs.vue'
import { useThemeStore } from '@/store/theme'

const themeStore = useThemeStore()
const collapsed = ref(false)

function toggleCollapse() {
  collapsed.value = !collapsed.value
}
</script>

<template>
  <el-container class="layout-container">
    <el-aside :width="collapsed ? '64px' : '220px'" class="layout-aside">
      <div class="logo">
        <span v-if="!collapsed">管理系统</span>
        <span v-else>M</span>
      </div>
      <AppMenu :collapsed="collapsed" />
    </el-aside>
    
    <el-container class="main-container">
      <el-header class="layout-header">
        <div class="header-left">
          <el-button text @click="toggleCollapse">
            <el-icon size="20"><Fold v-if="!collapsed" /><Expand v-else /></el-icon>
          </el-button>
        </div>
        <AppHeader />
      </el-header>
      
      <AppTabs />
      
      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <keep-alive>
            <component :is="Component" />
          </keep-alive>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout-container {
  min-height: 100vh;
}

.layout-aside {
  background: #304155;
  transition: width 0.3s;
}

.layout-aside :deep(.el-menu) {
  border-right: none;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  background: #263445;
  border-bottom: 1px solid #3d4d5d;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
}

.layout-main {
  padding: 16px;
  background: #f5f7fa;
}

:deep(.dark) .layout-aside {
  background: #1d1f20;
  border-color: #3d3d3d;
}

:deep(.dark) .logo {
  background: #121417;
  border-color: #2d2d2d;
  color: #e0e0e0;
}

:deep(.dark) .layout-header {
  background: #1d1f20;
  border-color: #3d3d3d;
  color: #e0e0e0;
}

:deep(.dark) .layout-main {
  background: #121417;
  color: #e0e0e0;
}
</style>
