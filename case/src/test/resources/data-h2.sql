-- Test data for casemanage module integration tests

DELETE FROM case_violation_fact;
DELETE FROM case_party;
DELETE FROM case_info;
DELETE FROM sys_dept;
DELETE FROM sys_user;
DELETE FROM sys_classification_level;

-- Seed data for JOIN-dependent tables
INSERT INTO sys_dept (id, dept_name) VALUES (1, '市发改委');
INSERT INTO sys_user (id, username, password, real_name) VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员');
INSERT INTO sys_classification_level (id, level_code, level_name, level_order) VALUES (1, 'SECRET', '秘密', 3);

INSERT INTO case_info (case_code, case_name, case_type, case_source, respondent_name, respondent_dept, respondent_position, status, filing_date, close_date) VALUES
('AJ20260501001', '张三违纪案', 1, '信访举报', '张三', '市发改委', '处长', 1, '2026-01-15', NULL);

INSERT INTO case_info (case_code, case_name, case_type, case_source, respondent_name, respondent_dept, respondent_position, status, filing_date, close_date) VALUES
('AJ20260501002', '李四职务违法案', 2, '巡视移交', '李四', '市财政局', '副局长', 1, '2026-02-20', NULL);

INSERT INTO case_info (case_code, case_name, case_type, case_source, respondent_name, respondent_dept, respondent_position, status, filing_date, close_date) VALUES
('AJ20260501003', '王五受贿案', 3, '上级交办', '王五', '市住建局', '局长', 2, '2025-12-01', '2026-04-15');

INSERT INTO case_party (case_id, party_name, party_type, gender, dept, position, phone, relation) VALUES
(1, '张三', 1, 1, '市发改委', '处长', '13800001111', '被调查人');

INSERT INTO case_party (case_id, party_name, party_type, gender, dept, position, phone, relation) VALUES
(1, '赵六', 2, 1, '市发改委', '科员', '13800002222', '证人');

INSERT INTO case_party (case_id, party_name, party_type, gender, dept, position, phone, relation) VALUES
(2, '李四', 1, 1, '市财政局', '副局长', '13800003333', '被调查人');

INSERT INTO case_party (case_id, party_name, party_type, gender, dept, position, phone, relation) VALUES
(3, '王五', 1, 1, '市住建局', '局长', '13800004444', '被调查人');

INSERT INTO case_violation_fact (case_id, fact_title, fact_content, violation_type, occurred_date, amount, sort) VALUES
(1, '违规收受礼品', '张三在任职期间，违规收受下属单位所送礼品礼金共计人民币 5 万元。', '违反廉洁纪律', '2025-06-15', 50000.00, 1);

INSERT INTO case_violation_fact (case_id, fact_title, fact_content, violation_type, occurred_date, amount, sort) VALUES
(1, '滥用职权', '张三利用职务便利，在项目审批中为他人谋取不正当利益。', '违反工作纪律', '2025-08-20', NULL, 2);

INSERT INTO case_violation_fact (case_id, fact_title, fact_content, violation_type, occurred_date, amount, sort) VALUES
(2, '挪用公款', '李四利用职务便利，挪用财政资金 20 万元用于个人投资。', '违反国家法律法规', '2025-03-10', 200000.00, 1);

INSERT INTO case_violation_fact (case_id, fact_title, fact_content, violation_type, occurred_date, amount, sort) VALUES
(3, '受贿', '王五利用职务便利，在工程招投标中为他人提供帮助，非法收受财物 100 万元。', '职务犯罪', '2025-01-05', 1000000.00, 1);
