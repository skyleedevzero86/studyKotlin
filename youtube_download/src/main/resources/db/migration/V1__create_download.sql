-- =============================================================================
-- V1: 다운로드 요청 테이블
-- -----
-- 목적
-- - 붙여 넣은 유튜브 주소를 저장 요청으로 남긴다.
-- - 상태 변경은 Debezium이 읽고, 목록은 Elasticsearch가 페이지로 보여 준다.
-- 대상
-- - download
-- 주의
-- - 파일 바이트는 두지 않는다. 파일은 MinIO object_key로만 가리킨다.
-- - 이 테이블을 애플리케이션이 DROP하거나 TRUNCATE하지 않는다.
-- =============================================================================

CREATE TABLE download (
    id BIGINT NOT NULL AUTO_INCREMENT,
    video_id CHAR(11) NOT NULL,
    watch_url VARCHAR(80) NOT NULL,
    title VARCHAR(300) NULL,
    status VARCHAR(20) NOT NULL,
    bucket VARCHAR(80) NULL,
    object_key VARCHAR(300) NULL,
    file_name VARCHAR(300) NULL,
    size_bytes BIGINT NULL,
    error_message VARCHAR(500) NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_download_video (video_id),
    KEY idx_download_status (status)
);
