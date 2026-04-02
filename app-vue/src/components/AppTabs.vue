<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const tabs = ref([
  { path: '/dashboard/index', title: '首页' }
])

const activeTab = ref('/dashboard/index')

// 监听路由变化，自动添加页签
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

function handleTabClick(path) {
  router.push(path)
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
    <el-tabs v-model="activeTab" type="card" @tab-click="handleTabClick">
      <el-tab-pane
        v-for="tab in tabs"
        :key="tab.path"
        :label="tab.title"
        :name="tab.path"
        :closable="tab.path !== '/dashboard/index'"
        @close="handleTabClose(tab.path)"
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
</style>
