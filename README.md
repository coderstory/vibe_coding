# 后台管理系统

<p align="center">
  <img src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=modern%20admin%20dashboard%20interface%20with%20blue%20and%20white%20color%20scheme&image_size=square_hd" alt="后台管理系统" width="200">
</p>

<p align="center">
  <a href="#">
    <img src="https://img.shields.io/badge/version-1.0.0-blue.svg" alt="版本">
  </a>
  <a href="#">
    <img src="https://img.shields.io/badge/build-passing-green.svg" alt="构建状态">
  </a>
  <a href="#">
    <img src="https://img.shields.io/badge/license-MIT-blue.svg" alt="许可证">
  </a>
</p>

## 📋 项目简介

这是一个基于 Spring Boot 4 和 Vue 3 的现代化后台管理系统，使用 AI 编程工具完成开发。

- **前端**：Vue 3.5 + Vite 8 + TypeScript
- **后端**：Spring Boot 4.0.5 + Java 21 + MyBatis Plus
- **数据库**：MySQL + Flyway 数据库迁移
- **认证**：JWT 令牌认证
- **UI 组件**：Element Plus
- **富文本编辑器**：WangEditor

## 🚀 快速开始

### 环境要求

- **前端**：Node.js v20.19+ 或 v22.12+
- **后端**：Java 21+
- **数据库**：MySQL（数据库名 `admin_system`，本地默认 root/123456）

### 安装与运行

#### 前端

```powershell
# 进入前端目录
cd app-vue

# 安装依赖
npm install

# 开发模式（热重载，端口 5173）
npm run dev

# 生产构建
npm run build

# 预览生产构建
npm run preview
```

#### 后端

```powershell
# 进入后端目录
cd springboot

# 运行应用
./mvnw.cmd spring-boot:run

# 编译打包
./mvnw.cmd package
```

## 📁 项目结构

```
vibe coding/
├── .agents/            # 配置目录（包含agent、skill、rules等）
├── .idea/              # IDEA配置目录
├── .planning/          # GSD日志文件
├── .playwright/        # playwright插件目录
├── app-vue/            # 前端项目源码
│   ├── src/            # 源代码
│   │   ├── api/        # API服务层
│   │   ├── components/ # 公共组件
│   │   ├── views/      # 页面视图
│   │   ├── router/     # 路由配置
│   │   └── store/      # 状态管理
│   └── vite.config.js  # Vite配置
├── docs/               # 项目文档
├── springboot/         # 后端源码目录
│   ├── src/            # 源代码
│   │   ├── main/java/  # Java代码
│   │   └── resources/  # 资源文件
│   └── pom.xml         # Maven配置
├── AGENTS.md           # AI开发说明文档
└── README.md           # 项目说明文档
```

## ✨ 核心功能

### 系统管理
- **用户管理**：用户CRUD、角色分配
- **角色管理**：角色CRUD、权限配置
- **菜单管理**：菜单配置、权限控制

### 知识库管理
- **文章管理**：富文本编辑、标签管理
- **分类管理**：树形分类结构
- **标签管理**：标签创建与管理
- **文件上传**：支持图片和附件上传

### 审计日志
- **操作日志**：自动记录用户操作
- **登录日志**：记录用户登录信息

## 🔧 开发流程

本项目使用基于 GSD（Get Shit Done）的开发流程：

1. **提出问题**：初步分析 `/gsd:new-project`
2. **拆解问题**：讨论方案，产出需求 `/gsd:discuss-phase N`
3. **详细设计**：针对单个需求进行设计 `/gsd:plan-phase N`
4. **执行编码**：实现功能 `/gsd:execute-phase N`
5. **功能测试**：验证功能正确性 `/gsd:verify-work N`
6. **完成里程碑**：结束本轮开发 `/gsd:complete-milestone`
7. **开始新里程**：启动新一轮开发 `/gsd:new-milestone`

## 🛠 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| **前端框架** | Vue | 3.5+ |
| **构建工具** | Vite | 8+ |
| **类型系统** | TypeScript | - |
| **UI库** | Element Plus | - |
| **状态管理** | Pinia | - |
| **路由** | Vue Router | - |
| **HTTP客户端** | Axios | - |
| **后端框架** | Spring Boot | 4.0.5 |
| **语言** | Java | 21 |
| **ORM** | MyBatis Plus | - |
| **数据库** | MySQL | - |
| **数据库迁移** | Flyway | - |
| **认证** | JWT | - |

## 📚 文档

- **开发文档**：`AGENTS.md` - AI开发说明文档
- **项目文档**：`docs/` 目录下的相关文档

## 🔍 问题分析

对于复杂场景的分析，使用 `/brainstorming` 进行头脑风暴，帮助理清思路和解决方案。

## 🌍 国际化

- 项目支持中文，相关规则已在 `AGENTS.md` 中定义
- 部分技能为英文，可能导致相关输出为英文

## ⚠️ 已知问题

1. **日志输出**：opencode无法接管日志输出，看不到异常，需要手动处理
2. **浏览器能力**：缺少浏览器访问技能

## 📦 技能依赖

1. **GSD**：安装到 `.opencode` 目录 `npx gsd-opencode@latest`
2. **Superpower**：汉化版本 `npx superpowers-zh`
3. **浏览器访问**：`npm install -g @playwright/cli@latest` `playwright-cli install --skills`

## 🔧 配置说明

- **npm镜像**：`npm config set registry https://registry.npmmirror.com`
- **数据库配置**：在 `springboot/src/main/resources/application.yaml` 中配置
- **直接访问数据库**：配置在 `opencode.json` 中

## 🤝 贡献

欢迎提交 Issue 和 Pull Request 来改进这个项目！

## 📄 许可证

本项目采用 MIT 许可证 - 详情请参阅 [LICENSE](LICENSE) 文件

## 🌟 鸣谢

- [Vue.js](https://vuejs.org/)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Element Plus](https://element-plus.org/)
- [MyBatis Plus](https://baomidou.com/)
- [GSD](https://github.com/gsd-2/get-shit-done)
- [Superpower](https://github.com/obra/superpowers)

---

<p align="center">
  Made with ❤️ by AI + Human Collaboration
</p>