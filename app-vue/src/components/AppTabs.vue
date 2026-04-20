<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

interface Tab {
  path: string
  title: string
}

const route = useRoute()
const router = useRouter()

const tabs = ref<Tab[]>([
  { path: '/index', title: '首页' }
])

const activeTab = ref('/index')

watch(() => route.path, (newPath) => {
  if (newPath.startsWith('/') && newPath !== '/') {
    const title = (route.meta?.title as string) || newPath.split('/').pop() || ''
    const existing = tabs.value.find(t => t.path === newPath)
    if (!existing) {
      tabs.value.push({ path: newPath, title })
    }
    activeTab.value = newPath
  }
}, { immediate: true })

function handleTabClick(tab: { props: { name: string } }) {
  router.push(tab.props.name)
}

function handleTabClose(path: string) {
  if (path === '/index') return

  const index = tabs.value.findIndex(t => t.path === path)
  tabs.value.splice(index, 1)

  if (activeTab.value === path) {
    const newTab = tabs.value[Math.max(0, index - 1)]
    router.push(newTab.path)
  }
}
</script>

<template>
  <div class="app-tabs">
    <el-tabs
      v-model="activeTab"
      type="card"
      @tab-click="handleTabClick"
      @tab-remove="handleTabClose"
    >
      <el-tab-pane
        v-for="tab in tabs"
        :key="tab.path"
        :label="tab.title"
        :name="tab.path"
        :closable="tab.path !== '/index'"
      />
    </el-tabs>
  </div>
</template>

<style scoped>
.app-tabs {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  padding: 0 24px;
  border-bottom: 1px solid rgba(147, 197, 253, 0.5);
}

.app-tabs :deep(.el-tabs__header) {
  margin: 0;
}

.app-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.app-tabs :deep(.el-tabs__item) {
  height: 42px;
  line-height: 42px;
  color: #64748b;
  font-size: 14px;
  font-weight: 500;
  border: none;
  border-bottom: 3px solid transparent;
  padding: 0 20px;
  background: transparent;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  margin-right: 4px;
  border-radius: 8px 8px 0 0;
}

.app-tabs :deep(.el-tabs__item:hover) {
  color: #1e3a8a;
  background: rgba(147, 197, 253, 0.2);
}

.app-tabs :deep(.el-tabs__item.is-active) {
  color: #1e3a8a;
  background: rgba(254, 243, 199, 0.5);
  border-bottom-color: #d97706;
  font-weight: 600;
}
</style>
