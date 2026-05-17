-- H2 test schema for repository module (MySQL compatible mode)

-- repo_directory 表
CREATE TABLE IF NOT EXISTS repo_directory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    repo_type INT NOT NULL,
    parent_id BIGINT DEFAULT 0,
    name VARCHAR(200) NOT NULL,
    sort INT DEFAULT 0,
    classification_level_id BIGINT,
    permission_scope VARCHAR(50) DEFAULT 'public',
    path VARCHAR(500),
    status INT DEFAULT 1,
    deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(50),
    update_by VARCHAR(50)
);

-- repo_document 表
CREATE TABLE IF NOT EXISTS repo_document (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    repo_type INT NOT NULL,
    directory_id BIGINT,
    title VARCHAR(500) NOT NULL,
    doc_no VARCHAR(100),
    publish_unit VARCHAR(200),
    publish_date DATE,
    effective_date DATE,
    revision_date DATE,
    validity_status VARCHAR(20) DEFAULT 'valid',
    classification_level_id BIGINT,
    file_path VARCHAR(500),
    file_size BIGINT,
    file_type VARCHAR(20),
    summary CLOB,
    vector_id VARCHAR(100),
    status INT DEFAULT 0,
    case_id BIGINT,
    deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(50),
    update_by VARCHAR(50)
);
