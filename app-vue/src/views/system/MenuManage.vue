<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { getMenuTree, createMenu, updateMenu, deleteMenu } from '@/api/menu'
import type { MenuTree, Menu } from '@/api/types'
import type { ElTree } from 'element-plus'

// 菜单树数据
const menuTreeData = ref<MenuTree[]>([])
const loading = ref(false)
const menuTreeRef = ref<InstanceType<typeof ElTree> | null>(null)

// el-tree配置
const treeProps = {
  label: 'name',
  children: 'children'
}

// 新建/编辑菜单对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新建菜单')
const menuForm = reactive({
  id: null as number | null,
  parentId: 0 as number,
  name: '',
  path: '',
  icon: '',
  sortOrder: 0
})
const menuFormRef = ref<FormInstance | null>(null)
const isEdit = ref(false)

// 表单验证
const menuFormRules: FormRules = {
  name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  path: [{ required: true, message: '请输入菜单路径', trigger: 'blur' }]
}

// 加载菜单树
async function loadMenuTree() {
  loading.value = true
  try {
    const res = await getMenuTree()
    menuTreeData.value = res.data || []
  } catch {
    ElMessage.error('加载菜单列表失败')
  } finally {
    loading.value = false
  }
}

// 获取所有菜单节点（扁平化）
function getAllMenuNodes(): Menu[] {
  const nodes: Menu[] = []
  function flatten(list: MenuTree[]) {
    list.forEach(item => {
      nodes.push(item)
      if (item.children && item.children.length > 0) {
        flatten(item.children)
      }
    })
  }
  flatten(menuTreeData.value)
  return nodes
}

// 获取父级菜单选项
function getParentMenuOptions() {
  const nodes = getAllMenuNodes()
  return nodes.filter(node => !isEdit.value || node.id !== menuForm.id)
}

// 新建菜单
function handleCreate(parentId: number = 0) {
  isEdit.value = false
  dialogTitle.value = '新建菜单'
  menuForm.id = null
  menuForm.parentId = parentId
  menuForm.name = ''
  menuForm.path = ''
  menuForm.icon = ''
  menuForm.sortOrder = 0
  dialogVisible.value = true
}

// 编辑菜单
function handleEdit(row: Menu) {
  isEdit.value = true
  dialogTitle.value = '编辑菜单'
  menuForm.id = row.id
  menuForm.parentId = row.parentId || 0
  menuForm.name = row.name
  menuForm.path = row.path
  menuForm.icon = row.icon || ''
  menuForm.sortOrder = row.sortOrder || 0
  dialogVisible.value = true
}

// 删除菜单
function handleDelete(row: Menu) {
  ElMessageBox.confirm(
    `确定要删除菜单「${row.name}」吗？`,
    '删除确认',
    {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteMenu(row.id)
      ElMessage.success('菜单删除成功')
      loadMenuTree()
    } catch {
      ElMessage.error('菜单删除失败')
    }
  }).catch(() => {})
}

// 保存菜单
async function handleSaveMenu() {
  if (!menuFormRef.value) return

  await menuFormRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      if (isEdit.value) {
        await updateMenu(menuForm.id as number, menuForm)
        ElMessage.success('菜单更新成功')
      } else {
        await createMenu(menuForm)
        ElMessage.success('菜单创建成功')
      }
      dialogVisible.value = false
      loadMenuTree()
    } catch {
      ElMessage.error(isEdit.value ? '菜单更新失败' : '菜单创建失败')
    }
  })
}

// 添加子菜单
function handleAddChild(row: Menu) {
  handleCreate(row.id)
}

// 初始化
onMounted(() => {
  loadMenuTree()
})
</script>

<template>
  <div class="page-container">
    <h2 class="page-title">菜单管理</h2>

    <!-- 操作按钮 -->
    <div class="action-section">
      <el-button type="primary" @click="handleCreate()">新建菜单</el-button>
    </div>

    <!-- 菜单树 -->
    <el-table
      :data="menuTreeData"
      stripe
      border
      v-loading="loading"
      row-key="id"
      class="menu-table"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
    >
      <el-table-column prop="name" label="菜单名称" min-width="150" />
      <el-table-column prop="path" label="路由路径" min-width="200" />
      <el-table-column prop="icon" label="图标" width="100" align="center">
        <template #default="{ row }">
          <span v-if="row.icon">{{ row.icon }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleAddChild(row)">添加子菜单</el-button>
          <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新建/编辑菜单对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" :close-on-click-modal="false">
      <el-form ref="menuFormRef" :model="menuForm" :rules="menuFormRules" label-width="100px">
        <el-form-item label="上级菜单" prop="parentId">
          <el-select v-model="menuForm.parentId" placeholder="请选择上级菜单" style="width: 100%">
            <el-option label="顶级菜单" :value="0" />
            <el-option
              v-for="item in getParentMenuOptions()"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="菜单名称" prop="name">
          <el-input v-model="menuForm.name" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="路由路径" prop="path">
          <el-input v-model="menuForm.path" placeholder="请输入路由路径，如：/system/user" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="menuForm.icon" placeholder="请输入图标名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="menuForm.sortOrder" :min="0" :max="9999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveMenu">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-container {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
}

.page-title {
  margin: 0 0 20px 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.action-section {
  margin-bottom: 16px;
}

.menu-table {
  margin-bottom: 16px;
}
</style>
