/**
 * API 统一响应类型
 */
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}

/**
 * 分页响应类型
 */
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/**
 * 分页请求参数
 */
export interface PageParams {
  page?: number
  size?: number
}

/**
 * 用户信息
 */
export interface User {
  id: number
  username: string
  name: string
  nickname?: string
  email?: string
  phone?: string
  avatar?: string
  gender?: number
  department?: string
  position?: string
  roleId?: number
  roleName?: string
  enabled?: number
  createTime?: string
  updateTime?: string
}

/**
 * 用户详情（含角色名）
 */
export interface UserVO extends User {
  roleName: string
}

/**
 * 登录请求
 */
export interface LoginParams {
  username: string
  password: string
}

/**
 * 登录响应
 */
export interface LoginResult {
  token: string
  refreshToken: string
  expiresIn: number
  user: {
    id: number
    username: string
    name: string
    roleId: number
  }
}

/**
 * 刷新令牌响应
 */
export interface RefreshTokenResult {
  token: string
  refreshToken: string
  expiresIn: number
}

/**
 * 用户查询参数
 */
export interface UserQueryParams extends PageParams {
  username?: string
  name?: string
  department?: string
  enabled?: number
  phone?: string
}

/**
 * 创建用户参数
 */
export interface CreateUserParams {
  username: string
  name: string
  password?: string
  gender?: number
  email?: string
  department?: string
  position?: string
  roleId?: number
  enabled?: number
  avatar?: string
  phone?: string
}

/**
 * 更新用户参数
 */
export interface UpdateUserParams {
  name?: string
  gender?: number
  email?: string
  department?: string
  position?: string
  roleId?: number
  enabled?: number
  avatar?: string
  phone?: string
}

/**
 * 角色信息
 */
export interface Role {
  id: number
  roleName: string
  roleCode?: string
  description?: string
  enabled?: number
  createTime?: string
  updateTime?: string
}

/**
 * 菜单信息
 */
export interface Menu {
  id: number
  name: string
  path: string
  icon?: string
  parentId?: number
  sortOrder?: number
  description?: string
  createTime?: string
  updateTime?: string
  children?: Menu[]
}

/**
 * 菜单树
 */
export interface MenuTree extends Menu {
  children: MenuTree[]
}

/**
 * 审计日志
 */
export interface AuditLog {
  id: number
  userId: number
  username: string
  action: string
  method: string
  url: string
  ip: string
  createTime: string
}

/**
 * 知识库条目
 */
export interface Knowledge {
  id: number
  title: string
  content: string
  category?: string
  tags?: string[]
  createTime: string
  updateTime: string
}

/**
 * 角色查询参数
 */
export interface RoleQueryParams extends PageParams {
  roleName?: string
  enabled?: number
}

/**
 * 创建角色参数
 */
export interface CreateRoleParams {
  roleName: string
  roleCode?: string
  description?: string
  enabled?: number
}

/**
 * 更新角色参数
 */
export interface UpdateRoleParams {
  roleName?: string
  roleCode?: string
  description?: string
  enabled?: number
}

/**
 * 审计日志查询参数
 */
export interface AuditLogQueryParams extends PageParams {
  username?: string
  action?: string
  startDate?: string
  endDate?: string
}

/**
 * 知识库分类
 */
export interface KnowledgeCategory {
  id: number
  name: string
  parentId?: number
  sortOrder?: number
  children?: KnowledgeCategory[]
}

/**
 * 创建分类参数
 */
export interface CreateCategoryParams {
  name: string
  parentId?: number
  sortOrder?: number
}

/**
 * 更新分类参数
 */
export interface UpdateCategoryParams {
  name?: string
  parentId?: number
  sortOrder?: number
}

/**
 * 知识库文章
 */
export interface KnowledgeArticle {
  id: number
  title: string
  content: string
  categoryId?: number
  categoryName?: string
  tags?: number[]
  createTime: string
  updateTime: string
}

/**
 * 文章查询参数
 */
export interface ArticleQueryParams extends PageParams {
  title?: string
  categoryId?: number
  tag?: string
}

/**
 * 创建文章参数
 */
export interface CreateArticleParams {
  title: string
  content: string
  categoryId?: number
  tags?: number[]
}

/**
 * 更新文章参数
 */
export interface UpdateArticleParams {
  title?: string
  content?: string
  categoryId?: number
  tags?: number[]
}

/**
 * 知识库标签
 */
export interface KnowledgeTag {
  id: number
  name: string
  articleCount?: number
}

/**
 * 文章文件
 */
export interface ArticleFile {
  id: number
  articleId: number
  fileName: string
  fileSize: number
  downloadUrl: string
  createTime: string
}
