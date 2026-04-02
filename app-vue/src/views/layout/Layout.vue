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
  <el-container class="layout-container" :class="{ 'dark-theme': themeStore.isDark }">
    <el-aside :width="collapsed ? '64px' : '220px'" class="layout-aside glass">
      <div class="logo">
        <span v-if="!collapsed" class="logo-text">管理系统</span>
        <span v-else class="logo-icon">M</span>
      </div>
      <AppMenu :collapsed="collapsed" />
    </el-aside>
    
    <el-container class="main-container">
      <el-header class="layout-header glass">
        <div class="header-left">
          <el-button text @click="toggleCollapse" class="toggle-btn">
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
  background: #f5f7fa;
  transition: background 0.3s;
}

.layout-container.dark-theme {
  background: #1a1a2e;
}

.layout-aside {
  background: rgba(48, 65, 86, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-right: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.dark-theme .layout-aside {
  background: rgba(22, 33, 62, 0.9);
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(38, 52, 69, 0.8);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-text {
  color: white;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 2px;
}

.logo-icon {
  color: #667eea;
  font-size: 24px;
  font-weight: 700;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  padding: 0 20px;
  transition: all 0.3s;
}

.dark-theme .layout-header {
  background: rgba(26, 26, 46, 0.8);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.toggle-btn {
  padding: 8px;
  border-radius: 8px;
  transition: background 0.3s;
}

.toggle-btn:hover {
  background: rgba(102, 126, 234, 0.1);
}

.layout-main {
  background: transparent;
  padding: 20px;
  min-height: calc(100vh - 120px);
}

.dark-theme .layout-main {
  background: transparent;
}
</style>