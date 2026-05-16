-- ==========================================
-- 智能审理系统数据库初始化脚本
-- MySQL 8.0 / utf8mb4 / InnoDB
-- ==========================================

CREATE DATABASE IF NOT EXISTS intelligent_trial DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE intelligent_trial;

-- ==========================================
-- 1. 权限管理模块 (RBAC)
-- ==========================================

-- 1.1 部门表
DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    parent_id       BIGINT UNSIGNED   NOT NULL DEFAULT 0 COMMENT '上级部门ID，0表示顶级',
    dept_name       VARCHAR(100)      NOT NULL COMMENT '部门名称',
    leader          VARCHAR(50)       DEFAULT NULL COMMENT '负责人',
    phone           VARCHAR(20)       DEFAULT NULL COMMENT '联系电话',
    sort            INT               NOT NULL DEFAULT 0 COMMENT '排序',
    status          TINYINT UNSIGNED  NOT NULL DEFAULT 1 COMMENT '状态：0=停用, 1=启用',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

-- 1.2 角色表
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_name       VARCHAR(50)       NOT NULL COMMENT '角色名称',
    role_code       VARCHAR(50)       NOT NULL COMMENT '角色编码',
    description     VARCHAR(255)      DEFAULT NULL COMMENT '描述',
    status          TINYINT UNSIGNED  NOT NULL DEFAULT 1 COMMENT '状态：0=停用, 1=启用',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 1.3 用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    username        VARCHAR(50)       NOT NULL COMMENT '用户名',
    password        VARCHAR(255)      NOT NULL COMMENT '密码（BCrypt加密）',
    real_name       VARCHAR(50)       NOT NULL COMMENT '真实姓名',
    dept_id         BIGINT UNSIGNED   DEFAULT NULL COMMENT '所属部门ID',
    phone           VARCHAR(20)       DEFAULT NULL COMMENT '手机号',
    email           VARCHAR(100)      DEFAULT NULL COMMENT '邮箱',
    status          TINYINT UNSIGNED  NOT NULL DEFAULT 1 COMMENT '状态：0=停用, 1=启用',
    last_login_time DATETIME          DEFAULT NULL COMMENT '最后登录时间',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_dept_id (dept_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 1.4 用户角色关联表
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id         BIGINT UNSIGNED   NOT NULL COMMENT '用户ID',
    role_id         BIGINT UNSIGNED   NOT NULL COMMENT '角色ID',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 1.5 菜单/权限表
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    parent_id       BIGINT UNSIGNED   NOT NULL DEFAULT 0 COMMENT '上级菜单ID，0表示顶级',
    name            VARCHAR(50)       NOT NULL COMMENT '菜单名称',
    path            VARCHAR(255)      DEFAULT NULL COMMENT '路由路径',
    component       VARCHAR(255)      DEFAULT NULL COMMENT '前端组件路径',
    perms           VARCHAR(100)      DEFAULT NULL COMMENT '权限标识（如 system:user:list）',
    type            TINYINT UNSIGNED  NOT NULL DEFAULT 1 COMMENT '类型：1=目录, 2=菜单, 3=按钮',
    icon            VARCHAR(100)      DEFAULT NULL COMMENT '图标',
    sort            INT               NOT NULL DEFAULT 0 COMMENT '排序',
    visible         TINYINT UNSIGNED  NOT NULL DEFAULT 1 COMMENT '是否可见：0=隐藏, 1=显示',
    status          TINYINT UNSIGNED  NOT NULL DEFAULT 1 COMMENT '状态：0=停用, 1=启用',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id),
    KEY idx_type (type),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单/权限表';

-- 1.6 角色菜单关联表
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_id         BIGINT UNSIGNED   NOT NULL COMMENT '角色ID',
    menu_id         BIGINT UNSIGNED   NOT NULL COMMENT '菜单ID',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    KEY idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

-- ==========================================
-- 2. 定密管理
-- ==========================================

-- 2.1 密级字典表
DROP TABLE IF EXISTS sys_classification_level;
CREATE TABLE sys_classification_level (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    level_code      VARCHAR(20)       NOT NULL COMMENT '密级编码',
    level_name      VARCHAR(20)       NOT NULL COMMENT '密级名称',
    sort            INT               NOT NULL DEFAULT 0 COMMENT '排序（数值越小密级越高）',
    status          TINYINT UNSIGNED  NOT NULL DEFAULT 1 COMMENT '状态：0=停用, 1=启用',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_level_code (level_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='密级字典表';

-- ==========================================
-- 3. 多库管理 - 目录
-- ==========================================

-- 3.1 统一目录表
DROP TABLE IF EXISTS repo_directory;
CREATE TABLE repo_directory (
    id                      BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    repo_type               TINYINT UNSIGNED  NOT NULL COMMENT '库类型：1=法规库, 2=资料库, 3=裁判文书库, 4=案例库',
    parent_id               BIGINT UNSIGNED   NOT NULL DEFAULT 0 COMMENT '上级目录ID，0表示根目录',
    name                    VARCHAR(100)      NOT NULL COMMENT '目录名称',
    sort                    INT               NOT NULL DEFAULT 0 COMMENT '排序',
    classification_level_id BIGINT UNSIGNED   DEFAULT NULL COMMENT '密级ID（关联sys_classification_level）',
    permission_scope        VARCHAR(255)      DEFAULT NULL COMMENT '权限范围（逗号分隔的角色ID列表）',
    path                    VARCHAR(500)      DEFAULT NULL COMMENT '层级路径（如 /1/3/5）',
    status                  TINYINT UNSIGNED  NOT NULL DEFAULT 1 COMMENT '状态：0=停用, 1=启用',
    create_time             DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time             DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_repo_type (repo_type),
    KEY idx_parent_id (parent_id),
    KEY idx_classification_level_id (classification_level_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='统一目录表';

-- ==========================================
-- 4. 多库管理 - 文档
-- ==========================================

-- 4.1 统一文档表
DROP TABLE IF EXISTS repo_document;
CREATE TABLE repo_document (
    id                      BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    repo_type               TINYINT UNSIGNED  NOT NULL COMMENT '库类型：1=法规库, 2=资料库, 3=裁判文书库, 4=案例库',
    directory_id            BIGINT UNSIGNED   NOT NULL COMMENT '所属目录ID',
    title                   VARCHAR(500)      NOT NULL COMMENT '文档标题',
    doc_no                  VARCHAR(100)      DEFAULT NULL COMMENT '文号',
    publish_unit            VARCHAR(100)      DEFAULT NULL COMMENT '发布单位',
    publish_date            DATE              DEFAULT NULL COMMENT '发布日期',
    effective_date          DATE              DEFAULT NULL COMMENT '生效日期',
    revision_date           DATE              DEFAULT NULL COMMENT '修订日期',
    validity_status         TINYINT UNSIGNED  NOT NULL DEFAULT 1 COMMENT '效力状态：1=现行有效, 2=已废止, 3=已修订',
    classification_level_id BIGINT UNSIGNED   DEFAULT NULL COMMENT '密级ID（关联sys_classification_level）',
    file_path               VARCHAR(500)      DEFAULT NULL COMMENT '文件存储路径',
    file_size               BIGINT UNSIGNED   DEFAULT NULL COMMENT '文件大小（字节）',
    file_type               VARCHAR(20)       DEFAULT NULL COMMENT '文件类型（pdf/docx/txt等）',
    summary                 TEXT              DEFAULT NULL COMMENT '摘要/简介',
    vector_id               VARCHAR(100)      DEFAULT NULL COMMENT '向量数据库ID（用于语义检索）',
    status                  TINYINT UNSIGNED  NOT NULL DEFAULT 1 COMMENT '状态：0=停用, 1=启用',
    create_time             DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time             DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_repo_type (repo_type),
    KEY idx_directory_id (directory_id),
    KEY idx_doc_no (doc_no),
    KEY idx_publish_unit (publish_unit),
    KEY idx_validity_status (validity_status),
    KEY idx_classification_level_id (classification_level_id),
    KEY idx_status (status),
    FULLTEXT KEY ft_title_summary (title, summary)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='统一文档表';

-- ==========================================
-- 5. 智能文档解析
-- ==========================================

-- 5.1 解析任务表
DROP TABLE IF EXISTS doc_parse_task;
CREATE TABLE doc_parse_task (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    file_name       VARCHAR(255)      NOT NULL COMMENT '文件名',
    file_path       VARCHAR(500)      NOT NULL COMMENT '文件存储路径',
    file_type       VARCHAR(20)       DEFAULT NULL COMMENT '文件类型（pdf/docx/txt等）',
    status          TINYINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '状态：0=待处理, 1=处理中, 2=已完成, 3=失败',
    progress        INT               NOT NULL DEFAULT 0 COMMENT '进度（0-100）',
    result_json     JSON              DEFAULT NULL COMMENT '解析结果（JSON格式）',
    error_msg       TEXT              DEFAULT NULL COMMENT '错误信息',
    parse_time      DATETIME          DEFAULT NULL COMMENT '完成时间',
    vector_count    INT               NOT NULL DEFAULT 0 COMMENT '生成的向量片段数量',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_status (status),
    KEY idx_file_type (file_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档解析任务表';

-- ==========================================
-- 6. 类案推送
-- ==========================================

-- 6.1 类案相似度记录表
DROP TABLE IF EXISTS case_similarity_record;
CREATE TABLE case_similarity_record (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    source_case_id  VARCHAR(100)      NOT NULL COMMENT '源案件ID',
    similar_case_id VARCHAR(100)      NOT NULL COMMENT '相似案件ID',
    similarity_score DECIMAL(5,4)     NOT NULL COMMENT '综合相似度得分（0.0000-1.0000）',
    content_score   DECIMAL(5,4)      NOT NULL DEFAULT 0.0000 COMMENT '内容相似度得分',
    amount_score    DECIMAL(5,4)      NOT NULL DEFAULT 0.0000 COMMENT '金额相似度得分',
    type_score      DECIMAL(5,4)      NOT NULL DEFAULT 0.0000 COMMENT '类型相似度得分',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_source_case_id (source_case_id),
    KEY idx_similar_case_id (similar_case_id),
    KEY idx_similarity_score (similarity_score DESC),
    UNIQUE KEY uk_case_pair (source_case_id, similar_case_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='类案相似度记录表';

-- ==========================================
-- 7. 处分执行
-- ==========================================

-- 7.1 处分执行表
DROP TABLE IF EXISTS punishment_execution;
CREATE TABLE punishment_execution (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    case_id         VARCHAR(100)      NOT NULL COMMENT '关联案件ID',
    punishment_type VARCHAR(50)       NOT NULL COMMENT '处分类型（警告/记过/降级/撤职/开除等）',
    decision_date   DATE              DEFAULT NULL COMMENT '决定日期',
    start_date      DATE              DEFAULT NULL COMMENT '开始日期',
    end_date        DATE              DEFAULT NULL COMMENT '结束日期',
    status          TINYINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '状态：0=待执行, 1=执行中, 2=已完成, 3=已撤销',
    reminder_flag   TINYINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '提醒标志：0=未提醒, 1=已提醒',
    is_overdue      TINYINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '是否逾期：0=否, 1=是',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_case_id (case_id),
    KEY idx_status (status),
    KEY idx_end_date (end_date),
    KEY idx_is_overdue (is_overdue)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='处分执行表';

-- 7.2 处分材料表
DROP TABLE IF EXISTS punishment_material;
CREATE TABLE punishment_material (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    execution_id    BIGINT UNSIGNED   NOT NULL COMMENT '关联处分执行ID',
    material_type   VARCHAR(50)       NOT NULL COMMENT '材料类型（决定书/送达回证/执行报告等）',
    file_path       VARCHAR(500)      NOT NULL COMMENT '文件存储路径',
    upload_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    uploader_id     BIGINT UNSIGNED   DEFAULT NULL COMMENT '上传人ID',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_execution_id (execution_id),
    KEY idx_material_type (material_type),
    KEY idx_uploader_id (uploader_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='处分材料表';

-- ==========================================
-- 8. 阅卷笔记
-- ==========================================

-- 8.1 阅卷笔记表
DROP TABLE IF EXISTS reading_note;
CREATE TABLE reading_note (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    case_id         VARCHAR(100)      NOT NULL COMMENT '关联案件ID',
    title           VARCHAR(200)      NOT NULL COMMENT '笔记标题',
    content         TEXT              NOT NULL COMMENT '笔记内容',
    tags            VARCHAR(500)      DEFAULT NULL COMMENT '标签（逗号分隔）',
    note_type       TINYINT UNSIGNED  NOT NULL DEFAULT 1 COMMENT '笔记类型：1=个人笔记, 2=批注, 3=摘要',
    is_shared       TINYINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '是否共享：0=否, 1=是',
    user_id         BIGINT UNSIGNED   DEFAULT NULL COMMENT '创建人ID',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_case_id (case_id),
    KEY idx_user_id (user_id),
    KEY idx_note_type (note_type),
    KEY idx_is_shared (is_shared)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='阅卷笔记表';

-- ==========================================
-- 9. 以案促改
-- ==========================================

-- 9.1 以案促改表
DROP TABLE IF EXISTS case_promotion;
CREATE TABLE case_promotion (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    case_id         VARCHAR(100)      NOT NULL COMMENT '关联案件ID',
    template_id     BIGINT UNSIGNED   DEFAULT NULL COMMENT '使用的模板ID',
    content         TEXT              NOT NULL COMMENT '促改内容',
    status          TINYINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '状态：0=草稿, 1=待审核, 2=已通过, 3=已驳回',
    user_id         BIGINT UNSIGNED   DEFAULT NULL COMMENT '创建人ID',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_case_id (case_id),
    KEY idx_status (status),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='以案促改表';

-- ==========================================
-- 10. 审计日志
-- ==========================================

-- 10.1 审计日志表
DROP TABLE IF EXISTS sys_audit_log;
CREATE TABLE sys_audit_log (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id         BIGINT UNSIGNED   DEFAULT NULL COMMENT '操作用户ID',
    module          VARCHAR(50)       DEFAULT NULL COMMENT '操作模块',
    action          VARCHAR(50)       DEFAULT NULL COMMENT '操作类型（增/删/改/查/导出等）',
    description     VARCHAR(500)      DEFAULT NULL COMMENT '操作描述',
    ip              VARCHAR(50)       DEFAULT NULL COMMENT '操作IP',
    user_agent      VARCHAR(500)      DEFAULT NULL COMMENT '浏览器标识',
    request_url     VARCHAR(500)      DEFAULT NULL COMMENT '请求URL',
    request_method  VARCHAR(10)       DEFAULT NULL COMMENT '请求方法（GET/POST/PUT/DELETE）',
    params          TEXT              DEFAULT NULL COMMENT '请求参数',
    result          TINYINT UNSIGNED  DEFAULT NULL COMMENT '操作结果：0=失败, 1=成功',
    duration        INT               DEFAULT NULL COMMENT '耗时（毫秒）',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_module (module),
    KEY idx_action (action),
    KEY idx_result (result),
    KEY idx_create_time (create_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';

-- ==========================================
-- 11. 来文登记
-- ==========================================

-- 11.1 来文登记表
DROP TABLE IF EXISTS incoming_doc;
CREATE TABLE incoming_doc (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    doc_no          VARCHAR(100)      DEFAULT NULL COMMENT '来文文号',
    from_unit       VARCHAR(100)      DEFAULT NULL COMMENT '来文单位',
    title           VARCHAR(500)      NOT NULL COMMENT '来文标题',
    receive_date    DATE              NOT NULL COMMENT '收到日期',
    subject         VARCHAR(255)      DEFAULT NULL COMMENT '事由/主题',
    ocr_content     TEXT              DEFAULT NULL COMMENT 'OCR识别内容',
    status          TINYINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '状态：0=待处理, 1=处理中, 2=已办结, 3=已归档',
    handler_id      BIGINT UNSIGNED   DEFAULT NULL COMMENT '当前处理人ID',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_doc_no (doc_no),
    KEY idx_from_unit (from_unit),
    KEY idx_receive_date (receive_date),
    KEY idx_status (status),
    KEY idx_handler_id (handler_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='来文登记表';

-- ==========================================
-- 初始化数据
-- ==========================================

-- 初始化密级字典
INSERT INTO sys_classification_level (level_code, level_name, sort) VALUES
('TOP_SECRET', '绝密', 1),
('SECRET',     '机密', 2),
('CONFIDENTIAL', '秘密', 3),
('INTERNAL',   '内部', 4),
('PUBLIC',     '公开', 5);

-- 初始化默认部门
INSERT INTO sys_dept (parent_id, dept_name, leader, phone, sort) VALUES
(0, '智能审理系统', '系统管理员', '010-00000000', 0),
(1, '审理一室', '张三', '010-11111111', 1),
(1, '审理二室', '李四', '010-22222222', 2),
(1, '案件监督管理室', '王五', '010-33333333', 3);

-- 初始化默认角色
INSERT INTO sys_role (role_name, role_code, description) VALUES
('超级管理员', 'super_admin', '拥有系统全部权限'),
('审理人员', 'trial_staff', '案件审理相关权限'),
('查看人员', 'viewer', '仅查看权限');

-- 初始化默认管理员用户（密码: admin123，BCrypt加密）
INSERT INTO sys_user (username, password, real_name, dept_id, phone, email) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 1, '13800000000', 'admin@example.com');

-- 绑定管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 初始化默认菜单
INSERT INTO sys_menu (parent_id, name, path, component, perms, type, icon, sort) VALUES
-- 一级菜单
(0, '系统管理', '/system', 'Layout', NULL, 1, 'setting', 1),
(0, '定密管理', '/classification', 'Layout', NULL, 1, 'lock', 2),
(0, '法规库', '/repo/law', 'Layout', NULL, 1, 'book', 3),
(0, '资料库', '/repo/data', 'Layout', NULL, 1, 'folder', 4),
(0, '裁判文书库', '/repo/judgment', 'Layout', NULL, 1, 'document', 5),
(0, '案例库', '/repo/case', 'Layout', NULL, 1, 'cases', 6),
(0, '文档解析', '/parse', 'Layout', NULL, 1, 'analysis', 7),
(0, '类案推送', '/similar-case', 'Layout', NULL, 1, 'search', 8),
(0, '处分执行', '/punishment', 'Layout', NULL, 1, 'tool', 9),
(0, '阅卷笔记', '/reading-note', 'Layout', NULL, 1, 'edit', 10),
(0, '以案促改', '/case-promotion', 'Layout', NULL, 1, 'solution', 11),
(0, '来文登记', '/incoming-doc', 'Layout', NULL, 1, 'mail', 12),
(0, '审计日志', '/audit', 'Layout', NULL, 1, 'file-text', 13),
-- 系统管理子菜单
(1, '用户管理', 'user', 'system/user/index', 'system:user:list', 2, 'user', 1),
(1, '角色管理', 'role', 'system/role/index', 'system:role:list', 2, 'peoples', 2),
(1, '菜单管理', 'menu', 'system/menu/index', 'system:menu:list', 2, 'tree-table', 3),
(1, '部门管理', 'dept', 'system/dept/index', 'system:dept:list', 2, 'tree', 4),
-- 用户管理按钮权限
(14, '用户查询', NULL, NULL, 'system:user:query', 3, '#', 1),
(14, '用户新增', NULL, NULL, 'system:user:add', 3, '#', 2),
(14, '用户修改', NULL, NULL, 'system:user:edit', 3, '#', 3),
(14, '用户删除', NULL, NULL, 'system:user:remove', 3, '#', 4);

-- 为超级管理员绑定所有菜单权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;
-- ==================================================
-- 12. н�码敌的是名之亓件 用户件
-- ======================================================

-- 12.1 н�码敌之亓件一业亓是名么
P��9D$R�b��BU��5E266U���f�5$TDRD$�R66U���f����B$�t��BT�4�t�TB��B�T��UD����5$T�T�B4���T�B~ikz��Br��66U�6�FRd$4�"�S���B�T��4���T�B~i��Z�nY8�K��K��X�rr��66U���Rd$4�"�#���B�T��4���T�B~i��Z�nY.�랊�K��K��K��z��K��[��y�NY��h��r��66U�G�RD�唔�BT�4�t�TB��B�T��4���T�B~i��Y�[z^K��iK�i[��X�~K���#�X�~K��i[��>y��X��X�~K�z��Y��i{br��66U�6�W&6Rd$4�"��DTdT�B�T��4���T�B~i��Y��ɮY�K��Y�K��i[r��&W7��FV�E���Rd$4�"��DTdT�B�T��4���T�B~���i��K��iK�i[Xˮz�K��"��&W7��FV�E�FWBd$4�"�#�DTdT�B�T��4���T�B~i��ZI�[ɎK��i�^K��K��ZI�K��y�Br��&W7��FV�E��6�F���d$4�"��DTdT�B�T��4���T�B~y��ZI�[ɎK��K��iK�i[r��6�76�f�6F�����WfV���B$t��BT�4�t�TBDTdT�B�T��4���T�B~[�Y��K��yJ�y�N���y�Br��7FGW2D�唔�BT�4�t�TB��B�T��DTdT�B4���T�B~K�K���ɣ�i�~z��Xh���z�K��j8X����.z�ih~i��Y�K�K�����3�ih~i��K�K��[��r��f�Ɩ�u�FFRDDRDTdT�B�T��4���T�B~K�K��X{�y>Xˢr��6��6U�FFRDDRDTdT�B�T��4���T�B~j8j8X{�y>Xˢr��'&�Ve�FW67&�F���DU�BDTdT�B�T��4���T�B~z�K��h�kX�K�"r����FƖ�u�FWE��B$�t��BT�4�t�TBDTdT�B�T��4���T�B~K�΋��ik�K�i�^K��ZI�i[r����FƖ�u�W6W%��B$t��BT�4�t�TBDTdT�B�T��4���T�B~K�΋��ik�K��i�^Y��K��[��r��7&VFU�F��RDDUD��R��B�T��DTdT�B5U%$T�E�D��U5D�4���T�B~i��K�N[��K��r��WFFU�F��RDDUD��R��B�T��DTdT�B5U%$T�E�D��U5D���UDDR5U%$T�E�D��U5D�4���T�B~y�nh�Y��K��r��$��%��U���B���T�TR�U�V��66U�6�FR�66U�6�FR����U��G��66U���R�66U���R����U��G��66U�G�R�66U�G�R����U��G��7FGW2�7FGW2����U��G��&W7��FV�E���R�&W7��FV�E���R����U��G����FƖ�u�FWE��B���FƖ�u�FWE��B����U��G����FƖ�u�W6W%��B���FƖ�u�W6W%��B����U��G��f�Ɩ�u�FFR�f�Ɩ�u�FFR���T�t��SԖ���D"DTdT�B4�%4UC�WFc��#B4���DS�WFc��#E�V�6�FU�6�4���T�C�~K��K�>K�nK�K��K�>i��Y�K��s�����"�"��zi��ZI�i[K�nK�K��K�>K�`ФE$�D$�R�bU��5E266U�'G���5$TDRD$�R66U�'G����B$t��BT�4�t�TB��B�T��UD����5$T�T�B4���T�B~ikz��Br��66U��B$t��BT�4�t�TB��B�T��4���T�B~i��ZI�i[X��Y�t���7FFRW6R��FV�ƖvV�E�G&�ð��DdD��BU��5E266U�'G���5$TDRD$�R66U�'G����B$t��BT�4�t�TB��B�T��UD����5$T�T�B4���T�B~ikz��Br��66U��B$t��BT�4�t�TB��B�T��4���T�B~i��ZI�i[X��Y�K�r��'G����Rd$4�"����B�T��4���T�B~i��ZInyI�K��r��'G��G�RD�唔�BT�4�t�TB��B�T��4���T�B~Y��K��K��X��Z�n�ɣދ��yJ�K��iK�i[�#�X�~K��K��[��y�B�>y��Y�I�K��[��y�B�C�K�X�r��vV�FW"D�唔�BT�4�t�TBDTdT�B�T��4���T�B~ZIn�ɣ�Xh��y�Br���E��V�&W"d$4�"�S�DTdT�B�T��4���T�B~k>[��K�K���K��K�>iȞ��r��FWBd$4�"�#�DTdT�B�T��4���T�B~j�N�xފz2r���6�F���d$4�"��DTdT�B�T��4���T�B~K�K��r�����Rd$4�"�#�DTdT�B�T��4���T�B~ZIޘ���K��K�Rr��&V�F���d$4�"�#�DTdT�B�T��4���T�B~i��ZI�ih~K��ZI�K��y�Ny�Br��7&VFU�F��RDDUD��R��B�T��DTdT�B5U%$T�E�D��U5D�4���T�B~i��K�N[��K��r��WFFU�F��RDDUD��R��B�T��DTdT�B5U%$T�E�D��U5D���UDDR5U%$T�E�D��U5D�4���T�B~y�nh�Y��K��r��$��%��U���B����U��G��66U��B�66U��B����U��G��'G��G�R�'G��G�R����U��G��'G����R�'G����R���T�t��SԖ���D"DTdT�B4�%4UC�WFc��#B4���DS�WFc��#E�V�6�FU�6�4���T�C�~K��K�>K�K��K��K�>K�bs�����"�2��zi��ZI�i[X��i[K�K��K�nY�ZI�ikY�K�K��K��K�`ФE$�D$�R�bU��5E266U�f���F����f7C��5$TDRD$�R66U�f���F����f7B���B$t��BT�4�t�TB��B�T��UD����5$T�T�B4���T�B~ikz��Br��66U��B$t��BT�4�t�TB��B�T��4���T�B~i��ZI�i[X��Y�K�r��f7E�F�F�Rd$4�"�#���B�T��4���T�B~K�K����NK�>K��y�Br��f7E�6��FV�BDU�BDTdT�B�T��4���T�B~K�K��K��ihrr��f���F����G�Rd$4�"��DTdT�B�T��4���T�B~i��ZJnK��j�z�Br���67W'&VE�FFRDDRDTdT�B�T��4���T�B~iky�y�NK��[��r����V�BDT4��"�"�DTdT�B�T��4���T�B~K��yJ�h����ZكB�~k�r��Wf�FV�6RDU�BDTdT�B�T��4���T�B~��NX�X�����r��6�'B��B��B�T��DTdT�B4���T�B~z�iȒr��7&VFU�F��RDDUD��R��B�T��DTdT�B5U%$T�E�D��U5D�4���T�B~i��K�N[��K��r��WFFU�F��RDDUD��R��B�T��DTdT�B5U%$T�E�D��U5D���UDDR5U%$T�E�D��U5D�4���T�B~y�nh�Y��K��r��$��%��U���B����U��G��66U��B�66U��B����U��G��f���F����G�R�f���F����G�R����U��G��6�'B�6�'B���T�t��SԖ���D"DTdT�B4�%4UC�WFc��#B4���DS�WFc��#E�V�6�FU�6�4���T�C�~I��ZJnX��Y�j��X�~K��K��Z��Y�s�
-- ==========================================
-- 12. 案件管理模块
-- ==========================================

-- 12.1 案件信息表
DROP TABLE IF EXISTS case_info;
CREATE TABLE case_info (
    id                      BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    case_code               VARCHAR(50)       NOT NULL COMMENT '案件编号',
    case_name               VARCHAR(200)      NOT NULL COMMENT '案件名称',
    case_type               TINYINT UNSIGNED  NOT NULL COMMENT '案件类型：1=违纪, 2=违法, 3=职务犯罪',
    case_source             VARCHAR(100)      DEFAULT NULL COMMENT '案件来源',
    respondent_name         VARCHAR(100)      DEFAULT NULL COMMENT '被调查人姓名',
    respondent_dept         VARCHAR(200)      DEFAULT NULL COMMENT '被调查人单位',
    respondent_position     VARCHAR(100)      DEFAULT NULL COMMENT '被调查人职务',
    classification_level_id BIGINT UNSIGNED   DEFAULT NULL COMMENT '密级ID',
    status                  TINYINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '状态：0=草稿, 1=审理中, 2=已完结, 3=已归档',
    filing_date             DATE              DEFAULT NULL COMMENT '立案日期',
    close_date              DATE              DEFAULT NULL COMMENT '结案日期',
    brief_description       TEXT              DEFAULT NULL COMMENT '简要案情',
    handling_dept_id        BIGINT UNSIGNED   DEFAULT NULL COMMENT '承办部门ID',
    handling_user_id        BIGINT UNSIGNED   DEFAULT NULL COMMENT '承办人ID',
    create_time             DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time             DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_case_code (case_code),
    KEY idx_case_name (case_name),
    KEY idx_case_type (case_type),
    KEY idx_status (status),
    KEY idx_respondent_name (respondent_name),
    KEY idx_handling_dept_id (handling_dept_id),
    KEY idx_handling_user_id (handling_user_id),
    KEY idx_filing_date (filing_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='案件信息表';

-- 12.2 案件当事人表
DROP TABLE IF EXISTS case_party;
CREATE TABLE case_party (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    case_id         BIGINT UNSIGNED   NOT NULL COMMENT '关联案件ID',
    party_name      VARCHAR(100)      NOT NULL COMMENT '当事人姓名',
    party_type      TINYINT UNSIGNED  NOT NULL COMMENT '类型：1=被调查人, 2=证人, 3=举报人, 4=其他',
    gender          TINYINT UNSIGNED  DEFAULT NULL COMMENT '性别：0=女, 1=男',
    id_number       VARCHAR(50)       DEFAULT NULL COMMENT '身份证号',
    dept            VARCHAR(200)      DEFAULT NULL COMMENT '所在单位',
    position        VARCHAR(100)      DEFAULT NULL COMMENT '职务',
    phone           VARCHAR(20)       DEFAULT NULL COMMENT '联系电话',
    relation        VARCHAR(200)      DEFAULT NULL COMMENT '与案件关系',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_case_id (case_id),
    KEY idx_party_type (party_type),
    KEY idx_party_name (party_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='案件当事人表';

-- 12.3 案件违纪事实表
DROP TABLE IF EXISTS case_violation_fact;
CREATE TABLE case_violation_fact (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    case_id         BIGINT UNSIGNED   NOT NULL COMMENT '关联案件ID',
    fact_title      VARCHAR(200)      NOT NULL COMMENT '事实标题',
    fact_content    TEXT              DEFAULT NULL COMMENT '事实内容',
    violation_type  VARCHAR(100)      DEFAULT NULL COMMENT '违纪类型',
    occurred_date   DATE              DEFAULT NULL COMMENT '发生时间',
    amount          DECIMAL(12,2)     DEFAULT NULL COMMENT '涉及金额',
    evidence        TEXT              DEFAULT NULL COMMENT '证据材料',
    sort            INT               NOT NULL DEFAULT 0 COMMENT '排序',
    create_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_case_id (case_id),
    KEY idx_violation_type (violation_type),
    KEY idx_sort (sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='案件违纪事实表';
