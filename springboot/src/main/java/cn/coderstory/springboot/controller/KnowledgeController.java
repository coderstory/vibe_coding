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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @GetMapping("/categories/tree")
    public ResponseEntity<ApiResponse<List<KnowledgeCategory>>> getCategoryTree() {
        List<KnowledgeCategory> tree = knowledgeService.getCategoryTree();
        return ResponseEntity.ok(ApiResponse.success(tree));
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<KnowledgeCategory>> createCategory(@RequestBody KnowledgeCategory category) {
        KnowledgeCategory created = knowledgeService.createCategory(category);
        return ResponseEntity.ok(ApiResponse.success(created));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<KnowledgeCategory>> updateCategory(@PathVariable Long id, @RequestBody KnowledgeCategory category) {
        KnowledgeCategory updated = knowledgeService.updateCategory(id, category);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        boolean success = knowledgeService.deleteCategory(id);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success());
        } else {
            return ResponseEntity.ok(ApiResponse.error("删除失败"));
        }
    }

    @GetMapping("/articles")
    public ResponseEntity<ApiResponse<Object>> getArticlePage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Object data = knowledgeService.getArticlePage(keyword, categoryId, page, size);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/articles/{id}")
    public ResponseEntity<ApiResponse<KnowledgeArticle>> getArticleById(@PathVariable Long id) {
        knowledgeService.incrementViewCount(id);
        KnowledgeArticle article = knowledgeService.getArticleById(id);
        return ResponseEntity.ok(ApiResponse.success(article));
    }

    @PostMapping("/articles")
    public ResponseEntity<ApiResponse<KnowledgeArticle>> createArticle(@RequestBody KnowledgeArticle article) {
        List<Long> tagIds = article.getTags() != null ? article.getTags() : null;
        KnowledgeArticle created = knowledgeService.createArticle(article, tagIds);
        return ResponseEntity.ok(ApiResponse.success(created));
    }

    @PutMapping("/articles/{id}")
    public ResponseEntity<ApiResponse<KnowledgeArticle>> updateArticle(@PathVariable Long id, @RequestBody KnowledgeArticle article) {
        List<Long> tagIds = article.getTags() != null ? article.getTags() : null;
        KnowledgeArticle updated = knowledgeService.updateArticle(id, article, tagIds);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/articles/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteArticle(@PathVariable Long id) {
        boolean success = knowledgeService.deleteArticle(id);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success());
        } else {
            return ResponseEntity.ok(ApiResponse.error("删除失败"));
        }
    }

    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<List<KnowledgeTag>>> getAllTags() {
        List<KnowledgeTag> tags = knowledgeService.getAllTags();
        return ResponseEntity.ok(ApiResponse.success(tags));
    }

    @PostMapping("/tags")
    public ResponseEntity<ApiResponse<KnowledgeTag>> createTag(@RequestBody KnowledgeTag tag) {
        KnowledgeTag created = knowledgeService.createTag(tag);
        return ResponseEntity.ok(ApiResponse.success(created));
    }

    @DeleteMapping("/tags/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTag(@PathVariable Long id) {
        boolean success = knowledgeService.deleteTag(id);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success());
        } else {
            return ResponseEntity.ok(ApiResponse.error("删除失败"));
        }
    }

    @GetMapping("/articles/{id}/tags")
    public ResponseEntity<ApiResponse<List<KnowledgeTag>>> getArticleTags(@PathVariable Long id) {
        List<KnowledgeTag> tags = knowledgeService.getTagsByArticleId(id);
        return ResponseEntity.ok(ApiResponse.success(tags));
    }

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

    @DeleteMapping("/files/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable Long id) {
        boolean success = knowledgeService.deleteFile(id);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success());
        } else {
            return ResponseEntity.ok(ApiResponse.error("删除失败"));
        }
    }

    @GetMapping("/articles/{id}/files")
    public ResponseEntity<ApiResponse<List<KnowledgeFile>>> getArticleFiles(@PathVariable Long id) {
        List<KnowledgeFile> files = knowledgeService.getFilesByArticleId(id);
        return ResponseEntity.ok(ApiResponse.success(files));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<KnowledgeArticle>>> search(@RequestParam String keyword) {
        List<KnowledgeArticle> articles = knowledgeService.searchArticles(keyword);
        return ResponseEntity.ok(ApiResponse.success(articles));
    }
}
