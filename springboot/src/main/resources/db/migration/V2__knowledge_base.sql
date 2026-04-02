-- Flyway 迁移脚本 V2 - 知识库系统

USE admin_system;

-- 知识分类表
CREATE TABLE IF NOT EXISTS knowledge_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    sort_order INT DEFAULT 0 COMMENT '排序',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识分类表';

-- 知识文章表
CREATE TABLE IF NOT EXISTS knowledge_article (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    title VARCHAR(255) NOT NULL COMMENT '标题',
    content LONGTEXT COMMENT '内容(Markdown)',
    status TINYINT DEFAULT 1 COMMENT '状态(0草稿1发布)',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category_id (category_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识文章表';

-- 知识标签表
CREATE TABLE IF NOT EXISTS knowledge_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    name VARCHAR(50) NOT NULL COMMENT '标签名称',
    color VARCHAR(20) DEFAULT '#409EFF' COMMENT '标签颜色',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识标签表';

-- 文章标签中间表（多对多）
CREATE TABLE IF NOT EXISTS knowledge_article_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    article_id BIGINT NOT NULL COMMENT '文章ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    UNIQUE KEY uk_article_tag (article_id, tag_id),
    INDEX idx_article_id (article_id),
    INDEX idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章标签关联表';

-- 知识文件表（zstd压缩存储）
CREATE TABLE IF NOT EXISTS knowledge_file (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    article_id BIGINT COMMENT '关联文章ID',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_size BIGINT COMMENT '原始文件大小',
    compressed_data LONGBLOB COMMENT 'zstd压缩后的二进制数据',
    compressed_size BIGINT COMMENT '压缩后大小',
    content_type VARCHAR(100) COMMENT 'MIME类型',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_article_id (article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识文件表';

-- 初始化知识分类示例数据
INSERT INTO knowledge_category (id, parent_id, name, sort_order) VALUES
(1, 0, '产品文档', 1),
(2, 0, '技术文档', 2),
(3, 0, '运营指南', 3);

-- 初始化知识标签示例数据
INSERT INTO knowledge_tag (name, color) VALUES
('入门', '#67C23A'),
('进阶', '#E6A23C'),
('高级', '#F56C6C'),
('FAQ', '#909399');
