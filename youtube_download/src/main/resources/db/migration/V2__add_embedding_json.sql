-- =============================================================================
-- V2: 다운로드 임베딩
-- -----
-- 목적
-- - 제목과 파일명을 해시 임베딩으로 남겨 코사인 kNN에 쓴다.
-- - Elasticsearch vectordb_document는 벡터를 _source에 남기지 않는다.
-- 대상
-- - download.embedding_json
-- 주의
-- - 값은 쉼표로 이은 float 64개다.
-- =============================================================================

ALTER TABLE download
    ADD COLUMN embedding_json VARCHAR(2000) NOT NULL DEFAULT '';
