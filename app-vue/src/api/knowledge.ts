/**
 * 知识库 API 模块
 *
 * 模块说明：
 * 提供知识库文章的 CRUD、分类管理、标签管理、文件管理等功能接口
 *
 * API 端点前缀：/api/knowledge
 *
 * @module api/knowledge
 */
import request from './request'
import type {
  ApiResponse,
  KnowledgeCategory,
  KnowledgeArticle,
  KnowledgeTag,
  ArticleFile,
  PageResult,
  ArticleQueryParams,
  CreateCategoryParams,
  UpdateCategoryParams,
  CreateArticleParams,
  UpdateArticleParams
} from './types'

/**
 * 获取分类树形结构
 * @returns 分类树列表
 */
export function getCategoryTree() {
  return request.get<ApiResponse<KnowledgeCategory[]>>('/knowledge/categories/tree')
}

/**
 * 创建分类
 * @param data - 分类参数（包含分类名称等信息）
 * @returns 创建结果
 */
export function createCategory(data: CreateCategoryParams) {
  return request.post<ApiResponse<void>>('/knowledge/categories', data)
}

/**
 * 更新分类
 * @param id - 分类 ID
 * @param data - 分类参数
 * @returns 更新结果
 */
export function updateCategory(id: number, data: UpdateCategoryParams) {
  return request.put<ApiResponse<void>>(`/knowledge/categories/${id}`, data)
}

/**
 * 删除分类
 * @param id - 分类 ID
 * @returns 删除结果
 */
export function deleteCategory(id: number) {
  return request.delete<ApiResponse<void>>(`/knowledge/categories/${id}`)
}

/**
 * 分页获取文章列表
 * @param params - 查询参数（支持关键字、分类 ID、分页等条件）
 * @returns 分页后的文章列表
 */
export function getArticlePage(params: ArticleQueryParams) {
  return request.get<ApiResponse<PageResult<KnowledgeArticle>>>('/knowledge/articles', { params })
}

/**
 * 获取文章详情
 * @param id - 文章 ID
 * @returns 文章详情（包含阅读数 +1）
 */
export function getArticleDetail(id: number) {
  return request.get<ApiResponse<KnowledgeArticle>>(`/knowledge/articles/${id}`)
}

/**
 * 创建文章
 * @param data - 文章参数（包含标题、内容、标签等）
 * @returns 创建结果
 */
export function createArticle(data: CreateArticleParams) {
  return request.post<ApiResponse<void>>('/knowledge/articles', data)
}

/**
 * 更新文章
 * @param id - 文章 ID
 * @param data - 文章参数
 * @returns 更新结果
 */
export function updateArticle(id: number, data: UpdateArticleParams) {
  return request.put<ApiResponse<void>>(`/knowledge/articles/${id}`, data)
}

/**
 * 删除文章
 * @param id - 文章 ID
 * @returns 删除结果
 */
export function deleteArticle(id: number) {
  return request.delete<ApiResponse<void>>(`/knowledge/articles/${id}`)
}

/**
 * 获取所有标签
 * @returns 标签列表
 */
export function getAllTags() {
  return request.get<ApiResponse<KnowledgeTag[]>>('/knowledge/tags')
}

/**
 * 获取文章关联的标签
 * @param articleId - 文章 ID
 * @returns 标签列表
 */
export function getArticleTags(articleId: number) {
  return request.get<ApiResponse<KnowledgeTag[]>>(`/knowledge/articles/${articleId}/tags`)
}

/**
 * 创建标签
 * @param data - 标签参数（包含标签名称）
 * @returns 创建结果
 */
export function createTag(data: { name: string }) {
  return request.post<ApiResponse<void>>('/knowledge/tags', data)
}

/**
 * 删除标签
 * @param id - 标签 ID
 * @returns 删除结果
 */
export function deleteTag(id: number) {
  return request.delete<ApiResponse<void>>(`/knowledge/tags/${id}`)
}

/**
 * 上传文件到文章
 * @param articleId - 关联的文章 ID
 * @param fileName - 文件名
 * @param file - 文件数据
 * @returns 上传结果
 */
export function uploadFile(articleId: number, fileName: string, file: File) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('articleId', String(articleId))
  formData.append('fileName', fileName)
  return request.post<ApiResponse<void>>('/knowledge/files', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 下载文件
 * @param fileId - 文件 ID
 * @returns 文件二进制数据
 */
export function downloadFile(fileId: number) {
  return request.get<Blob>(`/knowledge/files/${fileId}`, {
    responseType: 'blob'
  })
}

/**
 * 删除文件
 * @param fileId - 文件 ID
 * @returns 删除结果
 */
export function deleteFile(fileId: number) {
  return request.delete<ApiResponse<void>>(`/knowledge/files/${fileId}`)
}

/**
 * 获取文章关联的文件列表
 * @param articleId - 文章 ID
 * @returns 文件列表
 */
export function getArticleFiles(articleId: number) {
  return request.get<ApiResponse<ArticleFile[]>>(`/knowledge/articles/${articleId}/files`)
}

/**
 * 搜索文章
 * @param keyword - 搜索关键字
 * @returns 匹配的文章列表
 */
export function searchArticles(keyword: string) {
  return request.get<ApiResponse<KnowledgeArticle[]>>('/knowledge/search', {
    params: { keyword }
  })
}
