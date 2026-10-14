-- =============================================================================
-- V1: MSA 다운로드 원장
-- -----
-- 목적
-- - 붙여 넣은 유튜브 주소를 저장 요청으로 남긴다.
-- - 벡터와 좋아요, 싫어요, 조회 수를 같은 행에 둔다.
-- 대상
-- - download
-- 주의
-- - 파일 바이트는 두지 않는다. 파일은 MinIO object_key로만 가리킨다.
-- - youtube_download 모놀리스 테이블과 다른 데이터베이스다.
-- - 이 테이블을 애플리케이션이 DROP하거나 TRUNCATE하지 않는다.
-- =============================================================================

CREATE TABLE download (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '저장 요청 번호',
    video_id CHAR(11) NOT NULL COMMENT '유튜브 영상 ID 11자. 공유 주소의 si는 버린다',
    watch_url VARCHAR(80) NOT NULL COMMENT '정규화된 시청 주소',
    title VARCHAR(300) NULL COMMENT '저장 파일에서 읽은 제목',
    status VARCHAR(20) NOT NULL COMMENT 'REQUESTED, SAVING, SAVED, FAILED',
    bucket VARCHAR(80) NULL COMMENT 'MinIO 버킷 이름',
    object_key VARCHAR(300) NULL COMMENT 'MinIO 객체 키. 파일 바이트는 여기 두지 않는다',
    file_name VARCHAR(300) NULL COMMENT '받은 파일 이름',
    size_bytes BIGINT NULL COMMENT '파일 크기(바이트)',
    error_message VARCHAR(500) NULL COMMENT '저장에 실패했을 때의 이유',
    embedding_json VARCHAR(2000) NOT NULL DEFAULT '' COMMENT '제목, 파일명, 영상 ID를 쉼표로 이은 64차원 벡터',
    like_count INT NOT NULL DEFAULT 0 COMMENT '좋아요를 누른 횟수',
    dislike_count INT NOT NULL DEFAULT 0 COMMENT '싫어요를 누른 횟수',
    view_count INT NOT NULL DEFAULT 0 COMMENT '조회한 횟수',
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '요청을 만든 시각',
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '상태나 반응이 바뀐 시각',
    PRIMARY KEY (id),
    KEY idx_download_video (video_id),
    KEY idx_download_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
