-- Test data for promotion module integration tests

-- Seed data: 3 records with different statuses
INSERT INTO case_promotion (id, case_id, template_id, content, status, user_id, create_time, update_time) VALUES
(1, 'AJ20260501001', 100, 'Draft promotion analysis content for case AJ20260501001', 0, 10, '2026-05-01 10:00:00', '2026-05-01 10:00:00');

INSERT INTO case_promotion (id, case_id, template_id, content, status, user_id, create_time, update_time) VALUES
(2, 'AJ20260501002', 101, 'Pending promotion analysis content for case AJ20260501002', 1, 11, '2026-05-02 14:00:00', '2026-05-02 14:00:00');

INSERT INTO case_promotion (id, case_id, template_id, content, status, user_id, create_time, update_time) VALUES
(3, 'AJ20260501003', 102, 'Approved promotion analysis content for case AJ20260501003', 2, 10, '2026-05-03 09:00:00', '2026-05-03 09:00:00');
