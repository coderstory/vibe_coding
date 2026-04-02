package cn.coderstory.springboot.service.impl;

import cn.coderstory.springboot.entity.KnowledgeArticle;
import cn.coderstory.springboot.entity.KnowledgeArticleTag;
import cn.coderstory.springboot.entity.KnowledgeCategory;
import cn.coderstory.springboot.entity.KnowledgeFile;
import cn.coderstory.springboot.entity.KnowledgeTag;
import cn.coderstory.springboot.mapper.*;
import cn.coderstory.springboot.service.KnowledgeService;
import cn.coderstory.springboot.util.ZstdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {
    
    private final KnowledgeCategoryMapper categoryMapper;
    private final KnowledgeArticleMapper articleMapper;
    private final KnowledgeTagMapper tagMapper;
    private final KnowledgeArticleTagMapper articleTagMapper;
    private final KnowledgeFileMapper fileMapper;
    
    @Override
    public List<KnowledgeCategory> getCategoryTree() {
        List<KnowledgeCategory> all = categoryMapper.selectTree();
        return buildTree(all);
    }
    
    private List<KnowledgeCategory> buildTree(List<KnowledgeCategory> list) {
        Map<Long, List<KnowledgeCategory>> group = list.stream()
                .collect(Collectors.groupingBy(KnowledgeCategory::getParentId));
        for (KnowledgeCategory cat : list) {
            cat.setChildren(group.getOrDefault(cat.getId(), new ArrayList<>()));
        }
        return list.stream().filter(c -> c.getParentId() == 0L).collect(Collectors.toList());
    }
    
    @Override
    public KnowledgeCategory createCategory(KnowledgeCategory category) {
        categoryMapper.insert(category);
        return category;
    }
    
    @Override
    public KnowledgeCategory updateCategory(Long id, KnowledgeCategory category) {
        category.setId(id);
        categoryMapper.updateById(category);
        return category;
    }
    
    @Override
    public boolean deleteCategory(Long id) {
        return categoryMapper.deleteById(id) > 0;
    }
    
    @Override
    public Map<String, Object> getArticlePage(String keyword, Long categoryId, int page, int size) {
        Page<KnowledgeArticle> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<KnowledgeArticle> wrapper = new LambdaQueryWrapper<>();
        if (categoryId != null) {
            wrapper.eq(KnowledgeArticle::getCategoryId, categoryId);
        }
        wrapper.orderByDesc(KnowledgeArticle::getCreateTime);
        Page<KnowledgeArticle> result = articleMapper.selectPage(pageParam, wrapper);
        
        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("size", result.getSize());
        data.put("current", result.getCurrent());
        data.put("pages", result.getPages());
        return data;
    }
    
    @Override
    public KnowledgeArticle getArticleById(Long id) {
        KnowledgeArticle article = articleMapper.selectById(id);
        if (article != null) {
            List<KnowledgeTag> tags = getTagsByArticleId(id);
            article.setTags(tags.stream().map(KnowledgeTag::getName).collect(Collectors.toList()));
        }
        return article;
    }
    
    @Override
    @Transactional
    public KnowledgeArticle createArticle(KnowledgeArticle article, List<Long> tagIds) {
        articleMapper.insert(article);
        if (tagIds != null && !tagIds.isEmpty()) {
            saveArticleTags(article.getId(), tagIds);
        }
        return article;
    }
    
    @Override
    @Transactional
    public KnowledgeArticle updateArticle(Long id, KnowledgeArticle article, List<Long> tagIds) {
        article.setId(id);
        articleMapper.updateById(article);
        if (tagIds != null) {
            articleTagMapper.deleteByArticleId(id);
            saveArticleTags(id, tagIds);
        }
        return article;
    }
    
    private void saveArticleTags(Long articleId, List<Long> tagIds) {
        for (Long tagId : tagIds) {
            KnowledgeArticleTag at = new KnowledgeArticleTag();
            at.setArticleId(articleId);
            at.setTagId(tagId);
            articleTagMapper.insert(at);
        }
    }
    
    @Override
    public boolean deleteArticle(Long id) {
        return articleMapper.deleteById(id) > 0;
    }
    
    @Override
    public void incrementViewCount(Long id) {
        KnowledgeArticle article = articleMapper.selectById(id);
        if (article != null) {
            article.setViewCount(article.getViewCount() == null ? 1 : article.getViewCount() + 1);
            articleMapper.updateById(article);
        }
    }
    
    @Override
    public List<KnowledgeTag> getAllTags() {
        return tagMapper.selectList(new LambdaQueryWrapper<>());
    }
    
    @Override
    public KnowledgeTag createTag(KnowledgeTag tag) {
        tagMapper.insert(tag);
        return tag;
    }
    
    @Override
    public boolean deleteTag(Long id) {
        return tagMapper.deleteById(id) > 0;
    }
    
    @Override
    public List<KnowledgeTag> getTagsByArticleId(Long articleId) {
        List<Long> tagIds = articleTagMapper.selectTagIdsByArticleId(articleId);
        if (tagIds.isEmpty()) {
            return new ArrayList<>();
        }
        return tagMapper.selectBatchIds(tagIds);
    }
    
    @Override
    public KnowledgeFile uploadFile(Long articleId, String fileName, byte[] data, String contentType) {
        byte[] compressed = ZstdUtil.compress(data);
        KnowledgeFile file = new KnowledgeFile();
        file.setArticleId(articleId);
        file.setFileName(fileName);
        file.setFileSize((long) data.length);
        file.setCompressedData(compressed);
        file.setCompressedSize(compressed != null ? (long) compressed.length : 0L);
        file.setContentType(contentType);
        fileMapper.insert(file);
        return file;
    }
    
    @Override
    public byte[] downloadFile(Long fileId) {
        KnowledgeFile file = fileMapper.selectById(fileId);
        if (file == null) {
            return null;
        }
        return ZstdUtil.decompress(file.getCompressedData(), file.getFileSize());
    }
    
    @Override
    public KnowledgeFile getFileMetadata(Long fileId) {
        return fileMapper.selectById(fileId);
    }
    
    @Override
    public boolean deleteFile(Long fileId) {
        return fileMapper.deleteById(fileId) > 0;
    }
    
    @Override
    public List<KnowledgeFile> getFilesByArticleId(Long articleId) {
        return fileMapper.selectByArticleId(articleId);
    }
    
    @Override
    public List<KnowledgeArticle> searchArticles(String keyword) {
        return articleMapper.searchArticles(keyword, null);
    }
}
