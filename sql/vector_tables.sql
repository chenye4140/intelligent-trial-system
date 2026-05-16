-- ==========================================
-- 向量存储表 DDL
-- 用于存储文档段落向量，支持类案相似度查询
-- MySQL 8.0 / utf8mb4 / InnoDB
-- ==========================================

USE intelligent_trial;

-- ==========================================
-- 段落向量存储表
-- ==========================================

CREATE TABLE IF NOT EXISTS doc_paragraph_vector (
    id                BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    task_id           BIGINT UNSIGNED   NOT NULL COMMENT '关联解析任务ID',
    paragraph_index   INT               NOT NULL COMMENT '段落序号',
    content           TEXT              NOT NULL COMMENT '段落文本内容',
    category          VARCHAR(50)       DEFAULT NULL COMMENT '分类：总则/分则/附则/法律责任/案件事实/处理意见/法律依据',
    law_level         VARCHAR(20)       DEFAULT NULL COMMENT '法规层级：篇/章/节/条/款/项',
    vector_data       TEXT              NOT NULL COMMENT '向量数据（JSON数组格式）',
    vector_dimension  INT               NOT NULL COMMENT '向量维度',
    create_time       DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_task_id (task_id),
    KEY idx_category (category),
    KEY idx_law_level (law_level),
    KEY idx_create_time (create_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='段落向量存储表';
