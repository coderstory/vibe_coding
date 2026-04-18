<script setup lang="ts">
import { ref, onMounted } from 'vue'
import CategoryTree from '@/components/knowledge/CategoryTree.vue'
import ArticleEditor from '@/components/knowledge/ArticleEditor.vue'
import { getArticlePage, deleteArticle, searchArticles } from '@/api/knowledge'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { KnowledgeArticle, KnowledgeCategory, ArticleQueryParams } from '@/api/types'

const categoryTreeRef = ref<InstanceType<typeof CategoryTree> | null>(null)
const showMobileTree = ref(false)
const selectedCategory = ref<KnowledgeCategory | null>(null)
const searchKeyword = ref('')
const articleList = ref<KnowledgeArticle[]>([])
const editorVisible = ref(false)
const currentArticleId = ref<number | null>(null)
const pagination = ref({
  page: 1,
  size: 20,
  total: 0
})

async function loadArticles() {
  try {
    let res
    if (searchKeyword.value) {
      res = await searchArticles(searchKeyword.value)
      articleList.value = res.data || []
      pagination.value.total = articleList.value.length
    } else {
      const params: ArticleQueryParams = {
        page: pagination.value.page,
        size: pagination.value.size
      }
      if (selectedCategory.value?.id) {
        params.categoryId = selectedCategory.value.id
      }
      res = await getArticlePage(params)
      const data = res.data
      articleList.value = data.records || []
      pagination.value.total = data.total || 0
    }
  } catch {
    ElMessage.error('加载知识列表失败')
  }
}

function handleCategorySelect(category: KnowledgeCategory) {
  selectedCategory.value = category
  pagination.value.page = 1
  loadArticles()
  showMobileTree.value = false
}

async function handleSearch() {
  pagination.value.page = 1
  loadArticles()
}

function handleCreate() {
  if (!selectedCategory.value?.id) {
    ElMessage.warning('请先选择左侧分类')
    return
  }
  currentArticleId.value = null
  editorVisible.value = true
}

function handleEdit(row: KnowledgeArticle) {
  currentArticleId.value = row.id
  editorVisible.value = true
}

async function handleDelete(row: KnowledgeArticle) {
  try {
    await ElMessageBox.confirm('确定删除该知识吗？', '警告', { type: 'warning' })
    await deleteArticle(row.id)
    ElMessage.success('删除成功')
    loadArticles()
  } catch {
    // user cancelled
  }
}

function handleEditorSuccess() {
  loadArticles()
  categoryTreeRef.value?.loadTree()
}

onMounted(() => {
  loadArticles()
})
</script>

<template>
  <div class="knowledge-layout">
    <el-button class="mobile-category-btn" @click="showMobileTree = true">
      选择分类
    </el-button>

    <div class="category-panel" :class="{ 'mobile-show': showMobileTree }">
      <CategoryTree ref="categoryTreeRef" @select="handleCategorySelect" />
      <el-button
        v-if="showMobileTree"
        text
        class="mobile-close-btn"
        @click="showMobileTree = false"
      >
        关闭
      </el-button>
    </div>

    <div class="article-panel">
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索标题、内容、标签..."
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
        <el-button type="primary" @click="handleCreate">新建知识</el-button>
      </div>

      <el-table :data="articleList" stripe class="article-table">
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="120">
          <template #default="{ row }">
            {{ row.categoryId }}
          </template>
        </el-table-column>
        <el-table-column prop="tags" label="标签" width="150">
          <template #default="{ row }">
            <el-tag v-for="tag in row.tags" :key="tag" size="small" class="tag-item">
              {{ tag }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ row.createTime }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next"
        @size-change="loadArticles"
        @current-change="loadArticles"
        class="pagination"
      />
    </div>

    <ArticleEditor
      v-model="editorVisible"
      :article-id="currentArticleId"
      :category-id="selectedCategory?.id"
      @success="handleEditorSuccess"
    />
  </div>
</template>

<style scoped>
.knowledge-layout {
  display: flex;
  height: 100%;
  gap: 16px;
  padding: 16px;
  background: #fff;
}

.mobile-category-btn {
  display: none;
}

.category-panel {
  width: 240px;
  flex-shrink: 0;
  border-right: 1px solid #eee;
  padding-right: 16px;
}

.mobile-close-btn {
  width: 100%;
  margin-top: 8px;
}

.article-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.search-bar .el-input {
  flex: 1;
}

.article-table {
  flex: 1;
}

.tag-item {
  margin-right: 4px;
  margin-bottom: 2px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .knowledge-layout {
    flex-direction: column;
    padding: 12px;
  }

  .mobile-category-btn {
    display: flex;
    margin-bottom: 12px;
  }

  .category-panel {
    position: fixed;
    left: -100%;
    top: 0;
    bottom: 0;
    width: 280px;
    background: #fff;
    z-index: 1000;
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
    transition: left 0.3s;
    border-right: none;
    padding: 16px;
  }

  .category-panel.mobile-show {
    left: 0;
  }

  .article-panel {
    width: 100%;
  }
}
</style>
