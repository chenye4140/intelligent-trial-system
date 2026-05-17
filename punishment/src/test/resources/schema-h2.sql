-- H2 test schema for punishment module (MySQL compatible mode)

-- 处分执行表
CREATE TABLE IF NOT EXISTS punishment_execution (
    id              BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    case_id         VARCHAR(100)    NOT NULL COMMENT '关联案件ID',
    punishment_type VARCHAR(50)     NOT NULL COMMENT '处分类型',
    decision_date   DATE            DEFAULT NULL COMMENT '决定日期',
    start_date      DATE            DEFAULT NULL COMMENT '开始日期',
    end_date        DATE            DEFAULT NULL COMMENT '结束日期',
    status          INT             DEFAULT 0 COMMENT '状态：0=待执行, 1=执行中, 2=已完成, 3=已撤销',
    reminder_flag   INT             DEFAULT 0 COMMENT '提醒标志：0=未提醒, 1=已提醒',
    is_overdue      INT             DEFAULT 0 COMMENT '是否逾期：0=否, 1=是',
    create_time     TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- 处分材料表
CREATE TABLE IF NOT EXISTS punishment_material (
    id              BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    execution_id    BIGINT          NOT NULL COMMENT '关联处分执行ID',
    material_type   VARCHAR(50)     NOT NULL COMMENT '材料类型',
    file_path       VARCHAR(500)    NOT NULL COMMENT '文件存储路径',
    upload_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    uploader_id     BIGINT          DEFAULT NULL COMMENT '上传人ID',
    create_time     TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);
