-- H2 test data for repository module

-- 种子目录数据
INSERT INTO repo_directory (id, repo_type, parent_id, name, sort, permission_scope, path, status) VALUES
(1, 1, 0, '党内法规', 1, 'public', '/1/', 1),
(2, 1, 1, '党章', 1, 'public', '/1/2/', 1),
(3, 1, 1, '准则', 2, 'public', '/1/3/', 1),
(4, 2, 0, '学习资料', 1, 'internal', '/4/', 1),
(5, 2, 4, '培训教材', 1, 'internal', '/4/5/', 1),
(6, 3, 0, '裁判文书', 1, 'restricted', '/6/', 1),
(7, 4, 0, '典型案例', 1, 'public', '/7/', 1);

-- 种子文档数据
INSERT INTO repo_document (id, repo_type, directory_id, title, doc_no, publish_unit, publish_date, validity_status, file_path, file_size, file_type, status) VALUES
(1, 1, 2, '中国共产党章程', '中发〔2022〕1号', '中共中央', '2022-10-22', 'valid', 'docs/party_constitution.pdf', 1024000, 'pdf', 1),
(2, 1, 3, '中国共产党廉洁自律准则', '中发〔2015〕2号', '中共中央', '2016-01-01', 'valid', 'docs/integrity准则.pdf', 512000, 'pdf', 1),
(3, 2, 5, '纪检监察业务培训手册', '', '中央纪委', '2023-06-15', 'valid', 'docs/training_manual.docx', 2048000, 'docx', 1),
(4, 3, 6, '某市纪委关于张某违纪违法案的审理报告', '', '某市纪委', '2024-03-20', 'valid', 'docs/case_report_001.pdf', 768000, 'pdf', 1),
(5, 4, 7, '违反中央八项规定精神典型案例', '', '中央纪委', '2023-12-01', 'valid', 'docs/typical_cases.pdf', 1536000, 'pdf', 1);
