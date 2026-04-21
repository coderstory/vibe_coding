package cn.coderstory.springboot.controller;

import cn.coderstory.springboot.entity.KnowledgeArticle;
import cn.coderstory.springboot.entity.KnowledgeCategory;
import cn.coderstory.springboot.entity.KnowledgeFile;
import cn.coderstory.springboot.entity.KnowledgeTag;
import cn.coderstory.springboot.service.KnowledgeService;
import cn.coderstory.springboot.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库管理控制器
 *
 * 功能描述：
 * - 提供知识库文章的 CRUD 操作接口
 * - 提供分类管理和标签管理
 * - 提供文件上传下载功能
 * - 支持文章搜索和全文检索
 *
 * 接口列表：
 * - 文章管理：列表、详情、创建、更新、删除、搜索
 * - 分类管理：树形结构、创建、更新、删除
 * - 标签管理：列表、创建、删除
 * - 文件管理：上传、下载、删除
 *
 * @author system
 * @version 1.0
 * @since 2026-04-20
 */
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    /**
     * 获取分类树形结构
     *
     * @return 分类树列表
     */
    @GetMapping("/categories/tree")
    public ResponseEntity<ApiResponse<List<KnowledgeCategory>>> getCategoryTree() {
        List<KnowledgeCategory> tree = knowledgeService.getCategoryTree();
        return ResponseEntity.ok(ApiResponse.success(tree));
    }

    /**
     * 创建分类
     *
     * @param category 分类信息
     * @return 创建的分类
     */
    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<KnowledgeCategory>> createCategory(@RequestBody KnowledgeCategory category) {
        KnowledgeCategory created = knowledgeService.createCategory(category);
        return ResponseEntity.ok(ApiResponse.success(created));
    }

    /**
     * 更新分类
     *
     * @param id 分类 ID
     * @param category 分类信息
     * @return 更新后的分类
     */
    @PutMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<KnowledgeCategory>> updateCategory(@PathVariable Long id, @RequestBody KnowledgeCategory category) {
        KnowledgeCategory updated = knowledgeService.updateCategory(id, category);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * 删除分类
     *
     * @param id 分类 ID
     * @return 删除结果
     */
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        boolean success = knowledgeService.deleteCategory(id);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success());
        } else {
            return ResponseEntity.ok(ApiResponse.error("删除失败"));
        }
    }

    /**
     * 分页获取文章列表
     *
     * @param keyword 搜索关键字（可选）
     * @param categoryId 分类 ID（可选）
     * @param page 页码，默认 1
     * @param size 每页数量，默认 20
     * @return 分页后的文章列表
     */
    @GetMapping("/articles")
    public ResponseEntity<ApiResponse<Object>> getArticlePage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Object data = knowledgeService.getArticlePage(keyword, categoryId, page, size);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 获取文章详情
     *
     * @param id 文章 ID
     * @return 文章详情（包含阅读数 +1）
     */
    @GetMapping("/articles/{id}")
    public ResponseEntity<ApiResponse<KnowledgeArticle>> getArticleById(@PathVariable Long id) {
        knowledgeService.incrementViewCount(id);
        KnowledgeArticle article = knowledgeService.getArticleById(id);
        return ResponseEntity.ok(ApiResponse.success(article));
    }

    /**
     * 创建文章
     *
     * @param article 文章信息（包含标签列表）
     * @return 创建的文章
     */
    @PostMapping("/articles")
    public ResponseEntity<ApiResponse<KnowledgeArticle>> createArticle(@RequestBody KnowledgeArticle article) {
        List<Long> tagIds = article.getTags() != null ? article.getTags() : null;
        KnowledgeArticle created = knowledgeService.createArticle(article, tagIds);
        return ResponseEntity.ok(ApiResponse.success(created));
    }

    /**
     * 更新文章
     *
     * @param id 文章 ID
     * @param article 文章信息
     * @return 更新后的文章
     */
    @PutMapping("/articles/{id}")
    public ResponseEntity<ApiResponse<KnowledgeArticle>> updateArticle(@PathVariable Long id, @RequestBody KnowledgeArticle article) {
        List<Long> tagIds = article.getTags() != null ? article.getTags() : null;
        KnowledgeArticle updated = knowledgeService.updateArticle(id, article, tagIds);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * 删除文章
     *
     * @param id 文章 ID
     * @return 删除结果
     */
    @DeleteMapping("/articles/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteArticle(@PathVariable Long id) {
        boolean success = knowledgeService.deleteArticle(id);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success());
        } else {
            return ResponseEntity.ok(ApiResponse.error("删除失败"));
        }
    }

    /**
     * 获取所有标签
     *
     * @return 标签列表
     */
    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<List<KnowledgeTag>>> getAllTags() {
        List<KnowledgeTag> tags = knowledgeService.getAllTags();
        return ResponseEntity.ok(ApiResponse.success(tags));
    }

    /**
     * 创建标签
     *
     * @param tag 标签信息
     * @return 创建的标签
     */
    @PostMapping("/tags")
    public ResponseEntity<ApiResponse<KnowledgeTag>> createTag(@RequestBody KnowledgeTag tag) {
        KnowledgeTag created = knowledgeService.createTag(tag);
        return ResponseEntity.ok(ApiResponse.success(created));
    }

    /**
     * 删除标签
     *
     * @param id 标签 ID
     * @return 删除结果
     */
    @DeleteMapping("/tags/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTag(@PathVariable Long id) {
        boolean success = knowledgeService.deleteTag(id);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success());
        } else {
            return ResponseEntity.ok(ApiResponse.error("删除失败"));
        }
    }

    /**
     * 获取文章关联的标签
     *
     * @param id 文章 ID
     * @return 标签列表
     */
    @GetMapping("/articles/{id}/tags")
    public ResponseEntity<ApiResponse<List<KnowledgeTag>>> getArticleTags(@PathVariable Long id) {
        List<KnowledgeTag> tags = knowledgeService.getTagsByArticleId(id);
        return ResponseEntity.ok(ApiResponse.success(tags));
    }

    /**
     * 上传文件到文章
     *
     * @param articleId 关联的文章 ID
     * @param fileName 文件名
     * @param file 文件数据
     * @return 上传的文件元信息
     */
    @PostMapping("/files")
    public ResponseEntity<ApiResponse<KnowledgeFile>> uploadFile(
            @RequestParam Long articleId,
            @RequestParam String fileName,
            @RequestParam MultipartFile file) throws Exception {
        byte[] data = file.getBytes();
        String contentType = file.getContentType();
        KnowledgeFile uploaded = knowledgeService.uploadFile(articleId, fileName, data, contentType);
        return ResponseEntity.ok(ApiResponse.success(uploaded));
    }

    /**
     * 下载文件
     *
     * @param id 文件 ID
     * @return 文件二进制数据
     */
    @GetMapping("/files/{id}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        KnowledgeFile file = knowledgeService.getFileMetadata(id);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }
        byte[] data = knowledgeService.downloadFile(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(file.getContentType() != null ? file.getContentType() : "application/octet-stream"))
                .body(data);
    }

    /**
     * 删除文件
     *
     * @param id 文件 ID
     * @return 删除结果
     */
    @DeleteMapping("/files/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable Long id) {
        boolean success = knowledgeService.deleteFile(id);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success());
        } else {
            return ResponseEntity.ok(ApiResponse.error("删除失败"));
        }
    }

    /**
     * 获取文章关联的文件列表
     *
     * @param id 文章 ID
     * @return 文件列表
     */
    @GetMapping("/articles/{id}/files")
    public ResponseEntity<ApiResponse<List<KnowledgeFile>>> getArticleFiles(@PathVariable Long id) {
        List<KnowledgeFile> files = knowledgeService.getFilesByArticleId(id);
        return ResponseEntity.ok(ApiResponse.success(files));
    }

    /**
     * 搜索文章
     *
     * @param keyword 搜索关键字
     * @return 匹配的文章列表
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<KnowledgeArticle>>> search(@RequestParam String keyword) {
        List<KnowledgeArticle> articles = knowledgeService.searchArticles(keyword);
        return ResponseEntity.ok(ApiResponse.success(articles));
    }
}
