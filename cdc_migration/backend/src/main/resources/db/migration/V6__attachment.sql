-- =====================================================================================
-- Flyway 마이그레이션: 첨부파일 스키마
-- 프로젝트: CDC Migration Practice (cdc_migration)
-- 마이그레이션 버전: V6
-- 작성일: 2026-11-11
-- 설명: 첨부파일 메타데이터, BYTEA 본문, 구 MinIO 파일 이관 작업 테이블을 만든다
-- =====================================================================================

-- =====================================================================================
-- 1. 첨부파일 (attachment)
-- 설명: 첨부파일 메타데이터. MinIO 객체 위치와 저장 상태를 관리한다 (본문은 attachment_content)
-- =====================================================================================
CREATE TABLE attachment (
    attachment_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, -- 첨부파일 ID (자동증가)
    sub_no            BIGINT       NOT NULL REFERENCES sub_mastinfo (sub_no), -- 새 신청번호
    request_id        VARCHAR(100) NOT NULL UNIQUE,                    -- 업로드 요청 ID (재시도 시 같은 값, 고유)
    origin_inst_code  VARCHAR(10),                                     -- 원본 기관 코드
    origin_file_id    VARCHAR(50),                                     -- 원본 파일 ID
    origin_regno      VARCHAR(30),                                     -- 원본 신청번호
    origin_seq        INT,                                             -- 원본 첨부 순번
    file_name         VARCHAR(300) NOT NULL,                           -- 원본 파일명
    content_type      VARCHAR(200),                                    -- 콘텐츠 타입
    byte_size         BIGINT       NOT NULL,                           -- 바이트 크기
    sha256            CHAR(64)     NOT NULL,                           -- SHA-256 해시 (16진수 64자)
    bucket            VARCHAR(100) NOT NULL,                           -- MinIO 버킷
    object_key        VARCHAR(500) NOT NULL,                           -- MinIO 객체 키
    status            VARCHAR(10)  NOT NULL CHECK (status IN ('PENDING', 'READY', 'FAILED')), -- 저장 상태 (PENDING:저장 중, READY:MinIO와 DB 본문 저장 완료, FAILED:저장 실패)
    last_error        TEXT,                                            -- 마지막 실패 사유
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),             -- 등록일시
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),             -- 수정일시
    CONSTRAINT uq_attachment_origin UNIQUE (origin_inst_code, origin_file_id) -- 원본 식별자 유니크 제약
);

-- 첨부파일 테이블 코멘트
COMMENT ON TABLE attachment IS '첨부파일 메타데이터. MinIO 객체 위치와 저장 상태를 관리한다 (본문은 attachment_content)';
COMMENT ON COLUMN attachment.attachment_id IS '첨부파일 ID (자동증가)';
COMMENT ON COLUMN attachment.sub_no IS '새 신청번호';
COMMENT ON COLUMN attachment.request_id IS '업로드 요청 ID (재시도 시 같은 값, 고유)';
COMMENT ON COLUMN attachment.origin_inst_code IS '원본 기관 코드';
COMMENT ON COLUMN attachment.origin_file_id IS '원본 파일 ID';
COMMENT ON COLUMN attachment.origin_regno IS '원본 신청번호';
COMMENT ON COLUMN attachment.origin_seq IS '원본 첨부 순번';
COMMENT ON COLUMN attachment.file_name IS '원본 파일명';
COMMENT ON COLUMN attachment.content_type IS '콘텐츠 타입';
COMMENT ON COLUMN attachment.byte_size IS '바이트 크기';
COMMENT ON COLUMN attachment.sha256 IS 'SHA-256 해시 (16진수 64자)';
COMMENT ON COLUMN attachment.bucket IS 'MinIO 버킷';
COMMENT ON COLUMN attachment.object_key IS 'MinIO 객체 키';
COMMENT ON COLUMN attachment.status IS '저장 상태 (PENDING:저장 중, READY:MinIO와 DB 본문 저장 완료, FAILED:저장 실패)';
COMMENT ON COLUMN attachment.last_error IS '마지막 실패 사유';
COMMENT ON COLUMN attachment.created_at IS '등록일시';
COMMENT ON COLUMN attachment.updated_at IS '수정일시';


-- 인덱스: 신청별 첨부파일 목록 조회
CREATE INDEX ix_attachment_sub ON attachment (sub_no);

-- =====================================================================================
-- 2. 첨부파일 본문 (attachment_content)
-- 설명: 첨부파일 전체 바이트를 BYTEA로 보관하는 테이블. 목록·상세 조회에서는 읽지 않는다
-- =====================================================================================
CREATE TABLE attachment_content (
    attachment_id  BIGINT      PRIMARY KEY REFERENCES attachment (attachment_id) ON DELETE CASCADE, -- 첨부파일 ID
    file_content   BYTEA       NOT NULL,                               -- 파일 전체 바이트
    saved_at       TIMESTAMPTZ NOT NULL DEFAULT now()                  -- 본문 저장일시
);

-- 첨부파일 본문 테이블 코멘트
COMMENT ON TABLE attachment_content IS '첨부파일 전체 바이트를 BYTEA로 보관하는 테이블. 목록·상세 조회에서는 읽지 않는다';
COMMENT ON COLUMN attachment_content.attachment_id IS '첨부파일 ID';
COMMENT ON COLUMN attachment_content.file_content IS '파일 전체 바이트';
COMMENT ON COLUMN attachment_content.saved_at IS '본문 저장일시';


-- =====================================================================================
-- 3. 첨부파일 이관 묶음 (file_transfer_batch)
-- 설명: 첨부파일 이관 CSV 한 번 업로드 단위
-- =====================================================================================
CREATE TABLE file_transfer_batch (
    batch_id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,       -- 묶음 ID (자동증가)
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),                    -- 생성일시
    total       INT         NOT NULL DEFAULT 0                         -- CSV 행 수
);

-- 첨부파일 이관 묶음 테이블 코멘트
COMMENT ON TABLE file_transfer_batch IS '첨부파일 이관 CSV 한 번 업로드 단위';
COMMENT ON COLUMN file_transfer_batch.batch_id IS '묶음 ID (자동증가)';
COMMENT ON COLUMN file_transfer_batch.created_at IS '생성일시';
COMMENT ON COLUMN file_transfer_batch.total IS 'CSV 행 수';


-- =====================================================================================
-- 4. 첨부파일 이관 작업 (file_transfer)
-- 설명: 구 MinIO 파일을 새 MinIO와 BYTEA로 옮기는 작업 단위와 완료 여부
-- =====================================================================================
CREATE TABLE file_transfer (
    transfer_id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, -- 이관 작업 ID (자동증가)
    source             VARCHAR(10)  NOT NULL CHECK (source IN ('CSV', 'CDC')), -- 작업 생성 경로 (CSV, CDC)
    batch_id           BIGINT REFERENCES file_transfer_batch (batch_id), -- CSV 묶음 ID
    cdc_run_id         BIGINT,                                         -- CDC 실행 ID
    inst_code          VARCHAR(10)  NOT NULL,                          -- 원본 기관 코드
    legacy_file_id     VARCHAR(50)  NOT NULL,                          -- 원본 파일 ID
    legacy_regno       VARCHAR(30),                                    -- 구 신청번호
    seq                INT,                                            -- 원본 첨부 순번
    file_name          VARCHAR(300),                                   -- 원본 파일명
    content_type       VARCHAR(200),                                   -- 콘텐츠 타입
    source_bucket      VARCHAR(100) NOT NULL,                          -- 구 MinIO 버킷
    source_object_key  VARCHAR(500) NOT NULL,                          -- 구 MinIO 객체 키
    status             VARCHAR(20)  NOT NULL CHECK (status IN ('PENDING', 'DONE', 'UNMAPPED', 'FAILED')), -- 작업 상태 (PENDING:대기, DONE:완료, UNMAPPED:신청 매핑 실패, FAILED:실패)
    attachment_id      BIGINT REFERENCES attachment (attachment_id),   -- 생성된 첨부파일 ID
    attempts           INT          NOT NULL DEFAULT 0,                -- 시도 횟수
    last_error         TEXT,                                           -- 마지막 실패 사유
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),            -- 생성일시
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),            -- 수정일시
    completed_at       TIMESTAMPTZ,                                    -- 완료일시
    CONSTRAINT uq_file_transfer_source UNIQUE (inst_code, legacy_file_id, source_object_key) -- 원본 식별자 유니크 제약
);

-- 첨부파일 이관 작업 테이블 코멘트
COMMENT ON TABLE file_transfer IS '구 MinIO 파일을 새 MinIO와 BYTEA로 옮기는 작업 단위와 완료 여부';
COMMENT ON COLUMN file_transfer.transfer_id IS '이관 작업 ID (자동증가)';
COMMENT ON COLUMN file_transfer.source IS '작업 생성 경로 (CSV, CDC)';
COMMENT ON COLUMN file_transfer.batch_id IS 'CSV 묶음 ID';
COMMENT ON COLUMN file_transfer.cdc_run_id IS 'CDC 실행 ID';
COMMENT ON COLUMN file_transfer.inst_code IS '원본 기관 코드';
COMMENT ON COLUMN file_transfer.legacy_file_id IS '원본 파일 ID';
COMMENT ON COLUMN file_transfer.legacy_regno IS '구 신청번호';
COMMENT ON COLUMN file_transfer.seq IS '원본 첨부 순번';
COMMENT ON COLUMN file_transfer.file_name IS '원본 파일명';
COMMENT ON COLUMN file_transfer.content_type IS '콘텐츠 타입';
COMMENT ON COLUMN file_transfer.source_bucket IS '구 MinIO 버킷';
COMMENT ON COLUMN file_transfer.source_object_key IS '구 MinIO 객체 키';
COMMENT ON COLUMN file_transfer.status IS '작업 상태 (PENDING:대기, DONE:완료, UNMAPPED:신청 매핑 실패, FAILED:실패)';
COMMENT ON COLUMN file_transfer.attachment_id IS '생성된 첨부파일 ID';
COMMENT ON COLUMN file_transfer.attempts IS '시도 횟수';
COMMENT ON COLUMN file_transfer.last_error IS '마지막 실패 사유';
COMMENT ON COLUMN file_transfer.created_at IS '생성일시';
COMMENT ON COLUMN file_transfer.updated_at IS '수정일시';
COMMENT ON COLUMN file_transfer.completed_at IS '완료일시';


-- 인덱스: 상태별 이관 작업 조회 (재처리 대상 찾기)
CREATE INDEX ix_file_transfer_status ON file_transfer (status);
