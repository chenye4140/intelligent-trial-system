-- Test data for document module integration tests

DELETE FROM incoming_doc WHERE id > 0;
DELETE FROM case_similarity_record WHERE id > 0;
DELETE FROM doc_paragraph_vector WHERE id > 0;
DELETE FROM doc_parse_task WHERE id > 0;

-- Seed data: doc_parse_task (3 rows - completed / processing / failed)

-- Task 1: completed (PDF)
INSERT INTO doc_parse_task (id, file_name, file_path, file_type, status, progress,
    result_json, error_msg, parse_time, vector_count, create_time, update_time)
VALUES (1, 'test_case_001.pdf', 'documents/2026/05/01/test_001.pdf',
        'pdf', 2, 100,
        '{"paragraphs":[{"content":"Defendant Zhang San committed theft on March 15 2025 in a certain district, stealing property worth 5000 RMB.","style":"normal","position":1},{"content":"Upon trial it was found that Defendant Zhang San with the purpose of illegal possession secretly stole others property constituting theft crime.","style":"normal","position":2}],"metadata":{"totalParagraphs":2,"totalPages":3,"totalCharacters":280,"parseDurationMs":15000}}',
        NULL, '2026-05-01 10:00:00', 2, '2026-05-01 09:30:00', '2026-05-01 10:00:00');

-- Task 2: processing (Word)
INSERT INTO doc_parse_task (id, file_name, file_path, file_type, status, progress,
    result_json, error_msg, parse_time, vector_count, create_time, update_time)
VALUES (2, 'test_case_002.docx', 'documents/2026/05/02/test_002.docx',
        'docx', 1, 60,
        NULL, NULL, NULL, 0, '2026-05-02 14:00:00', '2026-05-02 14:30:00');

-- Task 3: failed (Image)
INSERT INTO doc_parse_task (id, file_name, file_path, file_type, status, progress,
    result_json, error_msg, parse_time, vector_count, create_time, update_time)
VALUES (3, 'test_evidence.jpg', 'documents/2026/05/03/test_003.jpg',
        'jpg', 3, 40,
        NULL, 'OCR service call timeout', NULL, 0, '2026-05-03 08:00:00', '2026-05-03 08:05:00');

-- Seed data: doc_paragraph_vector (3 rows - linked to completed task 1)

INSERT INTO doc_paragraph_vector (id, task_id, paragraph_index, content, category,
    law_level, vector_data, vector_dimension, create_time)
VALUES (1, 1, 0, 'Defendant Zhang San committed theft on March 15 2025 in a certain district stealing property worth 5000 RMB.',
        'Case Fact', NULL,
        '[0.12,0.34,0.56,0.78,0.23,0.45,0.67,0.89,0.11,0.33]',
        10, '2026-05-01 10:00:00');

INSERT INTO doc_paragraph_vector (id, task_id, paragraph_index, content, category,
    law_level, vector_data, vector_dimension, create_time)
VALUES (2, 1, 1, 'Upon trial it was found that Defendant Zhang San with the purpose of illegal possession secretly stole others property constituting theft crime.',
        'Processing Opinion', NULL,
        '[0.15,0.37,0.59,0.81,0.26,0.48,0.70,0.92,0.14,0.36]',
        10, '2026-05-01 10:00:00');

INSERT INTO doc_paragraph_vector (id, task_id, paragraph_index, content, category,
    law_level, vector_data, vector_dimension, create_time)
VALUES (3, 1, 2, 'According to Article 264 of the Criminal Law of the PRC the judgment is as follows: Defendant Zhang San guilty of theft sentenced to six months imprisonment.',
        'Legal Basis', 'Article',
        '[0.18,0.40,0.62,0.84,0.29,0.51,0.73,0.95,0.17,0.39]',
        10, '2026-05-01 10:00:00');

-- Seed data: incoming_doc (4 rows - 待处理/处理中/已办结/已归档)

INSERT INTO incoming_doc (id, doc_no, from_unit, title, receive_date, subject, status, create_time, update_time)
VALUES (1, 'DOC-2026-001', '某市政府办公厅', '关于开展安全生产检查的通知',
        '2026-05-01 09:00:00', '安全生产检查工作安排', 0, '2026-05-01 09:00:00', '2026-05-01 09:00:00');

INSERT INTO incoming_doc (id, doc_no, from_unit, title, receive_date, subject, status, create_time, update_time)
VALUES (2, 'DOC-2026-002', '省高级人民法院', '关于印发年度工作要点的函',
        '2026-05-05 14:00:00', '2026年度法院工作重点', 1, '2026-05-05 14:00:00', '2026-05-05 14:00:00');

INSERT INTO incoming_doc (id, doc_no, from_unit, title, receive_date, subject, ocr_content, status, create_time, update_time)
VALUES (3, 'DOC-2026-003', '市发改委', '关于重点项目推进情况的报告',
        '2026-05-10 10:00:00', '重点项目进展汇报', '经OCR识别的项目报告全文内容...', 2, '2026-05-10 10:00:00', '2026-05-10 10:00:00');

INSERT INTO incoming_doc (id, doc_no, from_unit, title, receive_date, subject, status, create_time, update_time)
VALUES (4, 'DOC-2026-004', '市财政局', '关于预算执行情况的通报',
        '2026-05-15 16:00:00', '2026年上半年预算执行分析', 3, '2026-05-15 16:00:00', '2026-05-15 16:00:00');
