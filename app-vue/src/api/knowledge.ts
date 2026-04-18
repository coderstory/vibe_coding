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

export function getCategoryTree() {
  return request.get<ApiResponse<KnowledgeCategory[]>>('/knowledge/categories/tree')
}

export function createCategory(data: CreateCategoryParams) {
  return request.post<ApiResponse<void>>('/knowledge/categories', data)
}

export function updateCategory(id: number, data: UpdateCategoryParams) {
  return request.put<ApiResponse<void>>(`/knowledge/categories/${id}`, data)
}

export function deleteCategory(id: number) {
  return request.delete<ApiResponse<void>>(`/knowledge/categories/${id}`)
}

export function getArticlePage(params: ArticleQueryParams) {
  return request.get<ApiResponse<PageResult<KnowledgeArticle>>>('/knowledge/articles', { params })
}

export function getArticleDetail(id: number) {
  return request.get<ApiResponse<KnowledgeArticle>>(`/knowledge/articles/${id}`)
}

export function createArticle(data: CreateArticleParams) {
  return request.post<ApiResponse<void>>('/knowledge/articles', data)
}

export function updateArticle(id: number, data: UpdateArticleParams) {
  return request.put<ApiResponse<void>>(`/knowledge/articles/${id}`, data)
}

export function deleteArticle(id: number) {
  return request.delete<ApiResponse<void>>(`/knowledge/articles/${id}`)
}

export function getAllTags() {
  return request.get<ApiResponse<KnowledgeTag[]>>('/knowledge/tags')
}

export function getArticleTags(articleId: number) {
  return request.get<ApiResponse<KnowledgeTag[]>>(`/knowledge/articles/${articleId}/tags`)
}

export function createTag(data: { name: string }) {
  return request.post<ApiResponse<void>>('/knowledge/tags', data)
}

export function deleteTag(id: number) {
  return request.delete<ApiResponse<void>>(`/knowledge/tags/${id}`)
}

export function uploadFile(articleId: number, fileName: string, file: File) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('articleId', String(articleId))
  formData.append('fileName', fileName)
  return request.post<ApiResponse<void>>('/knowledge/files', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function downloadFile(fileId: number) {
  return request.get<Blob>(`/knowledge/files/${fileId}`, {
    responseType: 'blob'
  })
}

export function deleteFile(fileId: number) {
  return request.delete<ApiResponse<void>>(`/knowledge/files/${fileId}`)
}

export function getArticleFiles(articleId: number) {
  return request.get<ApiResponse<ArticleFile[]>>(`/knowledge/articles/${articleId}/files`)
}

export function searchArticles(keyword: string) {
  return request.get<ApiResponse<KnowledgeArticle[]>>('/knowledge/search', {
    params: { keyword }
  })
}
