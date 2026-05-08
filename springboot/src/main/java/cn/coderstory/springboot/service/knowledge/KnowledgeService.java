package cn.coderstory.springboot.service.knowledge;

import cn.coderstory.springboot.entity.knowledge.KnowledgeArticle;
import cn.coderstory.springboot.entity.knowledge.KnowledgeCategory;
import cn.coderstory.springboot.entity.knowledge.KnowledgeFile;
import cn.coderstory.springboot.entity.knowledge.KnowledgeTag;

import java.util.List;
import java.util.Map;

/**
 * 知识库管理服务接口。
 * <p>
 * 提供知识文章和分类的 CRUD 操作、标签管理、文件附件管理功能。
 *
 * @since 1.7.0
 */
public interface KnowledgeService {

    /**
     * 获取分类树。
     *
     * @return 分类树结构列表
     */
    List<KnowledgeCategory> getCategoryTree();

    /**
     * 创建知识分类。
     *
     * @param category 分类实体
     * @return 已创建的分类
     */
    KnowledgeCategory createCategory(KnowledgeCategory category);

    /**
     * 更新知识分类。
     *
     * @param id       分类 ID
     * @param category 分类更新数据
     * @return 更新后的分类
     */
    KnowledgeCategory updateCategory(Long id, KnowledgeCategory category);

    /**
     * 删除知识分类。
     *
     * @param id 分类 ID
     * @return 删除是否成功
     */
    boolean deleteCategory(Long id);

    /**
     * 分页查询知识文章。
     *
     * @param keyword    关键字（可选，模糊匹配标题和内容）
     * @param categoryId 分类 ID（可选）
     * @param page       页码
     * @param size       每页条数
     * @return 分页结果，包含文章列表和总数
     */
    Map<String, Object> getArticlePage(String keyword, Long categoryId, int page, int size);

    /**
     * 根据 ID 获取文章详情。
     *
     * @param id 文章 ID
     * @return 文章实体
     */
    KnowledgeArticle getArticleById(Long id);

    /**
     * 创建知识文章。
     *
     * @param article 文章实体
     * @param tagIds  标签 ID 列表
     * @return 已创建的文章
     */
    KnowledgeArticle createArticle(KnowledgeArticle article, List<Long> tagIds);

    /**
     * 更新知识文章。
     *
     * @param id      文章 ID
     * @param article 文章更新数据
     * @param tagIds  标签 ID 列表
     * @return 更新后的文章
     */
    KnowledgeArticle updateArticle(Long id, KnowledgeArticle article, List<Long> tagIds);

    /**
     * 删除知识文章。
     *
     * @param id 文章 ID
     * @return 删除是否成功
     */
    boolean deleteArticle(Long id);

    /**
     * 增加文章浏览次数。
     *
     * @param id 文章 ID
     */
    void incrementViewCount(Long id);

    /**
     * 获取所有标签。
     *
     * @return 标签列表
     */
    List<KnowledgeTag> getAllTags();

    /**
     * 创建标签。
     *
     * @param tag 标签实体
     * @return 已创建的标签
     */
    KnowledgeTag createTag(KnowledgeTag tag);

    /**
     * 删除标签。
     *
     * @param id 标签 ID
     * @return 删除是否成功
     */
    boolean deleteTag(Long id);

    /**
     * 根据文章 ID 获取关联标签列表。
     *
     * @param articleId 文章 ID
     * @return 标签列表
     */
    List<KnowledgeTag> getTagsByArticleId(Long articleId);

    /**
     * 上传文件附件。
     *
     * @param articleId   关联文章 ID
     * @param fileName    文件名
     * @param data        文件二进制数据
     * @param contentType 文件 MIME 类型
     * @return 文件元数据
     */
    KnowledgeFile uploadFile(Long articleId, String fileName, byte[] data, String contentType);

    /**
     * 下载文件附件。
     *
     * @param fileId 文件 ID
     * @return 文件二进制数据
     */
    byte[] downloadFile(Long fileId);

    /**
     * 获取文件元数据。
     *
     * @param fileId 文件 ID
     * @return 文件元数据实体
     */
    KnowledgeFile getFileMetadata(Long fileId);

    /**
     * 删除文件附件。
     *
     * @param fileId 文件 ID
     * @return 删除是否成功
     */
    boolean deleteFile(Long fileId);

    /**
     * 根据文章 ID 获取附件列表。
     *
     * @param articleId 文章 ID
     * @return 文件列表
     */
    List<KnowledgeFile> getFilesByArticleId(Long articleId);

    /**
     * 搜索知识文章。
     *
     * @param keyword 搜索关键字
     * @return 匹配的文章列表
     */
    List<KnowledgeArticle> searchArticles(String keyword);
}
