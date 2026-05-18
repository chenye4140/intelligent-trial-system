-- Test data for report module integration tests

DELETE FROM report_record;
DELETE FROM report_template;
DELETE FROM case_info;

-- Seed data: 案件信息
INSERT INTO case_info (id, case_code, case_name, case_type, case_source, respondent_name, respondent_dept, respondent_position, status, filing_date, brief_description) VALUES
(1, 'AJ20260501001', '张三受贿案', 1, '上级交办', '张三', '某市财政局', '局长', 1, '2026-01-15', '张三在任职期间利用职务便利收受他人贿赂共计50万元，为其在工程承揽方面谋取利益。');

INSERT INTO case_info (id, case_code, case_name, case_type, case_source, respondent_name, respondent_dept, respondent_position, status, filing_date, brief_description) VALUES
(2, 'AJ20260501002', '李四违纪案', 1, '群众举报', '李四', '某县教育局', '副局长', 1, '2026-03-01', '李四违反中央八项规定精神，多次公款旅游、违规吃喝。');

-- Seed data: 文书模板
INSERT INTO report_template (id, template_code, template_name, template_type, content, description, status) VALUES
(1, 'SHENLI_REPORT', '审理报告', 1, '一、被调查人基本情况\n二、问题线索来源及初核情况\n三、经审查认定的违纪违法事实\n四、被调查人的态度和认识\n五、处理建议', '标准审理报告模板', 1);

INSERT INTO report_template (id, template_code, template_name, template_type, content, description, status) VALUES
(2, 'CHUFEN_DECISION', '处分决定', 2, '一、被处分人基本情况\n二、违纪违法事实\n三、处分依据\n四、处分决定', '标准处分决定模板', 1);

INSERT INTO report_template (id, template_code, template_name, template_type, content, description, status) VALUES
(3, 'CHUHE_REPORT', '初核报告', 4, '一、线索来源\n二、初核过程\n三、初核结果\n四、处理建议', '标准初核报告模板', 1);

-- Seed data: 文书记录
INSERT INTO report_record (id, case_id, case_code, template_id, template_code, report_title, report_content, generated_by, status) VALUES
(1, 1, 'AJ20260501001', 1, 'SHENLI_REPORT', '张三受贿案 - 审理报告', '经审理查明：张三在担任某市财政局局长期间...', 1, 1);

INSERT INTO report_record (id, case_id, case_code, template_id, template_code, report_title, generated_by, status) VALUES
(2, 1, 'AJ20260501001', 2, 'CHUFEN_DECISION', '张三受贿案 - 处分决定', 1, 0);

INSERT INTO report_record (id, case_id, case_code, template_id, template_code, report_title, error_message, generated_by, status) VALUES
(3, 2, 'AJ20260501002', 1, 'SHENLI_REPORT', '李四违纪案 - 审理报告', 'AI服务调用超时', 1, 2);
