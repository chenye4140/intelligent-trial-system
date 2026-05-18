-- H2 test schema for promotion module (MySQL compatible mode)

-- 以案促改表
CREATE TABLE IF NOT EXISTS case_promotion (
    id              BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    case_id         VARCHAR(100)    NOT NULL COMMENT '关联案件ID',
    template_id     BIGINT          DEFAULT NULL COMMENT '使用的模板ID',
    content         CLOB            NOT NULL COMMENT '促改内容',
    status          INT             NOT NULL DEFAULT 0 COMMENT '状态：0=草稿, 1=待审核, 2=已通过, 3=已驳回',
    user_id         BIGINT          DEFAULT NULL COMMENT '创建人ID',
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

CREATE INDEX IF NOT EXISTS idx_case_id ON case_promotion(case_id);
CREATE INDEX IF NOT EXISTS idx_status ON case_promotion(status);
CREATE INDEX IF NOT EXISTS idx_user_id ON case_promotion(user_id);
