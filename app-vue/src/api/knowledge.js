import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res
    } else {
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  error => {
    return Promise.reject(error)
  }
)

export function getCategoryTree() {
  return request({
    url: '/knowledge/categories/tree',
    method: 'get'
  })
}

export function createCategory(data) {
  return request({
    url: '/knowledge/categories',
    method: 'post',
    data
  })
}

export function updateCategory(id, data) {
  return request({
    url: `/knowledge/categories/${id}`,
    method: 'put',
    data
  })
}

export function deleteCategory(id) {
  return request({
    url: `/knowledge/categories/${id}`,
    method: 'delete'
  })
}

export function getArticlePage(params) {
  return request({
    url: '/knowledge/articles',
    method: 'get',
    params
  })
}

export function getArticleDetail(id) {
  return request({
    url: `/knowledge/articles/${id}`,
    method: 'get'
  })
}

export function createArticle(data) {
  return request({
    url: '/knowledge/articles',
    method: 'post',
    data
  })
}

export function updateArticle(id, data) {
  return request({
    url: `/knowledge/articles/${id}`,
    method: 'put',
    data
  })
}

export function deleteArticle(id) {
  return request({
    url: `/knowledge/articles/${id}`,
    method: 'delete'
  })
}

export function getAllTags() {
  return request({
    url: '/knowledge/tags',
    method: 'get'
  })
}

export function getArticleTags(articleId) {
  return request({
    url: `/knowledge/articles/${articleId}/tags`,
    method: 'get'
  })
}

export function createTag(data) {
  return request({
    url: '/knowledge/tags',
    method: 'post',
    data
  })
}

export function deleteTag(id) {
  return request({
    url: `/knowledge/tags/${id}`,
    method: 'delete'
  })
}

export function uploadFile(articleId, fileName, file) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('articleId', articleId)
  formData.append('fileName', fileName)
  return request({
    url: '/knowledge/files',
    method: 'post',
    data: formData
  })
}

export function downloadFile(fileId) {
  return request({
    url: `/knowledge/files/${fileId}`,
    method: 'get',
    responseType: 'blob'
  })
}

export function deleteFile(fileId) {
  return request({
    url: `/knowledge/files/${fileId}`,
    method: 'delete'
  })
}

export function getArticleFiles(articleId) {
  return request({
    url: `/knowledge/articles/${articleId}/files`,
    method: 'get'
  })
}

export function searchArticles(keyword) {
  return request({
    url: '/knowledge/search',
    method: 'get',
    params: { keyword }
  })
}

export default request
