-- Test data for readingnote module integration tests

DELETE FROM reading_note;

-- Seed data: 阅卷笔记
INSERT INTO reading_note (id, case_id, title, content, tags, note_type, is_shared, user_id) VALUES
(1, 'AJ20260501001', '案件事实梳理', '张三，男，1980年出生，2023年任某局局长。经查，2023年1月至2024年6月期间，利用职务便利，收受他人贿赂共计50万元。', '受贿,事实,张三', 1, 0, 1);

INSERT INTO reading_note (id, case_id, title, content, tags, note_type, is_shared, user_id) VALUES
(2, 'AJ20260501001', '证据材料清单', '1.银行流水记录\n2.通话记录\n3.证人证言（李四、王五）\n4.受贿现场照片', '证据,清单', 1, 1, 1);

INSERT INTO reading_note (id, case_id, title, content, tags, note_type, is_shared, user_id) VALUES
(3, 'AJ20260501002', '纪律审查要点', '重点关注违反中央八项规定精神问题，包括公款旅游、违规吃喝等。', '纪律,八项规定', 2, 0, 2);

INSERT INTO reading_note (id, case_id, title, content, tags, note_type, is_shared, user_id) VALUES
(4, 'AJ20260501003', '法规适用分析', '本案涉及《中国共产党纪律处分条例》第二十七条、第二十八条相关规定。', '法规,适用', 3, 1, 1);
