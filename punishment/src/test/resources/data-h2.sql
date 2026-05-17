-- Test data for punishment module integration tests

DELETE FROM punishment_material;
DELETE FROM punishment_execution;

-- Seed data: 处分执行记录
INSERT INTO punishment_execution (id, case_id, punishment_type, decision_date, start_date, end_date, status, reminder_flag, is_overdue) VALUES
(1, 'AJ20260501001', '警告', '2026-01-20', '2026-01-20', '2026-07-20', 1, 0, 0);

INSERT INTO punishment_execution (id, case_id, punishment_type, decision_date, start_date, end_date, status, reminder_flag, is_overdue) VALUES
(2, 'AJ20260501002', '记过', '2026-03-01', '2026-03-01', '2026-09-01', 0, 0, 0);

INSERT INTO punishment_execution (id, case_id, punishment_type, decision_date, start_date, end_date, status, reminder_flag, is_overdue) VALUES
(3, 'AJ20260501003', '撤职', '2026-04-20', '2026-04-20', '2026-10-20', 2, 0, 0);

-- Seed data: 处分材料
INSERT INTO punishment_material (id, execution_id, material_type, file_path, uploader_id) VALUES
(1, 1, '决定书', '/files/punishment/decision_001.pdf', 1);

INSERT INTO punishment_material (id, execution_id, material_type, file_path, uploader_id) VALUES
(2, 1, '送达回证', '/files/punishment/receipt_001.pdf', 1);
