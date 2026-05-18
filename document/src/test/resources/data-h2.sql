-- Test data for document module integration tests

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
