package cn.coderstory.springboot.knowledge.service;

import cn.coderstory.springboot.knowledge.entity.KnowledgeArticle;
import cn.coderstory.springboot.knowledge.entity.KnowledgeCategory;
import cn.coderstory.springboot.knowledge.entity.KnowledgeFile;
import cn.coderstory.springboot.knowledge.entity.KnowledgeTag;
import java.util.List;
import java.util.Map;

public interface KnowledgeService {
    
    List<KnowledgeCategory> getCategoryTree();
    
    KnowledgeCategory createCategory(KnowledgeCategory category);
    
    KnowledgeCategory updateCategory(Long id, KnowledgeCategory category);
    
    boolean deleteCategory(Long id);
    
    Map<String, Object> getArticlePage(String keyword, Long categoryId, int page, int size);
    
    KnowledgeArticle getArticleById(Long id);
    
    KnowledgeArticle createArticle(KnowledgeArticle article, List<Long> tagIds);
    
    KnowledgeArticle updateArticle(Long id, KnowledgeArticle article, List<Long> tagIds);
    
    boolean deleteArticle(Long id);
    
    void incrementViewCount(Long id);
    
    List<KnowledgeTag> getAllTags();
    
    KnowledgeTag createTag(KnowledgeTag tag);
    
    boolean deleteTag(Long id);
    
    List<KnowledgeTag> getTagsByArticleId(Long articleId);
    
    KnowledgeFile uploadFile(Long articleId, String fileName, byte[] data, String contentType);
    
    byte[] downloadFile(Long fileId);
    
    KnowledgeFile getFileMetadata(Long fileId);
    
    boolean deleteFile(Long fileId);
    
    List<KnowledgeFile> getFilesByArticleId(Long articleId);
    
    List<KnowledgeArticle> searchArticles(String keyword);
}
