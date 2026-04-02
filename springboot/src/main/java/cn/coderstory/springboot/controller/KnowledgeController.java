package cn.coderstory.springboot.controller;

import cn.coderstory.springboot.entity.KnowledgeArticle;
import cn.coderstory.springboot.entity.KnowledgeCategory;
import cn.coderstory.springboot.entity.KnowledgeFile;
import cn.coderstory.springboot.entity.KnowledgeTag;
import cn.coderstory.springboot.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {
    
    private final KnowledgeService knowledgeService;
    
    @GetMapping("/categories/tree")
    public ResponseEntity<Map<String, Object>> getCategoryTree() {
        List<KnowledgeCategory> tree = knowledgeService.getCategoryTree();
        return ok(tree);
    }
    
    @PostMapping("/categories")
    public ResponseEntity<Map<String, Object>> createCategory(@RequestBody KnowledgeCategory category) {
        KnowledgeCategory created = knowledgeService.createCategory(category);
        return ok(created);
    }
    
    @PutMapping("/categories/{id}")
    public ResponseEntity<Map<String, Object>> updateCategory(@PathVariable Long id, @RequestBody KnowledgeCategory category) {
        KnowledgeCategory updated = knowledgeService.updateCategory(id, category);
        return ok(updated);
    }
    
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable Long id) {
        boolean success = knowledgeService.deleteCategory(id);
        return success ? ok(null) : fail("删除失败");
    }
    
    @GetMapping("/articles")
    public ResponseEntity<Map<String, Object>> getArticlePage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Map<String, Object> data = knowledgeService.getArticlePage(keyword, categoryId, page, size);
        return ok(data);
    }
    
    @GetMapping("/articles/{id}")
    public ResponseEntity<Map<String, Object>> getArticleById(@PathVariable Long id) {
        knowledgeService.incrementViewCount(id);
        KnowledgeArticle article = knowledgeService.getArticleById(id);
        return ok(article);
    }
    
    @PostMapping("/articles")
    public ResponseEntity<Map<String, Object>> createArticle(@RequestBody Map<String, Object> request) {
        KnowledgeArticle article = new KnowledgeArticle();
        article.setTitle((String) request.get("title"));
        article.setCategoryId(((Number) request.get("categoryId")).longValue());
        article.setContent((String) request.get("content"));
        article.setStatus((Integer) request.getOrDefault("status", 1));
        
        List<Integer> tagIds = (List<Integer>) request.get("tagIds");
        KnowledgeArticle created = knowledgeService.createArticle(article, tagIds);
        return ok(created);
    }
    
    @PutMapping("/articles/{id}")
    public ResponseEntity<Map<String, Object>> updateArticle(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        KnowledgeArticle article = new KnowledgeArticle();
        article.setTitle((String) request.get("title"));
        article.setContent((String) request.get("content"));
        article.setStatus((Integer) request.getOrDefault("status", 1));
        
        List<Integer> tagIds = (List<Integer>) request.get("tagIds");
        KnowledgeArticle updated = knowledgeService.updateArticle(id, article, tagIds);
        return ok(updated);
    }
    
    @DeleteMapping("/articles/{id}")
    public ResponseEntity<Map<String, Object>> deleteArticle(@PathVariable Long id) {
        boolean success = knowledgeService.deleteArticle(id);
        return success ? ok(null) : fail("删除失败");
    }
    
    @GetMapping("/tags")
    public ResponseEntity<Map<String, Object>> getAllTags() {
        List<KnowledgeTag> tags = knowledgeService.getAllTags();
        return ok(tags);
    }
    
    @PostMapping("/tags")
    public ResponseEntity<Map<String, Object>> createTag(@RequestBody KnowledgeTag tag) {
        KnowledgeTag created = knowledgeService.createTag(tag);
        return ok(created);
    }
    
    @DeleteMapping("/tags/{id}")
    public ResponseEntity<Map<String, Object>> deleteTag(@PathVariable Long id) {
        boolean success = knowledgeService.deleteTag(id);
        return success ? ok(null) : fail("删除失败");
    }
    
    @GetMapping("/articles/{id}/tags")
    public ResponseEntity<Map<String, Object>> getArticleTags(@PathVariable Long id) {
        List<KnowledgeTag> tags = knowledgeService.getTagsByArticleId(id);
        return ok(tags);
    }
    
    @PostMapping("/files")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam Long articleId,
            @RequestParam String fileName,
            @RequestParam MultipartFile file) throws Exception {
        byte[] data = file.getBytes();
        String contentType = file.getContentType();
        KnowledgeFile uploaded = knowledgeService.uploadFile(articleId, fileName, data, contentType);
        return ok(uploaded);
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
    public ResponseEntity<Map<String, Object>> deleteFile(@PathVariable Long id) {
        boolean success = knowledgeService.deleteFile(id);
        return success ? ok(null) : fail("删除失败");
    }
    
    @GetMapping("/articles/{id}/files")
    public ResponseEntity<Map<String, Object>> getArticleFiles(@PathVariable Long id) {
        List<KnowledgeFile> files = knowledgeService.getFilesByArticleId(id);
        return ok(files);
    }
    
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(@RequestParam String keyword) {
        List<KnowledgeArticle> articles = knowledgeService.searchArticles(keyword);
        return ok(articles);
    }
    
    private ResponseEntity<Map<String, Object>> ok(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", data);
        return ResponseEntity.ok(response);
    }
    
    private ResponseEntity<Map<String, Object>> fail(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 500);
        response.put("message", message);
        return ResponseEntity.ok(response);
    }
}
