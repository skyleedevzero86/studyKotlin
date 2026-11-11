-- =====================================================================================
-- 초기화 SQL: 구시스템 스키마
-- 프로젝트: CDC Migration Practice (cdc_migration)
-- 스크립트: 초기화 스크립트 01
-- 작성일: 2026-11-11
-- 설명: 실습용 구시스템(legacy_db) 테이블을 만든다. Debezium이 변경 전 값을 읽도록 REPLICA IDENTITY FULL을 건다
-- =====================================================================================

-- =====================================================================================
-- 1. 구시스템 사용자 (tb_user)
-- 설명: 구시스템 사용자 테이블 (기관별로 사용자 ID가 겹칠 수 있음)
-- =====================================================================================
CREATE TABLE tb_user (
    inst_code  VARCHAR(10) NOT NULL,                                   -- 기관 코드
    user_id    VARCHAR(20) NOT NULL,                                   -- 사용자 ID
    user_name  VARCHAR(50),                                            -- 사용자 이름
    email      VARCHAR(100),                                           -- 이메일
    dept_name  VARCHAR(50),                                            -- 부서명
    PRIMARY KEY (inst_code, user_id)                                   -- 기본키
);

-- 구시스템 사용자 테이블 코멘트
COMMENT ON TABLE tb_user IS '구시스템 사용자 테이블 (기관별로 사용자 ID가 겹칠 수 있음)';
COMMENT ON COLUMN tb_user.inst_code IS '기관 코드';
COMMENT ON COLUMN tb_user.user_id IS '사용자 ID';
COMMENT ON COLUMN tb_user.user_name IS '사용자 이름';
COMMENT ON COLUMN tb_user.email IS '이메일';
COMMENT ON COLUMN tb_user.dept_name IS '부서명';


-- =====================================================================================
-- 2. 구시스템 연구 과제 (tb_project)
-- 설명: 구시스템 연구 과제 테이블
-- =====================================================================================
CREATE TABLE tb_project (
    inst_code    VARCHAR(10) NOT NULL,                                 -- 기관 코드
    research_no  VARCHAR(30) NOT NULL,                                 -- 연구번호
    title        VARCHAR(200),                                         -- 과제명
    pi_user_id   VARCHAR(20),                                          -- 책임연구자 사용자 ID
    start_date   DATE,                                                 -- 시작일
    end_date     DATE,                                                 -- 종료일
    PRIMARY KEY (inst_code, research_no)                               -- 기본키
);

-- 구시스템 연구 과제 테이블 코멘트
COMMENT ON TABLE tb_project IS '구시스템 연구 과제 테이블';
COMMENT ON COLUMN tb_project.inst_code IS '기관 코드';
COMMENT ON COLUMN tb_project.research_no IS '연구번호';
COMMENT ON COLUMN tb_project.title IS '과제명';
COMMENT ON COLUMN tb_project.pi_user_id IS '책임연구자 사용자 ID';
COMMENT ON COLUMN tb_project.start_date IS '시작일';
COMMENT ON COLUMN tb_project.end_date IS '종료일';


-- =====================================================================================
-- 3. 구시스템 심의 신청 (tb_appl)
-- 설명: 구시스템 심의 신청 테이블. 신청번호에 유니크 제약이 없어 중복 데이터가 남아 있다
-- =====================================================================================
CREATE TABLE tb_appl (
    appl_seq      SERIAL PRIMARY KEY,                                  -- 내부 순번 (자동증가)
    inst_code     VARCHAR(10),                                         -- 기관 코드
    regno         VARCHAR(20),                                         -- 신청번호 (기관별 번호, 기관 간 중복 가능)
    research_no   VARCHAR(30),                                         -- 연구번호 (비어 있을 수 있음)
    applicant_id  VARCHAR(20),                                         -- 신청자 사용자 ID
    title         VARCHAR(200),                                        -- 신청 제목
    status_code   VARCHAR(2),                                          -- 상태 코드 (01:접수, 02:심의중, 03:승인, 04:반려, 05:취소)
    submitted_at  TIMESTAMP,                                           -- 신청일시
    updated_at    TIMESTAMP                                            -- 수정일시
);

-- 구시스템 심의 신청 테이블 코멘트
COMMENT ON TABLE tb_appl IS '구시스템 심의 신청 테이블. 신청번호에 유니크 제약이 없어 중복 데이터가 남아 있다';
COMMENT ON COLUMN tb_appl.appl_seq IS '내부 순번 (자동증가)';
COMMENT ON COLUMN tb_appl.inst_code IS '기관 코드';
COMMENT ON COLUMN tb_appl.regno IS '신청번호 (기관별 번호, 기관 간 중복 가능)';
COMMENT ON COLUMN tb_appl.research_no IS '연구번호 (비어 있을 수 있음)';
COMMENT ON COLUMN tb_appl.applicant_id IS '신청자 사용자 ID';
COMMENT ON COLUMN tb_appl.title IS '신청 제목';
COMMENT ON COLUMN tb_appl.status_code IS '상태 코드 (01:접수, 02:심의중, 03:승인, 04:반려, 05:취소)';
COMMENT ON COLUMN tb_appl.submitted_at IS '신청일시';
COMMENT ON COLUMN tb_appl.updated_at IS '수정일시';


-- =====================================================================================
-- 4. 구시스템 심의 이력 (tb_review)
-- 설명: 구시스템 심의 이력 테이블
-- =====================================================================================
CREATE TABLE tb_review (
    inst_code    VARCHAR(10) NOT NULL,                                 -- 기관 코드
    review_id    VARCHAR(20) NOT NULL,                                 -- 심의 ID
    regno        VARCHAR(20),                                          -- 신청번호
    reviewer_id  VARCHAR(20),                                          -- 심의위원 사용자 ID
    result_code  VARCHAR(2),                                           -- 심의 결과 코드 (A:승인, M:수정 후 승인, R:반려)
    opinion      VARCHAR(500),                                         -- 심의 의견
    reviewed_at  TIMESTAMP,                                            -- 심의일시
    PRIMARY KEY (inst_code, review_id)                                 -- 기본키
);

-- 구시스템 심의 이력 테이블 코멘트
COMMENT ON TABLE tb_review IS '구시스템 심의 이력 테이블';
COMMENT ON COLUMN tb_review.inst_code IS '기관 코드';
COMMENT ON COLUMN tb_review.review_id IS '심의 ID';
COMMENT ON COLUMN tb_review.regno IS '신청번호';
COMMENT ON COLUMN tb_review.reviewer_id IS '심의위원 사용자 ID';
COMMENT ON COLUMN tb_review.result_code IS '심의 결과 코드 (A:승인, M:수정 후 승인, R:반려)';
COMMENT ON COLUMN tb_review.opinion IS '심의 의견';
COMMENT ON COLUMN tb_review.reviewed_at IS '심의일시';


-- =====================================================================================
-- 5. 구시스템 첨부파일 (tb_attach)
-- 설명: 구시스템 첨부파일 메타데이터. 파일은 구 MinIO에 있다
-- =====================================================================================
CREATE TABLE tb_attach (
    inst_code     VARCHAR(10) NOT NULL,                                -- 기관 코드
    file_id       VARCHAR(20) NOT NULL,                                -- 파일 ID
    regno         VARCHAR(20),                                         -- 신청번호
    seq           INT,                                                 -- 첨부 순번
    file_name     VARCHAR(200),                                        -- 파일명
    content_type  VARCHAR(100),                                        -- 콘텐츠 타입
    bucket        VARCHAR(100),                                        -- MinIO 버킷
    object_key    VARCHAR(300),                                        -- MinIO 객체 키
    PRIMARY KEY (inst_code, file_id)                                   -- 기본키
);

-- 구시스템 첨부파일 테이블 코멘트
COMMENT ON TABLE tb_attach IS '구시스템 첨부파일 메타데이터. 파일은 구 MinIO에 있다';
COMMENT ON COLUMN tb_attach.inst_code IS '기관 코드';
COMMENT ON COLUMN tb_attach.file_id IS '파일 ID';
COMMENT ON COLUMN tb_attach.regno IS '신청번호';
COMMENT ON COLUMN tb_attach.seq IS '첨부 순번';
COMMENT ON COLUMN tb_attach.file_name IS '파일명';
COMMENT ON COLUMN tb_attach.content_type IS '콘텐츠 타입';
COMMENT ON COLUMN tb_attach.bucket IS 'MinIO 버킷';
COMMENT ON COLUMN tb_attach.object_key IS 'MinIO 객체 키';


-- =====================================================================================
-- 6. Debezium 변경 전 값 기록 (REPLICA IDENTITY FULL)
-- 설명: UPDATE·DELETE 이벤트의 before에 모든 컬럼이 담기도록 한다
-- =====================================================================================
ALTER TABLE tb_user REPLICA IDENTITY FULL;
ALTER TABLE tb_project REPLICA IDENTITY FULL;
ALTER TABLE tb_appl REPLICA IDENTITY FULL;
ALTER TABLE tb_review REPLICA IDENTITY FULL;
ALTER TABLE tb_attach REPLICA IDENTITY FULL;
