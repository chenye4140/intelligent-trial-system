-- H2 test schema for report module (MySQL compatible mode)

-- 文书模板表
CREATE TABLE IF NOT EXISTS report_template (
    id            BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    template_code VARCHAR(50)     NOT NULL COMMENT '模板编码',
    template_name VARCHAR(100)    NOT NULL COMMENT '模板名称',
    template_type INT             NOT NULL COMMENT '模板类型：1=审理报告, 2=处分决定, 3=谈话笔录, 4=初核报告',
    content       TEXT            DEFAULT NULL COMMENT '模板内容',
    description   VARCHAR(500)    DEFAULT NULL COMMENT '模板描述',
    status        INT             DEFAULT 1 COMMENT '状态：0=禁用, 1=启用',
    create_time   TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- 文书生成记录表
CREATE TABLE IF NOT EXISTS report_record (
    id             BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    case_id        BIGINT          DEFAULT NULL COMMENT '关联案件ID',
    case_code      VARCHAR(100)    DEFAULT NULL COMMENT '案件编号',
    template_id    BIGINT          DEFAULT NULL COMMENT '模板ID',
    template_code  VARCHAR(50)     DEFAULT NULL COMMENT '模板编码',
    report_title   VARCHAR(200)    DEFAULT NULL COMMENT '文书标题',
    report_content TEXT            DEFAULT NULL COMMENT '文书内容',
    generated_by   BIGINT          DEFAULT NULL COMMENT '生成人ID',
    status         INT             DEFAULT 0 COMMENT '状态：0=生成中, 1=已完成, 2=失败',
    error_message  VARCHAR(500)    DEFAULT NULL COMMENT '错误信息',
    create_time    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    update_time    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- 案件信息表（跨模块查询，ReportService 通过 JdbcTemplate 查询）
CREATE TABLE IF NOT EXISTS case_info (
    id                BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    case_code         VARCHAR(100)    NOT NULL COMMENT '案件编号',
    case_name         VARCHAR(200)    DEFAULT NULL COMMENT '案件名称',
    case_type         INT             DEFAULT NULL COMMENT '案件类型：1=违纪, 2=违法, 3=职务犯罪',
    case_source       VARCHAR(100)    DEFAULT NULL COMMENT '案件来源',
    respondent_name   VARCHAR(100)    DEFAULT NULL COMMENT '被调查人姓名',
    respondent_dept   VARCHAR(200)    DEFAULT NULL COMMENT '被调查人单位',
    respondent_position VARCHAR(200)  DEFAULT NULL COMMENT '被调查人职务',
    status            INT             DEFAULT 0 COMMENT '状态：0=草稿, 1=审理中, 2=已完结, 3=已归档',
    filing_date       DATE            DEFAULT NULL COMMENT '立案日期',
    close_date        DATE            DEFAULT NULL COMMENT '结案日期',
    brief_description TEXT            DEFAULT NULL COMMENT '简要案情',
    create_time       TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    update_time       TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);
