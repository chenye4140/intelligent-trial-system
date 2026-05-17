-- H2 test schema for casemanage module (MySQL compatible mode)

-- Dependent tables (for JOIN queries in CaseInfoMapper)
CREATE TABLE IF NOT EXISTS sys_dept (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT DEFAULT 0,
    dept_name VARCHAR(100) NOT NULL,
    leader VARCHAR(50),
    phone VARCHAR(20),
    sort INT DEFAULT 0,
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    dept_id BIGINT,
    status INT DEFAULT 1,
    deleted INT DEFAULT 0,
    last_login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_classification_level (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    level_code VARCHAR(20) NOT NULL UNIQUE,
    level_name VARCHAR(50) NOT NULL,
    level_order INT NOT NULL,
    description VARCHAR(200),
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS case_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    case_code VARCHAR(50) NOT NULL UNIQUE,
    case_name VARCHAR(200) NOT NULL,
    case_type INT NOT NULL,
    case_source VARCHAR(100),
    respondent_name VARCHAR(100),
    respondent_dept VARCHAR(200),
    respondent_position VARCHAR(100),
    classification_level_id BIGINT,
    status INT NOT NULL DEFAULT 0,
    filing_date DATE,
    close_date DATE,
    brief_description CLOB,
    handling_dept_id BIGINT,
    handling_user_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS case_party (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    case_id BIGINT NOT NULL,
    party_name VARCHAR(100) NOT NULL,
    party_type INT NOT NULL,
    gender INT,
    id_number VARCHAR(50),
    dept VARCHAR(200),
    position VARCHAR(100),
    phone VARCHAR(20),
    relation VARCHAR(200),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS case_violation_fact (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    case_id BIGINT NOT NULL,
    fact_title VARCHAR(200) NOT NULL,
    fact_content CLOB,
    violation_type VARCHAR(100),
    occurred_date DATE,
    amount DECIMAL(12,2),
    evidence CLOB,
    sort INT NOT NULL DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
