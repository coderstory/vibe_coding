<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const tabs = ref([
  { path: '/dashboard/index', title: '首页' }
])

const activeTab = ref('/dashboard/index')

watch(() => route.path, (newPath) => {
  if (newPath.startsWith('/dashboard/') && newPath !== '/dashboard') {
    const title = route.meta?.title || newPath.split('/').pop()
    const existing = tabs.value.find(t => t.path === newPath)
    if (!existing) {
      tabs.value.push({ path: newPath, title })
    }
    activeTab.value = newPath
  }
}, { immediate: true })

function handleTabClick(tab) {
  router.push(tab.props.name)
}

function handleTabClose(path) {
  if (path === '/dashboard/index') return
  
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
        :closable="tab.path !== '/dashboard/index'"
      />
    </el-tabs>
  </div>
</template>

<style scoped>
.app-tabs {
  background: #fff;
  padding: 0 16px;
  border-bottom: 1px solid #e4e7ed;
}

.app-tabs :deep(.el-tabs__header) {
  margin: 0;
}

.app-tabs :deep(.el-tabs__item) {
  height: 40px;
  line-height: 40px;
}

:deep(.dark) .app-tabs {
  background: #1d1f20;
  border-color: #3d3d3d;
}

:deep(.dark) .app-tabs .el-tabs__header {
  border-color: #3d3d3d;
}

:deep(.dark) .app-tabs .el-tabs__item {
  color: #a0a0a0;
  background: #1d1f20;
  border-color: #3d3d3d;
}

:deep(.dark) .app-tabs .el-tabs__item:hover {
  color: #e0e0e0;
}

:deep(.dark) .app-tabs .el-tabs__item.is-active {
  color: #409eff;
  background: #2d2d2d;
  border-color: #409eff;
}
</style>
