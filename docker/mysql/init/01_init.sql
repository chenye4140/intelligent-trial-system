-- 智能审理系统 数据库初始化脚本
-- 数据库: intelligent_trial

-- 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(加密)',
    `real_name` VARCHAR(50) COMMENT '真实姓名',
    `phone` VARCHAR(20) COMMENT '手机号',
    `email` VARCHAR(100) COMMENT '邮箱',
    `department` VARCHAR(100) COMMENT '所属部门',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `role_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `description` VARCHAR(200) COMMENT '角色描述',
    `status` TINYINT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 权限表
CREATE TABLE IF NOT EXISTS `sys_permission` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `permission_code` VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    `permission_name` VARCHAR(100) NOT NULL COMMENT '权限名称',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父权限ID',
    `type` VARCHAR(20) COMMENT '类型: menu-菜单, button-按钮',
    `sort_order` INT DEFAULT 0,
    `status` TINYINT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `role_id` BIGINT NOT NULL,
    `permission_id` BIGINT NOT NULL,
    UNIQUE KEY `uk_role_perm` (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 文档表(法规库/资料库/裁判文书库/案例库统一)
CREATE TABLE IF NOT EXISTS `doc_document` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(500) NOT NULL COMMENT '文档标题',
    `doc_type` VARCHAR(50) NOT NULL COMMENT '文档类型: regulation/material/judgment/case',
    `category_path` VARCHAR(1000) COMMENT '分类目录路径',
    `publish_date` DATETIME COMMENT '发布日期',
    `publish_unit` VARCHAR(200) COMMENT '发布单位',
    `document_number` VARCHAR(100) COMMENT '文号',
    `security_level` VARCHAR(50) DEFAULT 'internal' COMMENT '密级: top_secret/secret/confidential/internal/public',
    `validity_status` VARCHAR(50) DEFAULT 'valid' COMMENT '有效性: valid/invalid/draft',
    `expiry_date` DATETIME COMMENT '失效日期',
    `replaced_by` BIGINT COMMENT '替代文档ID',
    `storage_path` VARCHAR(500) COMMENT 'MinIO存储路径',
    `file_type` VARCHAR(20) COMMENT '文件类型',
    `file_size` BIGINT COMMENT '文件大小(字节)',
    `parse_status` VARCHAR(50) DEFAULT 'pending' COMMENT '解析状态',
    `content_summary` TEXT COMMENT '内容摘要',
    `structured_data` JSON COMMENT '结构化JSON数据',
    `vector_id` VARCHAR(100) COMMENT 'Milvus向量ID',
    `created_by` BIGINT COMMENT '创建人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_doc_type` (`doc_type`),
    INDEX `idx_security` (`security_level`),
    INDEX `idx_validity` (`validity_status`),
    FULLTEXT INDEX `ft_title_content` (`title`, `content_summary`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档表';

-- 文档分类目录表
CREATE TABLE IF NOT EXISTS `doc_category` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `library_type` VARCHAR(50) NOT NULL COMMENT '所属库类型',
    `name` VARCHAR(200) NOT NULL COMMENT '目录名称',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父目录ID',
    `level` INT DEFAULT 1 COMMENT '层级深度',
    `sort_order` INT DEFAULT 0 COMMENT '排序序号',
    `security_level` VARCHAR(50) DEFAULT 'internal' COMMENT '密级',
    `created_by` BIGINT,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_library_parent` (`library_type`, `parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档分类目录表';

-- 操作审计日志表
CREATE TABLE IF NOT EXISTS `sys_audit_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT COMMENT '操作用户',
    `action` VARCHAR(100) NOT NULL COMMENT '操作类型',
    `target_type` VARCHAR(50) COMMENT '目标类型',
    `target_id` BIGINT COMMENT '目标ID',
    `detail` JSON COMMENT '操作详情',
    `ip_address` VARCHAR(50) COMMENT 'IP地址',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作审计日志表';

-- 初始化默认数据
-- 默认管理员 (密码: admin123, 实际应加密)
INSERT IGNORE INTO `sys_user` (`id`, `username`, `password`, `real_name`, `status`) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 1);

-- 默认角色
INSERT IGNORE INTO `sys_role` (`id`, `role_code`, `role_name`, `description`) VALUES
(1, 'ADMIN', '系统管理员', '拥有所有权限'),
(2, 'USER', '普通用户', '基本浏览和查询权限'),
(3, 'REVIEWER', '审理人员', '可上传和编辑文档'),
(4, 'APPROVER', '审批人员', '可审批和处分执行');

-- 管理员关联角色
INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);

-- 初始化默认目录
INSERT IGNORE INTO `doc_category` (`id`, `library_type`, `name`, `parent_id`, `level`, `sort_order`) VALUES
(1, 'regulation', '党内法规', 0, 1, 1),
(2, 'regulation', '国家法律', 0, 1, 2),
(3, 'regulation', '行政法规', 0, 1, 3),
(4, 'material', '政策文件', 0, 1, 1),
(5, 'material', '学习资料', 0, 1, 2),
(6, 'judgment', '刑事裁判', 0, 1, 1),
(7, 'judgment', '行政处分', 0, 1, 2),
(8, 'case', '典型案例', 0, 1, 1),
(9, 'case', '指导案例', 0, 1, 2);
