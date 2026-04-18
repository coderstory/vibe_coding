<script setup>
import { ref } from 'vue'
import AppMenu from '@/components/AppMenu.vue'
import AppHeader from '@/components/AppHeader.vue'
import AppTabs from '@/components/AppTabs.vue'
import '@/assets/themes/animations/_keyframes.css'
import '@/assets/themes/animations/_wave.css'
import '@/assets/themes/animations/_bubble.css'

const collapsed = ref(false)

function toggleCollapse() {
  collapsed.value = !collapsed.value
}
</script>

<template>
  <el-container class="layout-container">
    <div class="bubble-container">
      <div class="bubble bubble-1 bubble-delay-1"></div>
      <div class="bubble bubble-2 bubble-delay-2"></div>
      <div class="bubble bubble-3 bubble-delay-3"></div>
      <div class="bubble bubble-4 bubble-delay-4"></div>
      <div class="bubble bubble-5 bubble-delay-5"></div>
      <div class="bubble bubble-6 bubble-delay-6"></div>
      <div class="bubble bubble-7 bubble-delay-7"></div>
      <div class="bubble bubble-8 bubble-delay-8"></div>
      <div class="bubble bubble-9 bubble-delay-9"></div>
      <div class="bubble bubble-10 bubble-delay-10"></div>
    </div>
    <el-aside :width="collapsed ? '64px' : '220px'" class="layout-aside">
      <div class="wave-container">
        <div class="wave-layer wave-layer-1"></div>
        <div class="wave-layer wave-layer-2"></div>
        <div class="wave-layer wave-layer-3"></div>
      </div>
      <div class="logo">
        <span v-if="!collapsed">AI驱动的管理系统</span>
        <span v-else>M</span>
      </div>
      <AppMenu :collapsed="collapsed" />
    </el-aside>
    
    <el-container class="main-container">
      <el-header class="layout-header">
        <div class="header-left">
          <el-button text @click="toggleCollapse" class="collapse-btn">
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
  background: linear-gradient(180deg, #1e3a8a 0%, #3b82f6 100%);
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  position: relative;
}

.layout-aside :deep(.el-menu) {
  border-right: none;
  background: transparent;
}

.wave-container {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 1;
  pointer-events: none;
}

.wave-layer {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 200%;
  height: 100px;
  background-repeat: repeat-x;
  transform-origin: center bottom;
}

.wave-layer-1 {
  z-index: 1;
  animation: wave 8s linear infinite;
  opacity: 0.3;
}

.wave-layer-2 {
  z-index: 2;
  animation: wave 6s linear infinite reverse;
  opacity: 0.2;
}

.wave-layer-3 {
  z-index: 3;
  animation: wave 10s linear infinite;
  opacity: 0.15;
}

.logo, .layout-aside :deep(.el-menu) {
  position: relative;
  z-index: 40;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 18px;
  font-weight: 600;
  background: rgba(0, 0, 0, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  letter-spacing: 2px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(147, 197, 253, 0.5);
  box-shadow: 0 2px 8px rgba(30, 58, 138, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
}

.collapse-btn {
  width: 40px;
  height: 40px;
  border-radius: var(--el-border-radius-round);
  color: #3b82f6;
  transition: all 0.3s;
  background: transparent;
}

.collapse-btn:hover {
  background: linear-gradient(135deg, #fef3c7, #fde68a);
  color: #92400e;
}

.layout-main {
  padding: 24px;
  background: linear-gradient(180deg, #f0f9ff 0%, #eff6ff 100%);
  min-height: calc(100vh - 60px);
}
</style>
