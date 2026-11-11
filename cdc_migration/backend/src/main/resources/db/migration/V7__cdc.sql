-- =====================================================================================
-- Flyway 마이그레이션: CDC 스키마
-- 프로젝트: CDC Migration Practice (cdc_migration)
-- 마이그레이션 버전: V7
-- 작성일: 2026-11-11
-- 설명: Debezium 실행 단위, 테이블별 스냅샷 진행, 이벤트 처리 기록 테이블을 만든다
-- =====================================================================================

-- =====================================================================================
-- 1. CDC 실행 (cdc_run)
-- 설명: Debezium 커넥터 한 번 실행 단위. 초기 스냅샷부터 변경분 반영까지의 진행 상태
-- =====================================================================================
CREATE TABLE cdc_run (
    cdc_run_id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, -- CDC 실행 ID (자동증가)
    migration_run_id      BIGINT       NOT NULL REFERENCES migration_run (id), -- 초기 적재에 쓰는 이관 작업 ID
    topic_prefix          VARCHAR(50)  NOT NULL UNIQUE,                -- Debezium 토픽 접두사 (legacy_r{ID})
    connector_name        VARCHAR(100) NOT NULL,                       -- Debezium 커넥터 이름
    status                VARCHAR(20)  NOT NULL,                       -- 진행 상태 (SNAPSHOTTING:스냅샷 수신, LOADING:초기 적재, STREAMING:변경분 반영, STOPPED:중지, FAILED:실패)
    snapshot_completed    BOOLEAN      NOT NULL DEFAULT false,         -- Debezium 스냅샷 완료 알림 수신 여부
    snapshot_completed_at TIMESTAMPTZ,                                 -- 스냅샷 완료 알림 수신일시
    initial_load_at       TIMESTAMPTZ,                                 -- 초기 적재 성공일시
    last_error            TEXT,                                        -- 마지막 실패 사유
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),         -- 생성일시
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT now()          -- 수정일시
);

-- CDC 실행 테이블 코멘트
COMMENT ON TABLE cdc_run IS 'Debezium 커넥터 한 번 실행 단위. 초기 스냅샷부터 변경분 반영까지의 진행 상태';
COMMENT ON COLUMN cdc_run.cdc_run_id IS 'CDC 실행 ID (자동증가)';
COMMENT ON COLUMN cdc_run.migration_run_id IS '초기 적재에 쓰는 이관 작업 ID';
COMMENT ON COLUMN cdc_run.topic_prefix IS 'Debezium 토픽 접두사 (legacy_r{ID})';
COMMENT ON COLUMN cdc_run.connector_name IS 'Debezium 커넥터 이름';
COMMENT ON COLUMN cdc_run.status IS '진행 상태 (SNAPSHOTTING:스냅샷 수신, LOADING:초기 적재, STREAMING:변경분 반영, STOPPED:중지, FAILED:실패)';
COMMENT ON COLUMN cdc_run.snapshot_completed IS 'Debezium 스냅샷 완료 알림 수신 여부';
COMMENT ON COLUMN cdc_run.snapshot_completed_at IS '스냅샷 완료 알림 수신일시';
COMMENT ON COLUMN cdc_run.initial_load_at IS '초기 적재 성공일시';
COMMENT ON COLUMN cdc_run.last_error IS '마지막 실패 사유';
COMMENT ON COLUMN cdc_run.created_at IS '생성일시';
COMMENT ON COLUMN cdc_run.updated_at IS '수정일시';


-- =====================================================================================
-- 2. CDC 스냅샷 테이블 진행 (cdc_snapshot_table)
-- 설명: Debezium 테이블별 스캔 완료 알림과 스캔 건수
-- =====================================================================================
CREATE TABLE cdc_snapshot_table (
    cdc_run_id      BIGINT      NOT NULL REFERENCES cdc_run (cdc_run_id), -- CDC 실행 ID
    table_name      VARCHAR(50) NOT NULL,                              -- 구시스템 테이블명
    expected_rows   BIGINT,                                            -- Debezium이 알린 스캔 건수
    scan_completed  BOOLEAN     NOT NULL DEFAULT false,                -- 테이블 스캔 완료 알림 수신 여부
    PRIMARY KEY (cdc_run_id, table_name)                               -- 기본키
);

-- CDC 스냅샷 테이블 진행 테이블 코멘트
COMMENT ON TABLE cdc_snapshot_table IS 'Debezium 테이블별 스캔 완료 알림과 스캔 건수';
COMMENT ON COLUMN cdc_snapshot_table.cdc_run_id IS 'CDC 실행 ID';
COMMENT ON COLUMN cdc_snapshot_table.table_name IS '구시스템 테이블명';
COMMENT ON COLUMN cdc_snapshot_table.expected_rows IS 'Debezium이 알린 스캔 건수';
COMMENT ON COLUMN cdc_snapshot_table.scan_completed IS '테이블 스캔 완료 알림 수신 여부';


-- =====================================================================================
-- 3. CDC 이벤트 처리 기록 (cdc_event)
-- 설명: 받은 이벤트와 처리 결과. 대상 데이터 변경과 같은 트랜잭션에 기록해 중복 수신을 막는다
-- =====================================================================================
CREATE TABLE cdc_event (
    event_id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,  -- 이벤트 처리 순번 (자동증가)
    cdc_run_id       BIGINT       NOT NULL REFERENCES cdc_run (cdc_run_id), -- CDC 실행 ID
    event_key        VARCHAR(300) NOT NULL,                            -- 중복 판별 키 (작업·테이블·원본 키·LSN)
    topic            VARCHAR(200) NOT NULL,                            -- Kafka 토픽
    kafka_partition  INT          NOT NULL,                            -- Kafka 파티션
    kafka_offset     BIGINT       NOT NULL,                            -- Kafka 오프셋
    table_name       VARCHAR(50)  NOT NULL,                            -- 구시스템 테이블명
    source_key       VARCHAR(200),                                     -- 원본 키 (기관 코드/구 번호, 화면 표시용)
    op               CHAR(1)      NOT NULL,                            -- Debezium 작업 (r:스냅샷, c:등록, u:수정, d:삭제)
    snapshot         BOOLEAN      NOT NULL,                            -- 초기 스냅샷 이벤트 여부
    source_lsn       BIGINT,                                           -- 원본 WAL LSN
    payload          JSONB        NOT NULL,                            -- 이벤트 원문 (before, after)
    status           VARCHAR(20)  NOT NULL CHECK (status IN ('STAGED', 'PENDING', 'APPLIED', 'WAITING', 'SKIPPED', 'FAILED')), -- 처리 상태 (STAGED:스테이징 적재, PENDING:초기 적재 대기, APPLIED:반영, WAITING:재처리 대기, SKIPPED:건너뜀, FAILED:실패)
    attempts         INT          NOT NULL DEFAULT 0,                  -- 처리 시도 횟수
    duplicate_count  INT          NOT NULL DEFAULT 0,                  -- 같은 이벤트를 다시 받은 횟수
    last_error       TEXT,                                             -- 마지막 처리 사유
    received_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),              -- 수신일시
    applied_at       TIMESTAMPTZ,                                      -- 반영일시
    CONSTRAINT uq_cdc_event UNIQUE (cdc_run_id, event_key)             -- 원본 식별자 유니크 제약
);

-- CDC 이벤트 처리 기록 테이블 코멘트
COMMENT ON TABLE cdc_event IS '받은 이벤트와 처리 결과. 대상 데이터 변경과 같은 트랜잭션에 기록해 중복 수신을 막는다';
COMMENT ON COLUMN cdc_event.event_id IS '이벤트 처리 순번 (자동증가)';
COMMENT ON COLUMN cdc_event.cdc_run_id IS 'CDC 실행 ID';
COMMENT ON COLUMN cdc_event.event_key IS '중복 판별 키 (작업·테이블·원본 키·LSN)';
COMMENT ON COLUMN cdc_event.topic IS 'Kafka 토픽';
COMMENT ON COLUMN cdc_event.kafka_partition IS 'Kafka 파티션';
COMMENT ON COLUMN cdc_event.kafka_offset IS 'Kafka 오프셋';
COMMENT ON COLUMN cdc_event.table_name IS '구시스템 테이블명';
COMMENT ON COLUMN cdc_event.source_key IS '원본 키 (기관 코드/구 번호, 화면 표시용)';
COMMENT ON COLUMN cdc_event.op IS 'Debezium 작업 (r:스냅샷, c:등록, u:수정, d:삭제)';
COMMENT ON COLUMN cdc_event.snapshot IS '초기 스냅샷 이벤트 여부';
COMMENT ON COLUMN cdc_event.source_lsn IS '원본 WAL LSN';
COMMENT ON COLUMN cdc_event.payload IS '이벤트 원문 (before, after)';
COMMENT ON COLUMN cdc_event.status IS '처리 상태 (STAGED:스테이징 적재, PENDING:초기 적재 대기, APPLIED:반영, WAITING:재처리 대기, SKIPPED:건너뜀, FAILED:실패)';
COMMENT ON COLUMN cdc_event.attempts IS '처리 시도 횟수';
COMMENT ON COLUMN cdc_event.duplicate_count IS '같은 이벤트를 다시 받은 횟수';
COMMENT ON COLUMN cdc_event.last_error IS '마지막 처리 사유';
COMMENT ON COLUMN cdc_event.received_at IS '수신일시';
COMMENT ON COLUMN cdc_event.applied_at IS '반영일시';


-- 인덱스: CDC 실행·상태별 이벤트를 처리 순서대로 조회
CREATE INDEX ix_cdc_event_status ON cdc_event (cdc_run_id, status, event_id);
