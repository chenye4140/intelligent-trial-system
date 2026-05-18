-- H2 test schema for document module (MySQL compatible mode)

-- 文档解析任务表
CREATE TABLE IF NOT EXISTS doc_parse_task (
    id              BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    file_name       VARCHAR(255)    NOT NULL COMMENT '文件名',
    file_path       VARCHAR(500)    NOT NULL COMMENT '文件存储路径',
    file_type       VARCHAR(20)     DEFAULT NULL COMMENT '文件类型',
    status          INT             NOT NULL DEFAULT 0 COMMENT '状态：0=待处理, 1=处理中, 2=已完成, 3=失败',
    progress        INT             NOT NULL DEFAULT 0 COMMENT '进度（0-100）',
    result_json     CLOB            DEFAULT NULL COMMENT '解析结果（JSON格式）',
    error_msg       CLOB            DEFAULT NULL COMMENT '错误信息',
    parse_time      TIMESTAMP       DEFAULT NULL COMMENT '完成时间',
    vector_count    INT             NOT NULL DEFAULT 0 COMMENT '生成的向量片段数量',
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    document_id     BIGINT          DEFAULT NULL COMMENT '关联的库文档ID',
    KEY idx_status (status),
    KEY idx_file_type (file_type)
);

-- 段落向量存储表
CREATE TABLE IF NOT EXISTS doc_paragraph_vector (
    id                BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    task_id           BIGINT          NOT NULL COMMENT '关联解析任务ID',
    paragraph_index   INT             NOT NULL COMMENT '段落序号',
    content           CLOB            NOT NULL COMMENT '段落文本内容',
    category          VARCHAR(50)     DEFAULT NULL COMMENT '分类',
    law_level         VARCHAR(20)     DEFAULT NULL COMMENT '法规层级',
    vector_data       CLOB            NOT NULL COMMENT '向量数据（JSON数组格式）',
    vector_dimension  INT             NOT NULL COMMENT '向量维度',
    create_time       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_task_id (task_id),
    KEY idx_category (category),
    KEY idx_law_level (law_level)
);

-- 类案相似度记录表
CREATE TABLE IF NOT EXISTS case_similarity_record (
    id                BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    source_case_id    VARCHAR(100)    NOT NULL COMMENT '源案件ID',
    similar_case_id   VARCHAR(100)    NOT NULL COMMENT '相似案件ID',
    similarity_score  DECIMAL(5,4)    NOT NULL COMMENT '综合相似度得分',
    content_score     DECIMAL(5,4)    NOT NULL DEFAULT 0.0000 COMMENT '内容相似度得分',
    amount_score      DECIMAL(5,4)    NOT NULL DEFAULT 0.0000 COMMENT '金额相似度得分',
    type_score        DECIMAL(5,4)    NOT NULL DEFAULT 0.0000 COMMENT '类型相似度得分',
    create_time       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT uk_case_pair UNIQUE (source_case_id, similar_case_id)
);
