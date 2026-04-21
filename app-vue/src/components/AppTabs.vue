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

// 右键菜单状态
const contextMenuVisible = ref(false)
const contextMenuPosition = ref({ x: 0, y: 0 })
const rightClickedTab = ref<Tab | null>(null)

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

// 右键菜单 - 通过 event.target 找到对应的 tab
function handleContextMenu(event: MouseEvent) {
  // 找到最近的 .el-tabs__item 元素
  const tabItem = (event.target as HTMLElement).closest('.el-tabs__item')
  if (!tabItem) return

  // Element Plus 会生成 id="tab-{path}" 格式的 id
  const id = tabItem.getAttribute('id')
  if (!id || !id.startsWith('tab-')) return

  const path = id.substring(4) // 去掉 "tab-" 前缀
  const tab = tabs.value.find(t => t.path === path)
  if (!tab) return

  event.preventDefault()
  rightClickedTab.value = tab
  contextMenuPosition.value = { x: event.clientX, y: event.clientY }
  contextMenuVisible.value = true
}

// 关闭右键菜单
function closeContextMenu() {
  contextMenuVisible.value = false
  rightClickedTab.value = null
}

// 关闭全部（保留首页）
function closeAll() {
  tabs.value = tabs.value.filter(t => t.path === '/index')
  if (activeTab.value !== '/index') {
    router.push('/index')
  }
  closeContextMenu()
}

// 关闭其他（保留当前和首页）
function closeOthers() {
  if (!rightClickedTab.value) return
  tabs.value = tabs.value.filter(t => t.path === '/index' || t.path === rightClickedTab.value!.path)
  if (!tabs.value.find(t => t.path === activeTab.value)) {
    router.push(rightClickedTab.value.path)
  }
  closeContextMenu()
}

// 刷新当前标签页
function refreshCurrentTab() {
  if (!rightClickedTab.value) return
  // 如果刷新的是当前激活的标签页，使用 router.replace 刷新
  if (rightClickedTab.value.path === activeTab.value) {
    router.replace(rightClickedTab.value.path)
  } else {
    // 如果刷新的是非激活标签页，先切换过去再刷新
    router.push(rightClickedTab.value.path)
    setTimeout(() => {
      router.replace(rightClickedTab.value!.path)
    }, 100)
  }
  closeContextMenu()
}

// 点击其他区域关闭右键菜单
function handleDocumentClick() {
  if (contextMenuVisible.value) {
    closeContextMenu()
  }
}
</script>

<template>
  <div class="app-tabs" @click="handleDocumentClick">
    <el-tabs
      v-model="activeTab"
      type="card"
      @tab-click="handleTabClick"
      @tab-remove="handleTabClose"
      @contextmenu="handleContextMenu"
    >
      <el-tab-pane
        v-for="tab in tabs"
        :key="tab.path"
        :label="tab.title"
        :name="tab.path"
        :closable="tab.path !== '/index'"
      />
    </el-tabs>

    <!-- 右键菜单 -->
    <Teleport to="body">
      <div
        v-if="contextMenuVisible"
        class="context-menu"
        :style="{ left: contextMenuPosition.x + 'px', top: contextMenuPosition.y + 'px' }"
        @click.stop
      >
        <div class="context-menu-item" @click="refreshCurrentTab">
          <span>刷新</span>
        </div>
        <div class="context-menu-item" @click="closeOthers">
          <span>关闭其他</span>
        </div>
        <div class="context-menu-item" @click="closeAll">
          <span>关闭全部</span>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.app-tabs {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  padding: 0 24px;
  border-bottom: 1px solid rgba(147, 197, 253, 0.5);
  position: relative;
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

/* 右键菜单样式 */
.context-menu {
  position: fixed;
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 4px 0;
  z-index: 9999;
  min-width: 100px;
}

.context-menu-item {
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
  transition: background-color 0.2s;
}

.context-menu-item:hover {
  background: #f5f7fa;
  color: #409eff;
}
</style>
