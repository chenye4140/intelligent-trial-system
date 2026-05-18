-- H2 test schema for readingnote module (MySQL compatible mode)

CREATE TABLE IF NOT EXISTS reading_note (
    id          BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    case_id     VARCHAR(100)    NOT NULL COMMENT '关联案件ID',
    title       VARCHAR(200)    NOT NULL COMMENT '笔记标题',
    content     TEXT            DEFAULT NULL COMMENT '笔记内容',
    tags        VARCHAR(500)    DEFAULT NULL COMMENT '标签（逗号分隔）',
    note_type   INT             DEFAULT 1 COMMENT '笔记类型：1=阅卷笔记, 2=庭审笔记, 3=调查笔记',
    is_shared   INT             DEFAULT 0 COMMENT '是否共享：0=私有, 1=共享',
    user_id     BIGINT          DEFAULT NULL COMMENT '创建人ID',
    create_time TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);
