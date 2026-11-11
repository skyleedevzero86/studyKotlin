-- =====================================================================================
-- Flyway 마이그레이션: 이관 핵심 스키마
-- 프로젝트: CDC Migration Practice (cdc_migration)
-- 마이그레이션 버전: V1
-- 작성일: 2026-11-11
-- 설명: 이관 작업·스테이징·검증 오류·매핑·코드 대응표와 새 시스템 사용자·과제·심의 신청·심의 이력 테이블을 만든다
-- =====================================================================================

-- =====================================================================================
-- 1. 이관 작업 (migration_run)
-- 설명: CSV 이관과 CDC 초기 적재 작업의 실행 상태와 적재 건수를 관리하는 테이블
-- =====================================================================================
CREATE TABLE migration_run (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, -- 이관 작업 ID (자동증가)
    mode                VARCHAR(10)  NOT NULL CHECK (mode IN ('CSV', 'CDC')), -- 이관 모드 (CSV:CSV 일회성 이관, CDC:Debezium 초기 스냅샷)
    status              VARCHAR(30)  NOT NULL,                         -- 작업 상태 (CREATED:적재 대기, RUNNING:실행 중, COMPLETED:완료, COMPLETED_WITH_ISSUES:검증 오류 있음, FAILED:실패)
    description         VARCHAR(200),                                  -- 작업 설명
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),           -- 작업 생성일시
    started_at          TIMESTAMPTZ,                                   -- 마지막 실행 시작일시
    finished_at         TIMESTAMPTZ,                                   -- 마지막 실행 종료일시
    attempt_count       INT          NOT NULL DEFAULT 0,               -- 실행 횟수 (재처리 포함)
    error_message       TEXT,                                          -- 마지막 실패 사유
    error_count         INT          NOT NULL DEFAULT 0,               -- 검증 오류(ERROR) 건수
    info_count          INT          NOT NULL DEFAULT 0,               -- 검증 안내(INFO) 건수
    inserted_users      INT          NOT NULL DEFAULT 0,               -- 마지막 실행에서 새로 넣은 사용자 수
    inserted_projects   INT          NOT NULL DEFAULT 0,               -- 마지막 실행에서 새로 넣은 과제 수
    inserted_applications INT        NOT NULL DEFAULT 0,               -- 마지막 실행에서 새로 넣은 심의 신청 수
    inserted_reviews    INT          NOT NULL DEFAULT 0                -- 마지막 실행에서 새로 넣은 심의 이력 수
);

-- 이관 작업 테이블 코멘트
COMMENT ON TABLE migration_run IS 'CSV 이관과 CDC 초기 적재 작업의 실행 상태와 적재 건수를 관리하는 테이블';
COMMENT ON COLUMN migration_run.id IS '이관 작업 ID (자동증가)';
COMMENT ON COLUMN migration_run.mode IS '이관 모드 (CSV:CSV 일회성 이관, CDC:Debezium 초기 스냅샷)';
COMMENT ON COLUMN migration_run.status IS '작업 상태 (CREATED:적재 대기, RUNNING:실행 중, COMPLETED:완료, COMPLETED_WITH_ISSUES:검증 오류 있음, FAILED:실패)';
COMMENT ON COLUMN migration_run.description IS '작업 설명';
COMMENT ON COLUMN migration_run.created_at IS '작업 생성일시';
COMMENT ON COLUMN migration_run.started_at IS '마지막 실행 시작일시';
COMMENT ON COLUMN migration_run.finished_at IS '마지막 실행 종료일시';
COMMENT ON COLUMN migration_run.attempt_count IS '실행 횟수 (재처리 포함)';
COMMENT ON COLUMN migration_run.error_message IS '마지막 실패 사유';
COMMENT ON COLUMN migration_run.error_count IS '검증 오류(ERROR) 건수';
COMMENT ON COLUMN migration_run.info_count IS '검증 안내(INFO) 건수';
COMMENT ON COLUMN migration_run.inserted_users IS '마지막 실행에서 새로 넣은 사용자 수';
COMMENT ON COLUMN migration_run.inserted_projects IS '마지막 실행에서 새로 넣은 과제 수';
COMMENT ON COLUMN migration_run.inserted_applications IS '마지막 실행에서 새로 넣은 심의 신청 수';
COMMENT ON COLUMN migration_run.inserted_reviews IS '마지막 실행에서 새로 넣은 심의 이력 수';


-- =====================================================================================
-- 2. 사용자 스테이징 (stg_user)
-- 설명: 구시스템 사용자 원본 값을 작업별로 그대로 보관하는 이관용 테이블
-- =====================================================================================
CREATE TABLE stg_user (
    migration_run_id  BIGINT NOT NULL REFERENCES migration_run (id) ON DELETE CASCADE, -- 이관 작업 ID
    row_no            INT    NOT NULL,                                 -- 원본 행 번호 (작업 안에서 순번)
    inst_code         TEXT,                                            -- 원본 기관 코드
    legacy_user_id    TEXT,                                            -- 구 사용자 ID
    user_name         TEXT,                                            -- 사용자 이름
    email             TEXT,                                            -- 이메일
    dept_name         TEXT,                                            -- 부서명
    PRIMARY KEY (migration_run_id, row_no)                             -- 기본키
);

-- 사용자 스테이징 테이블 코멘트
COMMENT ON TABLE stg_user IS '구시스템 사용자 원본 값을 작업별로 그대로 보관하는 이관용 테이블';
COMMENT ON COLUMN stg_user.migration_run_id IS '이관 작업 ID';
COMMENT ON COLUMN stg_user.row_no IS '원본 행 번호 (작업 안에서 순번)';
COMMENT ON COLUMN stg_user.inst_code IS '원본 기관 코드';
COMMENT ON COLUMN stg_user.legacy_user_id IS '구 사용자 ID';
COMMENT ON COLUMN stg_user.user_name IS '사용자 이름';
COMMENT ON COLUMN stg_user.email IS '이메일';
COMMENT ON COLUMN stg_user.dept_name IS '부서명';


-- =====================================================================================
-- 3. 과제 스테이징 (stg_project)
-- 설명: 구시스템 연구 과제 원본 값을 작업별로 보관하는 이관용 테이블
-- =====================================================================================
CREATE TABLE stg_project (
    migration_run_id    BIGINT NOT NULL REFERENCES migration_run (id) ON DELETE CASCADE, -- 이관 작업 ID
    row_no              INT    NOT NULL,                               -- 원본 행 번호
    inst_code           TEXT,                                          -- 원본 기관 코드
    legacy_research_no  TEXT,                                          -- 구 연구번호
    title               TEXT,                                          -- 과제명
    pi_legacy_user_id   TEXT,                                          -- 책임연구자 구 사용자 ID
    start_date          TEXT,                                          -- 시작일 (원본 문자열)
    end_date            TEXT,                                          -- 종료일 (원본 문자열)
    PRIMARY KEY (migration_run_id, row_no)                             -- 기본키
);

-- 과제 스테이징 테이블 코멘트
COMMENT ON TABLE stg_project IS '구시스템 연구 과제 원본 값을 작업별로 보관하는 이관용 테이블';
COMMENT ON COLUMN stg_project.migration_run_id IS '이관 작업 ID';
COMMENT ON COLUMN stg_project.row_no IS '원본 행 번호';
COMMENT ON COLUMN stg_project.inst_code IS '원본 기관 코드';
COMMENT ON COLUMN stg_project.legacy_research_no IS '구 연구번호';
COMMENT ON COLUMN stg_project.title IS '과제명';
COMMENT ON COLUMN stg_project.pi_legacy_user_id IS '책임연구자 구 사용자 ID';
COMMENT ON COLUMN stg_project.start_date IS '시작일 (원본 문자열)';
COMMENT ON COLUMN stg_project.end_date IS '종료일 (원본 문자열)';


-- =====================================================================================
-- 4. 심의 신청 스테이징 (stg_submission)
-- 설명: 구시스템 심의 신청 원본 값을 작업별로 보관하는 이관용 테이블
-- =====================================================================================
CREATE TABLE stg_submission (
    migration_run_id          BIGINT NOT NULL REFERENCES migration_run (id) ON DELETE CASCADE, -- 이관 작업 ID
    row_no                    INT    NOT NULL,                         -- 원본 행 번호
    inst_code                 TEXT,                                    -- 원본 기관 코드
    legacy_regno              TEXT,                                    -- 구 신청번호
    legacy_research_no        TEXT,                                    -- 구 연구번호 (구시스템부터 비어 있을 수 있음)
    applicant_legacy_user_id  TEXT,                                    -- 신청자 구 사용자 ID
    title                     TEXT,                                    -- 신청 제목
    legacy_status_code        TEXT,                                    -- 구 상태 코드
    submitted_at              TEXT,                                    -- 신청일시 (원본 문자열)
    updated_at                TEXT,                                    -- 수정일시 (원본 문자열)
    PRIMARY KEY (migration_run_id, row_no)                             -- 기본키
);

-- 심의 신청 스테이징 테이블 코멘트
COMMENT ON TABLE stg_submission IS '구시스템 심의 신청 원본 값을 작업별로 보관하는 이관용 테이블';
COMMENT ON COLUMN stg_submission.migration_run_id IS '이관 작업 ID';
COMMENT ON COLUMN stg_submission.row_no IS '원본 행 번호';
COMMENT ON COLUMN stg_submission.inst_code IS '원본 기관 코드';
COMMENT ON COLUMN stg_submission.legacy_regno IS '구 신청번호';
COMMENT ON COLUMN stg_submission.legacy_research_no IS '구 연구번호 (구시스템부터 비어 있을 수 있음)';
COMMENT ON COLUMN stg_submission.applicant_legacy_user_id IS '신청자 구 사용자 ID';
COMMENT ON COLUMN stg_submission.title IS '신청 제목';
COMMENT ON COLUMN stg_submission.legacy_status_code IS '구 상태 코드';
COMMENT ON COLUMN stg_submission.submitted_at IS '신청일시 (원본 문자열)';
COMMENT ON COLUMN stg_submission.updated_at IS '수정일시 (원본 문자열)';


-- =====================================================================================
-- 5. 심의 이력 스테이징 (stg_review)
-- 설명: 구시스템 심의 이력 원본 값을 작업별로 보관하는 이관용 테이블
-- =====================================================================================
CREATE TABLE stg_review (
    migration_run_id         BIGINT NOT NULL REFERENCES migration_run (id) ON DELETE CASCADE, -- 이관 작업 ID
    row_no                   INT    NOT NULL,                          -- 원본 행 번호
    inst_code                TEXT,                                     -- 원본 기관 코드
    legacy_review_id         TEXT,                                     -- 구 심의 ID
    legacy_regno             TEXT,                                     -- 구 신청번호
    reviewer_legacy_user_id  TEXT,                                     -- 심의위원 구 사용자 ID
    result_code              TEXT,                                     -- 구 심의 결과 코드
    opinion                  TEXT,                                     -- 심의 의견
    reviewed_at              TEXT,                                     -- 심의일시 (원본 문자열)
    PRIMARY KEY (migration_run_id, row_no)                             -- 기본키
);

-- 심의 이력 스테이징 테이블 코멘트
COMMENT ON TABLE stg_review IS '구시스템 심의 이력 원본 값을 작업별로 보관하는 이관용 테이블';
COMMENT ON COLUMN stg_review.migration_run_id IS '이관 작업 ID';
COMMENT ON COLUMN stg_review.row_no IS '원본 행 번호';
COMMENT ON COLUMN stg_review.inst_code IS '원본 기관 코드';
COMMENT ON COLUMN stg_review.legacy_review_id IS '구 심의 ID';
COMMENT ON COLUMN stg_review.legacy_regno IS '구 신청번호';
COMMENT ON COLUMN stg_review.reviewer_legacy_user_id IS '심의위원 구 사용자 ID';
COMMENT ON COLUMN stg_review.result_code IS '구 심의 결과 코드';
COMMENT ON COLUMN stg_review.opinion IS '심의 의견';
COMMENT ON COLUMN stg_review.reviewed_at IS '심의일시 (원본 문자열)';


-- =====================================================================================
-- 6. 검증 오류 (migration_issue)
-- 설명: 적재 전 검사에서 찾은 중복·필수값 누락·매핑 실패 사유를 남기는 테이블
-- =====================================================================================
CREATE TABLE migration_issue (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, -- 검증 오류 ID (자동증가)
    migration_run_id  BIGINT      NOT NULL REFERENCES migration_run (id) ON DELETE CASCADE, -- 이관 작업 ID
    table_name        VARCHAR(40) NOT NULL,                            -- 스테이징 테이블명
    row_no            INT         NOT NULL,                            -- 원본 행 번호
    source_key        TEXT,                                            -- 원본 키 (기관 코드/구 번호)
    issue_type        VARCHAR(40) NOT NULL,                            -- 오류 유형 (DUPLICATE_SOURCE_KEY, REQUIRED_MISSING, PROJECT_MAPPING_FAILED 등)
    severity          VARCHAR(10) NOT NULL CHECK (severity IN ('ERROR', 'INFO')), -- 심각도 (ERROR:적재 제외, INFO:안내만)
    message           TEXT        NOT NULL                             -- 사유
);

-- 검증 오류 테이블 코멘트
COMMENT ON TABLE migration_issue IS '적재 전 검사에서 찾은 중복·필수값 누락·매핑 실패 사유를 남기는 테이블';
COMMENT ON COLUMN migration_issue.id IS '검증 오류 ID (자동증가)';
COMMENT ON COLUMN migration_issue.migration_run_id IS '이관 작업 ID';
COMMENT ON COLUMN migration_issue.table_name IS '스테이징 테이블명';
COMMENT ON COLUMN migration_issue.row_no IS '원본 행 번호';
COMMENT ON COLUMN migration_issue.source_key IS '원본 키 (기관 코드/구 번호)';
COMMENT ON COLUMN migration_issue.issue_type IS '오류 유형 (DUPLICATE_SOURCE_KEY, REQUIRED_MISSING, PROJECT_MAPPING_FAILED 등)';
COMMENT ON COLUMN migration_issue.severity IS '심각도 (ERROR:적재 제외, INFO:안내만)';
COMMENT ON COLUMN migration_issue.message IS '사유';


-- 인덱스: 작업·테이블·행 번호로 검증 오류 조회
CREATE INDEX ix_migration_issue_row ON migration_issue (migration_run_id, table_name, row_no);

-- =====================================================================================
-- 7. 코드 매핑 (code_map)
-- 설명: CSV 이관과 CDC가 함께 쓰는 구 코드와 새 코드의 대응표
-- =====================================================================================
CREATE TABLE code_map (
    code_group   VARCHAR(30) NOT NULL,                                 -- 코드 그룹 (APPL_STATUS:신청 상태, REVIEW_RESULT:심의 결과)
    inst_code    VARCHAR(10) NOT NULL DEFAULT '*',                     -- 기관 코드 (*:모든 기관 공통)
    legacy_code  VARCHAR(20) NOT NULL,                                 -- 구 코드
    new_code     VARCHAR(30) NOT NULL,                                 -- 새 코드
    label        VARCHAR(50) NOT NULL,                                 -- 화면 표시명
    PRIMARY KEY (code_group, inst_code, legacy_code)                   -- 기본키
);

-- 코드 매핑 테이블 코멘트
COMMENT ON TABLE code_map IS 'CSV 이관과 CDC가 함께 쓰는 구 코드와 새 코드의 대응표';
COMMENT ON COLUMN code_map.code_group IS '코드 그룹 (APPL_STATUS:신청 상태, REVIEW_RESULT:심의 결과)';
COMMENT ON COLUMN code_map.inst_code IS '기관 코드 (*:모든 기관 공통)';
COMMENT ON COLUMN code_map.legacy_code IS '구 코드';
COMMENT ON COLUMN code_map.new_code IS '새 코드';
COMMENT ON COLUMN code_map.label IS '화면 표시명';


-- =====================================================================================
-- 8. 가상 과제 처리 기준 (mig_virtual_project_rule)
-- 설명: 구시스템부터 과제 연결이 없던 신청 중 가상 과제에 연결해도 되는 건과 그 사유
-- =====================================================================================
CREATE TABLE mig_virtual_project_rule (
    inst_code     VARCHAR(10)  NOT NULL,                               -- 원본 기관 코드
    legacy_regno  VARCHAR(30)  NOT NULL,                               -- 구 신청번호
    reason        VARCHAR(300) NOT NULL,                               -- 가상 과제로 연결하는 처리 기준
    PRIMARY KEY (inst_code, legacy_regno)                              -- 기본키
);

-- 가상 과제 처리 기준 테이블 코멘트
COMMENT ON TABLE mig_virtual_project_rule IS '구시스템부터 과제 연결이 없던 신청 중 가상 과제에 연결해도 되는 건과 그 사유';
COMMENT ON COLUMN mig_virtual_project_rule.inst_code IS '원본 기관 코드';
COMMENT ON COLUMN mig_virtual_project_rule.legacy_regno IS '구 신청번호';
COMMENT ON COLUMN mig_virtual_project_rule.reason IS '가상 과제로 연결하는 처리 기준';


-- 새 사용자 ID 채번 시퀀스
CREATE SEQUENCE seq_new_user_id START WITH 1;
-- 새 과제번호 채번 시퀀스
CREATE SEQUENCE seq_new_project_no START WITH 1;

-- =====================================================================================
-- 9. 사용자 ID 매핑 (map_user)
-- 설명: 구 사용자 ID와 새 사용자 ID의 대응표 (기관 코드 포함)
-- =====================================================================================
CREATE TABLE map_user (
    inst_code         VARCHAR(10) NOT NULL,                            -- 원본 기관 코드
    legacy_user_id    VARCHAR(50) NOT NULL,                            -- 구 사용자 ID
    user_id           VARCHAR(20) NOT NULL UNIQUE,                     -- 새 사용자 ID (U + 7자리)
    migration_run_id  BIGINT,                                          -- 매핑을 만든 이관 작업 ID
    mapped_at         TIMESTAMPTZ NOT NULL DEFAULT now(),              -- 매핑 생성일시
    PRIMARY KEY (inst_code, legacy_user_id)                            -- 기본키
);

-- 사용자 ID 매핑 테이블 코멘트
COMMENT ON TABLE map_user IS '구 사용자 ID와 새 사용자 ID의 대응표 (기관 코드 포함)';
COMMENT ON COLUMN map_user.inst_code IS '원본 기관 코드';
COMMENT ON COLUMN map_user.legacy_user_id IS '구 사용자 ID';
COMMENT ON COLUMN map_user.user_id IS '새 사용자 ID (U + 7자리)';
COMMENT ON COLUMN map_user.migration_run_id IS '매핑을 만든 이관 작업 ID';
COMMENT ON COLUMN map_user.mapped_at IS '매핑 생성일시';


-- =====================================================================================
-- 10. 과제번호 매핑 (map_project)
-- 설명: 구 연구번호와 새 과제번호의 대응표 (가상 과제 포함)
-- =====================================================================================
CREATE TABLE map_project (
    inst_code           VARCHAR(10) NOT NULL,                          -- 원본 기관 코드
    legacy_research_no  VARCHAR(50) NOT NULL,                          -- 구 연구번호 (__VIRTUAL__:가상 과제)
    project_no          VARCHAR(30) NOT NULL UNIQUE,                   -- 새 과제번호 (P-연도-5자리)
    is_virtual          BOOLEAN     NOT NULL DEFAULT false,            -- 가상 과제 여부
    migration_run_id    BIGINT,                                        -- 매핑을 만든 이관 작업 ID
    mapped_at           TIMESTAMPTZ NOT NULL DEFAULT now(),            -- 매핑 생성일시
    PRIMARY KEY (inst_code, legacy_research_no)                        -- 기본키
);

-- 과제번호 매핑 테이블 코멘트
COMMENT ON TABLE map_project IS '구 연구번호와 새 과제번호의 대응표 (가상 과제 포함)';
COMMENT ON COLUMN map_project.inst_code IS '원본 기관 코드';
COMMENT ON COLUMN map_project.legacy_research_no IS '구 연구번호 (__VIRTUAL__:가상 과제)';
COMMENT ON COLUMN map_project.project_no IS '새 과제번호 (P-연도-5자리)';
COMMENT ON COLUMN map_project.is_virtual IS '가상 과제 여부';
COMMENT ON COLUMN map_project.migration_run_id IS '매핑을 만든 이관 작업 ID';
COMMENT ON COLUMN map_project.mapped_at IS '매핑 생성일시';


-- =====================================================================================
-- 11. 사용자 (iuser)
-- 설명: 새 시스템 사용자 테이블
-- =====================================================================================
CREATE TABLE iuser (
    user_id     VARCHAR(20)  PRIMARY KEY,                              -- 새 사용자 ID
    inst_code   VARCHAR(10)  NOT NULL,                                 -- 소속 기관 코드
    user_name   VARCHAR(100) NOT NULL,                                 -- 사용자 이름
    email       VARCHAR(200),                                          -- 이메일
    dept_name   VARCHAR(100),                                          -- 부서명
    active      BOOLEAN      NOT NULL DEFAULT true,                    -- 사용 여부 (구시스템 삭제 시 false)
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),                   -- 생성일시
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()                    -- 수정일시
);

-- 사용자 테이블 코멘트
COMMENT ON TABLE iuser IS '새 시스템 사용자 테이블';
COMMENT ON COLUMN iuser.user_id IS '새 사용자 ID';
COMMENT ON COLUMN iuser.inst_code IS '소속 기관 코드';
COMMENT ON COLUMN iuser.user_name IS '사용자 이름';
COMMENT ON COLUMN iuser.email IS '이메일';
COMMENT ON COLUMN iuser.dept_name IS '부서명';
COMMENT ON COLUMN iuser.active IS '사용 여부 (구시스템 삭제 시 false)';
COMMENT ON COLUMN iuser.created_at IS '생성일시';
COMMENT ON COLUMN iuser.updated_at IS '수정일시';


-- =====================================================================================
-- 12. 과제 (project)
-- 설명: 새 시스템 연구 과제 테이블
-- =====================================================================================
CREATE TABLE project (
    project_no  VARCHAR(30)  PRIMARY KEY,                              -- 새 과제번호
    inst_code   VARCHAR(10)  NOT NULL,                                 -- 소속 기관 코드
    title       VARCHAR(300) NOT NULL,                                 -- 과제명
    pi_user_id  VARCHAR(20)  REFERENCES iuser (user_id),               -- 책임연구자 새 사용자 ID
    start_date  DATE,                                                  -- 시작일
    end_date    DATE,                                                  -- 종료일
    is_virtual  BOOLEAN      NOT NULL DEFAULT false,                   -- 가상 과제 여부
    active      BOOLEAN      NOT NULL DEFAULT true,                    -- 사용 여부
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),                   -- 생성일시
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()                    -- 수정일시
);

-- 과제 테이블 코멘트
COMMENT ON TABLE project IS '새 시스템 연구 과제 테이블';
COMMENT ON COLUMN project.project_no IS '새 과제번호';
COMMENT ON COLUMN project.inst_code IS '소속 기관 코드';
COMMENT ON COLUMN project.title IS '과제명';
COMMENT ON COLUMN project.pi_user_id IS '책임연구자 새 사용자 ID';
COMMENT ON COLUMN project.start_date IS '시작일';
COMMENT ON COLUMN project.end_date IS '종료일';
COMMENT ON COLUMN project.is_virtual IS '가상 과제 여부';
COMMENT ON COLUMN project.active IS '사용 여부';
COMMENT ON COLUMN project.created_at IS '생성일시';
COMMENT ON COLUMN project.updated_at IS '수정일시';


-- =====================================================================================
-- 13. 심의 신청 (sub_mastinfo)
-- 설명: 새 시스템 심의 신청 테이블. regnoold와 기관 코드로 원본 신청을 추적한다
-- =====================================================================================
CREATE TABLE sub_mastinfo (
    sub_no             BIGINT GENERATED BY DEFAULT AS IDENTITY (START WITH 8501) PRIMARY KEY, -- 새 신청번호 (8501부터 자동증가)
    inst_code          VARCHAR(10)  NOT NULL,                          -- 원본 기관 코드
    regnoold           VARCHAR(30)  NOT NULL,                          -- 구 신청번호 (원본 추적용)
    project_no         VARCHAR(30)  NOT NULL REFERENCES project (project_no), -- 새 과제번호
    applicant_user_id  VARCHAR(20)  NOT NULL REFERENCES iuser (user_id), -- 신청자 새 사용자 ID
    title              VARCHAR(300) NOT NULL,                          -- 신청 제목
    status             VARCHAR(20)  NOT NULL,                          -- 신청 상태 (RECEIVED:접수, UNDER_REVIEW:심의중, APPROVED:승인, REJECTED:반려, WITHDRAWN:취소)
    submitted_at       TIMESTAMPTZ,                                    -- 신청일시
    updated_at         TIMESTAMPTZ,                                    -- 수정일시
    deleted            BOOLEAN      NOT NULL DEFAULT false,            -- 삭제 여부 (구시스템에서 삭제되면 true)
    source             VARCHAR(10)  NOT NULL,                          -- 적재 경로 (CSV, CDC)
    migration_run_id   BIGINT,                                         -- 적재한 이관 작업 ID
    version            INT          NOT NULL DEFAULT 0,                -- 변경 버전 (낙관적 잠금)
    CONSTRAINT uq_sub_mastinfo_source UNIQUE (inst_code, regnoold)     -- 원본 식별자 유니크 제약
);

-- 심의 신청 테이블 코멘트
COMMENT ON TABLE sub_mastinfo IS '새 시스템 심의 신청 테이블. regnoold와 기관 코드로 원본 신청을 추적한다';
COMMENT ON COLUMN sub_mastinfo.sub_no IS '새 신청번호 (8501부터 자동증가)';
COMMENT ON COLUMN sub_mastinfo.inst_code IS '원본 기관 코드';
COMMENT ON COLUMN sub_mastinfo.regnoold IS '구 신청번호 (원본 추적용)';
COMMENT ON COLUMN sub_mastinfo.project_no IS '새 과제번호';
COMMENT ON COLUMN sub_mastinfo.applicant_user_id IS '신청자 새 사용자 ID';
COMMENT ON COLUMN sub_mastinfo.title IS '신청 제목';
COMMENT ON COLUMN sub_mastinfo.status IS '신청 상태 (RECEIVED:접수, UNDER_REVIEW:심의중, APPROVED:승인, REJECTED:반려, WITHDRAWN:취소)';
COMMENT ON COLUMN sub_mastinfo.submitted_at IS '신청일시';
COMMENT ON COLUMN sub_mastinfo.updated_at IS '수정일시';
COMMENT ON COLUMN sub_mastinfo.deleted IS '삭제 여부 (구시스템에서 삭제되면 true)';
COMMENT ON COLUMN sub_mastinfo.source IS '적재 경로 (CSV, CDC)';
COMMENT ON COLUMN sub_mastinfo.migration_run_id IS '적재한 이관 작업 ID';
COMMENT ON COLUMN sub_mastinfo.version IS '변경 버전 (낙관적 잠금)';


-- =====================================================================================
-- 14. 심의 이력 (sub_review)
-- 설명: 새 시스템 심의 이력 테이블
-- =====================================================================================
CREATE TABLE sub_review (
    review_no         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, -- 심의 이력 번호 (자동증가)
    sub_no            BIGINT      NOT NULL REFERENCES sub_mastinfo (sub_no), -- 새 신청번호
    inst_code         VARCHAR(10) NOT NULL,                            -- 원본 기관 코드
    legacy_review_id  VARCHAR(30) NOT NULL,                            -- 구 심의 ID (원본 추적용)
    reviewer_user_id  VARCHAR(20) REFERENCES iuser (user_id),          -- 심의위원 새 사용자 ID
    result            VARCHAR(30) NOT NULL,                            -- 심의 결과 (APPROVE:승인, APPROVE_WITH_CHANGES:수정 후 승인, REJECT:반려)
    opinion           TEXT,                                            -- 심의 의견
    reviewed_at       TIMESTAMPTZ,                                     -- 심의일시
    source            VARCHAR(10) NOT NULL,                            -- 적재 경로 (CSV, CDC)
    migration_run_id  BIGINT,                                          -- 적재한 이관 작업 ID
    CONSTRAINT uq_sub_review_source UNIQUE (inst_code, legacy_review_id) -- 원본 식별자 유니크 제약
);

-- 심의 이력 테이블 코멘트
COMMENT ON TABLE sub_review IS '새 시스템 심의 이력 테이블';
COMMENT ON COLUMN sub_review.review_no IS '심의 이력 번호 (자동증가)';
COMMENT ON COLUMN sub_review.sub_no IS '새 신청번호';
COMMENT ON COLUMN sub_review.inst_code IS '원본 기관 코드';
COMMENT ON COLUMN sub_review.legacy_review_id IS '구 심의 ID (원본 추적용)';
COMMENT ON COLUMN sub_review.reviewer_user_id IS '심의위원 새 사용자 ID';
COMMENT ON COLUMN sub_review.result IS '심의 결과 (APPROVE:승인, APPROVE_WITH_CHANGES:수정 후 승인, REJECT:반려)';
COMMENT ON COLUMN sub_review.opinion IS '심의 의견';
COMMENT ON COLUMN sub_review.reviewed_at IS '심의일시';
COMMENT ON COLUMN sub_review.source IS '적재 경로 (CSV, CDC)';
COMMENT ON COLUMN sub_review.migration_run_id IS '적재한 이관 작업 ID';


-- 인덱스: 신청별 심의 이력 조회
CREATE INDEX ix_sub_review_sub ON sub_review (sub_no);
