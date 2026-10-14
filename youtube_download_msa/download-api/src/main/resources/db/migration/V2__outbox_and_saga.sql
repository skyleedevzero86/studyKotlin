-- =============================================================================
-- V2: Outbox와 다운로드 사가
-- -----
-- 목적
-- - 원장 변경과 같은 트랜잭션에 전달할 이벤트를 남긴다.
-- - 저장 요청이 어느 단계인지 Debezium이 읽기 전에 확정한다.
-- 대상
-- - outbox, download_saga
-- 주의
-- - outbox 컬럼 이름은 Debezium Outbox Event Router가 읽는 이름이다.
-- - 애플리케이션이 이 테이블을 DROP하거나 TRUNCATE하지 않는다.
-- - download 모놀리스 테이블과는 다른 데이터베이스다.
-- =============================================================================

CREATE TABLE outbox (
    id CHAR(36) NOT NULL COMMENT '이벤트 ID. 소비자가 중복 처리하지 않을 때 쓴다',
    aggregatetype VARCHAR(40) NOT NULL COMMENT 'Debezium이 토픽으로 라우팅하는 집합 이름',
    aggregateid VARCHAR(40) NOT NULL COMMENT '다운로드 번호',
    type VARCHAR(40) NOT NULL COMMENT 'DOWNLOAD_REQUESTED, DOWNLOAD_CLAIMED, DOWNLOAD_SAVED, DOWNLOAD_FAILED, DOWNLOAD_COMPENSATED, DOWNLOAD_REACTED',
    payload VARCHAR(1000) NOT NULL COMMENT '이벤트 ID, 종류, 다운로드 번호, 상태를 담은 JSON',
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '이벤트를 원장과 함께 확정한 시각',
    PRIMARY KEY (id),
    KEY idx_outbox_aggregate (aggregateid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 사가 단계. 파일 업로드 후 원장 반영이 실패하면 COMPENSATED로 남긴다.
CREATE TABLE download_saga (
    download_id BIGINT NOT NULL COMMENT 'download.id와 같은 저장 요청 번호',
    step VARCHAR(20) NOT NULL COMMENT 'REQUESTED, FULFILLING, SAVED, FAILED, COMPENSATED',
    event_id CHAR(36) NOT NULL COMMENT '이 단계를 연 이벤트 ID. 같은 이벤트 재전달이면 저장을 다시 시작한다',
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '단계가 바뀐 시각',
    PRIMARY KEY (download_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
