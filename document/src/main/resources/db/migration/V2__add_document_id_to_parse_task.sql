-- Migration: Add document_id to doc_parse_task table
-- Purpose: Link parse tasks to their auto-created repo_document records
-- Date: 2026-05-17

ALTER TABLE doc_parse_task
    ADD COLUMN document_id BIGINT NULL COMMENT '关联的库文档ID（解析完成后自动创建的 repo_document 记录')
    AFTER update_time;
