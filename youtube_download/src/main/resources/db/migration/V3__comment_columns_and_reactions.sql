-- =============================================================================
-- V3: 컬럼 역할과 반응 횟수
-- -----
-- 목적
-- - 컬럼 역할을 파일 주석이 아니라 테이블 COMMENT로 남긴다.
-- - 저장된 영상의 좋아요, 싫어요, 조회 수를 센다.
-- 대상
-- - download 전체 컬럼
-- - download.like_count, dislike_count, view_count
-- 주의
-- - V1은 이미 적용되어 수정하지 않는다.
-- - 애플리케이션이 이 테이블을 DROP하거나 TRUNCATE하지 않는다.
-- - 횟수는 저장이 끝난 SAVED 행에만 올린다.
-- =============================================================================

ALTER TABLE download
    MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '저장 요청 번호',
    MODIFY COLUMN video_id CHAR(11) NOT NULL COMMENT '유튜브 영상 ID 11자. 공유 주소의 si는 버린다',
    MODIFY COLUMN watch_url VARCHAR(80) NOT NULL COMMENT '정규화된 시청 주소',
    MODIFY COLUMN title VARCHAR(300) NULL COMMENT '저장 파일에서 읽은 제목',
    MODIFY COLUMN status VARCHAR(20) NOT NULL COMMENT 'REQUESTED, SAVING, SAVED, FAILED',
    MODIFY COLUMN bucket VARCHAR(80) NULL COMMENT 'MinIO 버킷 이름',
    MODIFY COLUMN object_key VARCHAR(300) NULL COMMENT 'MinIO 객체 키. 파일 바이트는 여기 두지 않는다',
    MODIFY COLUMN file_name VARCHAR(300) NULL COMMENT '받은 파일 이름',
    MODIFY COLUMN size_bytes BIGINT NULL COMMENT '파일 크기(바이트)',
    MODIFY COLUMN error_message VARCHAR(500) NULL COMMENT '저장에 실패했을 때의 이유',
    MODIFY COLUMN created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '요청을 만든 시각',
    MODIFY COLUMN updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '상태나 반응이 바뀐 시각',
    MODIFY COLUMN embedding_json VARCHAR(2000) NOT NULL DEFAULT '' COMMENT '제목, 파일명, 영상 ID를 쉼표로 이은 64차원 벡터',
    ADD COLUMN like_count INT NOT NULL DEFAULT 0 COMMENT '좋아요를 누른 횟수',
    ADD COLUMN dislike_count INT NOT NULL DEFAULT 0 COMMENT '싫어요를 누른 횟수',
    ADD COLUMN view_count INT NOT NULL DEFAULT 0 COMMENT '조회한 횟수';
