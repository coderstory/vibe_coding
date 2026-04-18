<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getCategoryTree, createCategory, deleteCategory } from '@/api/knowledge'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { KnowledgeCategory, KnowledgeCategoryTree } from '@/api/types'

const emit = defineEmits<{
  select: [category: KnowledgeCategory]
}>()

const treeRef = ref<InstanceType<typeof import('element-plus').ElTree> | null>(null)
const treeData = ref<KnowledgeCategoryTree[]>([])
const selectedId = ref<number | null>(null)

interface FlatCategory extends KnowledgeCategory {
  level: number
}

function flattenCategories(categories: KnowledgeCategoryTree[], result: FlatCategory[] = [], level = 0): FlatCategory[] {
  for (const cat of categories) {
    result.push({ ...cat, level })
    if (cat.children && cat.children.length > 0) {
      flattenCategories(cat.children, result, level + 1)
    }
  }
  return result
}

const flatCategories = ref<FlatCategory[]>([])

async function loadTree() {
  try {
    const res = await getCategoryTree()
    treeData.value = res.data || []
    flatCategories.value = flattenCategories(treeData.value)
  } catch {
    ElMessage.error('加载分类失败')
  }
}

function handleNodeClick(data: KnowledgeCategory) {
  selectedId.value = data.id
  emit('select', data)
}

async function handleAddRoot() {
  try {
    const result = await ElMessageBox.prompt('请输入分类名称', '新增分类')
    await createCategory({ name: result.value, parentId: 0, sortOrder: 0 })
    ElMessage.success('创建成功')
    loadTree()
  } catch {
    // user cancelled
  }
}

async function handleAddChild(data: KnowledgeCategory) {
  try {
    const result = await ElMessageBox.prompt('请输入子分类名称', '新增子分类')
    await createCategory({ name: result.value, parentId: data.id, sortOrder: 0 })
    ElMessage.success('创建成功')
    loadTree()
  } catch {
    // user cancelled
  }
}

async function handleDelete(data: KnowledgeCategory) {
  try {
    await ElMessageBox.confirm('确定删除该分类吗？', '警告', { type: 'warning' })
    await deleteCategory(data.id)
    ElMessage.success('删除成功')
    loadTree()
  } catch {
    // user cancelled
  }
}

onMounted(() => {
  loadTree()
})

defineExpose({ loadTree, flatCategories })
</script>

<template>
  <div class="category-tree">
    <div class="tree-header">
      <span>分类</span>
      <el-button text @click="handleAddRoot">
        <el-icon><Plus /></el-icon>
      </el-button>
    </div>
    <el-tree
      ref="treeRef"
      :data="treeData"
      :props="{ label: 'name', children: 'children' }"
      node-key="id"
      v-model:current-node-key="selectedId"
      highlight-current
      :expand-on-click-node="false"
      :default-expand-all="true"
      @node-click="handleNodeClick"
    >
      <template #default="{ node, data }">
        <span class="tree-node">
          <span>{{ node.label }}</span>
          <span class="node-actions">
            <el-button text size="small" @click.stop="handleAddChild(data)">+</el-button>
            <el-button text size="small" type="danger" @click.stop="handleDelete(data)">×</el-button>
          </span>
        </span>
      </template>
    </el-tree>
  </div>
</template>

<style scoped>
.category-tree {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.tree-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #eee;
  font-weight: 600;
}

.tree-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding-right: 8px;
}

.node-actions {
  display: none;
}

.tree-node:hover .node-actions {
  display: flex;
  gap: 4px;
}

@media (max-width: 768px) {
  .category-tree {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    width: 280px;
    background: #fff;
    z-index: 1000;
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  }
}
</style>
