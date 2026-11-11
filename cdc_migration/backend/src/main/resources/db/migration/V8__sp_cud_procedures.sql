-- =====================================================================================
-- Flyway 마이그레이션: 등록·수정·삭제 프로시저
-- 프로젝트: CDC Migration Practice (cdc_migration)
-- 마이그레이션 버전: V8
-- 작성일: 2026-11-11
-- 설명: 새 시스템 테이블의 등록(C)·수정(U)·삭제(D)를 sp_*_cud 프로시저로 처리한다.
--       모든 프로시저는 작업 구분, 작업자, 작업 IP를 받고 rtn_cod(1:성공, 0:실패)와 err_msg(한국어)를 돌려준다.
--       커밋·롤백은 호출한 서비스 트랜잭션이 맡는다
-- =====================================================================================

-- =====================================================================================
-- 1. 작업 로그 (work_log)
-- 설명: 프로시저가 처리한 등록·수정·삭제를 누가, 어디서, 언제 했는지 남기는 테이블
-- =====================================================================================
CREATE TABLE work_log (
    log_id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,     -- 작업 로그 ID (자동증가)
    procedure_nm  VARCHAR(50)  NOT NULL,                               -- 실행한 프로시저명
    operation     CHAR(1)      NOT NULL,                               -- 작업 구분 (C:등록, U:수정, D:삭제)
    target_table  VARCHAR(50)  NOT NULL,                               -- 대상 테이블명
    target_key    VARCHAR(200) NOT NULL,                               -- 대상 키
    detail        VARCHAR(1000),                                       -- 작업 내용
    worker        VARCHAR(100) NOT NULL,                               -- 작업자 (화면 사용자, CDC, BATCH)
    work_ip       VARCHAR(50)  NOT NULL,                               -- 작업 IP
    work_dt       TIMESTAMPTZ  NOT NULL DEFAULT now()                  -- 작업일시
);

-- 인덱스: 대상별 작업 이력 조회
CREATE INDEX ix_work_log_target ON work_log (target_table, target_key, work_dt DESC);

-- 작업 로그 테이블 코멘트
COMMENT ON TABLE work_log IS '프로시저 등록·수정·삭제 작업 로그 (누가·어디서·언제·무엇을)';
COMMENT ON COLUMN work_log.log_id IS '작업 로그 ID (자동증가)';
COMMENT ON COLUMN work_log.procedure_nm IS '실행한 프로시저명';
COMMENT ON COLUMN work_log.operation IS '작업 구분 (C:등록, U:수정, D:삭제)';
COMMENT ON COLUMN work_log.target_table IS '대상 테이블명';
COMMENT ON COLUMN work_log.target_key IS '대상 키';
COMMENT ON COLUMN work_log.detail IS '작업 내용';
COMMENT ON COLUMN work_log.worker IS '작업자 (화면 사용자, CDC, BATCH)';
COMMENT ON COLUMN work_log.work_ip IS '작업 IP';
COMMENT ON COLUMN work_log.work_dt IS '작업일시';

-- =====================================================================================
-- 2. 작업 로그 기록 (fn_write_work_log)
-- 설명: 각 sp_*_cud 프로시저가 처리 끝에 호출한다
-- =====================================================================================
CREATE OR REPLACE FUNCTION fn_write_work_log(
    p_procedure VARCHAR,                                                -- 프로시저명
    p_operation VARCHAR,                                                -- 작업 구분
    p_table     VARCHAR,                                                -- 대상 테이블
    p_key       VARCHAR,                                                -- 대상 키
    p_detail    VARCHAR,                                                -- 작업 내용
    p_worker    VARCHAR,                                                -- 작업자
    p_workip    VARCHAR                                                 -- 작업 IP
) RETURNS VOID
LANGUAGE sql AS $$
    INSERT INTO work_log (procedure_nm, operation, target_table, target_key, detail, worker, work_ip)
    VALUES (p_procedure, p_operation, p_table, p_key, left(p_detail, 1000),
            coalesce(nullif(trim(p_worker), ''), 'anonymous'), coalesce(nullif(trim(p_workip), ''), 'unknown'))
$$;

-- =====================================================================================
-- 3. 사용자 등록·수정·삭제 (sp_user_cud)
-- 설명: D는 행을 지우지 않고 사용 중지(active=false)로 바꾼다 (신청·심의 이력이 참조하므로)
-- =====================================================================================
CREATE OR REPLACE PROCEDURE sp_user_cud(
    in_operation  VARCHAR,                                              -- 작업 구분 (C, U, D)
    in_worker     VARCHAR,                                              -- 작업자
    in_workip     VARCHAR,                                              -- 작업 IP
    in_user_id    VARCHAR,                                              -- 새 사용자 ID
    in_inst_code  VARCHAR,                                              -- 소속 기관 코드
    in_user_name  VARCHAR,                                              -- 사용자 이름
    in_email      VARCHAR,                                              -- 이메일
    in_dept_name  VARCHAR,                                              -- 부서명
    INOUT rtn_cod VARCHAR,                                              -- 반환: 1 성공, 0 실패
    INOUT err_msg VARCHAR                                               -- 반환: 실패 사유 (한국어)
)
LANGUAGE plpgsql AS $$
DECLARE
    v_rows INT;
BEGIN
    rtn_cod := '0';
    err_msg := '';

    -- 공통 입력값 확인
    IF nullif(trim(in_operation), '') IS NULL THEN err_msg := '작업 구분(in_operation)이 비어 있습니다.'; RETURN; END IF;
    IF nullif(trim(in_user_id), '') IS NULL THEN err_msg := '사용자 ID가 비어 있습니다.'; RETURN; END IF;

    IF in_operation = 'C' THEN
        -- 등록: 필수값 확인 후 추가
        IF nullif(trim(in_user_name), '') IS NULL THEN err_msg := '사용자 이름이 비어 있습니다. (' || in_user_id || ')'; RETURN; END IF;
        IF EXISTS (SELECT 1 FROM iuser WHERE user_id = in_user_id) THEN err_msg := '이미 등록된 사용자입니다. (' || in_user_id || ')'; RETURN; END IF;
        INSERT INTO iuser (user_id, inst_code, user_name, email, dept_name)
        VALUES (in_user_id, in_inst_code, trim(in_user_name), nullif(trim(in_email), ''), nullif(trim(in_dept_name), ''));
    ELSIF in_operation = 'U' THEN
        -- 수정: 이름·이메일·부서를 바꾸고 다시 사용 상태로 둔다
        IF nullif(trim(in_user_name), '') IS NULL THEN err_msg := '사용자 이름이 비어 있습니다. (' || in_user_id || ')'; RETURN; END IF;
        UPDATE iuser
           SET user_name = trim(in_user_name), email = nullif(trim(in_email), ''), dept_name = nullif(trim(in_dept_name), ''),
               active = true, updated_at = now()
         WHERE user_id = in_user_id;
        GET DIAGNOSTICS v_rows = ROW_COUNT;
        IF v_rows = 0 THEN err_msg := '수정할 사용자가 없습니다. (' || in_user_id || ')'; RETURN; END IF;
    ELSIF in_operation = 'D' THEN
        -- 삭제: 사용 중지 처리
        UPDATE iuser SET active = false, updated_at = now() WHERE user_id = in_user_id;
        GET DIAGNOSTICS v_rows = ROW_COUNT;
        IF v_rows = 0 THEN err_msg := '사용 중지할 사용자가 없습니다. (' || in_user_id || ')'; RETURN; END IF;
    ELSE
        err_msg := '작업 구분은 C, U, D 중 하나여야 합니다. (입력값: ' || in_operation || ')';
        RETURN;
    END IF;

    -- 작업 로그 기록
    PERFORM fn_write_work_log('sp_user_cud', in_operation, 'iuser', in_user_id, in_user_name, in_worker, in_workip);
    rtn_cod := '1';
EXCEPTION
    WHEN others THEN
        rtn_cod := '0';
        err_msg := left('DB 오류: ' || SQLERRM, 1000);
END;
$$;

-- =====================================================================================
-- 4. 과제 등록·수정·삭제 (sp_project_cud)
-- 설명: D는 사용 중지(active=false)로 바꾼다 (심의 신청이 참조하므로)
-- =====================================================================================
CREATE OR REPLACE PROCEDURE sp_project_cud(
    in_operation   VARCHAR,                                             -- 작업 구분 (C, U, D)
    in_worker      VARCHAR,                                             -- 작업자
    in_workip      VARCHAR,                                             -- 작업 IP
    in_project_no  VARCHAR,                                             -- 새 과제번호
    in_inst_code   VARCHAR,                                             -- 소속 기관 코드
    in_title       VARCHAR,                                             -- 과제명
    in_pi_user_id  VARCHAR,                                             -- 책임연구자 새 사용자 ID
    in_start_date  DATE,                                                -- 시작일
    in_end_date    DATE,                                                -- 종료일
    in_is_virtual  BOOLEAN,                                             -- 가상 과제 여부
    INOUT rtn_cod  VARCHAR,                                             -- 반환: 1 성공, 0 실패
    INOUT err_msg  VARCHAR                                              -- 반환: 실패 사유 (한국어)
)
LANGUAGE plpgsql AS $$
DECLARE
    v_rows INT;
BEGIN
    rtn_cod := '0';
    err_msg := '';

    -- 공통 입력값 확인
    IF nullif(trim(in_operation), '') IS NULL THEN err_msg := '작업 구분(in_operation)이 비어 있습니다.'; RETURN; END IF;
    IF nullif(trim(in_project_no), '') IS NULL THEN err_msg := '과제번호가 비어 있습니다.'; RETURN; END IF;
    IF in_operation IN ('C', 'U') AND nullif(trim(in_title), '') IS NULL THEN err_msg := '과제명이 비어 있습니다. (' || in_project_no || ')'; RETURN; END IF;
    IF in_operation IN ('C', 'U') AND in_pi_user_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM iuser WHERE user_id = in_pi_user_id) THEN
        err_msg := '책임연구자 ' || in_pi_user_id || '이(가) 새 시스템에 없습니다.';
        RETURN;
    END IF;

    IF in_operation = 'C' THEN
        -- 등록
        IF EXISTS (SELECT 1 FROM project WHERE project_no = in_project_no) THEN err_msg := '이미 등록된 과제입니다. (' || in_project_no || ')'; RETURN; END IF;
        INSERT INTO project (project_no, inst_code, title, pi_user_id, start_date, end_date, is_virtual)
        VALUES (in_project_no, in_inst_code, trim(in_title), in_pi_user_id, in_start_date, in_end_date, coalesce(in_is_virtual, false));
    ELSIF in_operation = 'U' THEN
        -- 수정
        UPDATE project
           SET title = trim(in_title), pi_user_id = in_pi_user_id, start_date = in_start_date, end_date = in_end_date,
               active = true, updated_at = now()
         WHERE project_no = in_project_no;
        GET DIAGNOSTICS v_rows = ROW_COUNT;
        IF v_rows = 0 THEN err_msg := '수정할 과제가 없습니다. (' || in_project_no || ')'; RETURN; END IF;
    ELSIF in_operation = 'D' THEN
        -- 삭제: 사용 중지 처리
        UPDATE project SET active = false, updated_at = now() WHERE project_no = in_project_no;
        GET DIAGNOSTICS v_rows = ROW_COUNT;
        IF v_rows = 0 THEN err_msg := '사용 중지할 과제가 없습니다. (' || in_project_no || ')'; RETURN; END IF;
    ELSE
        err_msg := '작업 구분은 C, U, D 중 하나여야 합니다. (입력값: ' || in_operation || ')';
        RETURN;
    END IF;

    -- 작업 로그 기록
    PERFORM fn_write_work_log('sp_project_cud', in_operation, 'project', in_project_no, in_title, in_worker, in_workip);
    rtn_cod := '1';
EXCEPTION
    WHEN others THEN
        rtn_cod := '0';
        err_msg := left('DB 오류: ' || SQLERRM, 1000);
END;
$$;

-- =====================================================================================
-- 5. 심의 신청 등록·수정·삭제 (sp_submission_cud)
-- 설명: C는 새 신청번호를 돌려주고, U는 version이 맞을 때만 수정한다(낙관적 잠금), D는 삭제 표시만 한다
-- =====================================================================================
CREATE OR REPLACE PROCEDURE sp_submission_cud(
    in_operation          VARCHAR,                                      -- 작업 구분 (C, U, D)
    in_worker             VARCHAR,                                      -- 작업자
    in_workip             VARCHAR,                                      -- 작업 IP
    in_sub_no             BIGINT,                                       -- 새 신청번호 (U, D)
    in_inst_code          VARCHAR,                                      -- 원본 기관 코드
    in_regnoold           VARCHAR,                                      -- 구 신청번호
    in_project_no         VARCHAR,                                      -- 새 과제번호
    in_applicant_user_id  VARCHAR,                                      -- 신청자 새 사용자 ID
    in_title              VARCHAR,                                      -- 신청 제목
    in_status             VARCHAR,                                      -- 신청 상태
    in_submitted_at       TIMESTAMPTZ,                                  -- 신청일시
    in_updated_at         TIMESTAMPTZ,                                  -- 수정일시
    in_source             VARCHAR,                                      -- 적재 경로 (CSV, CDC)
    in_migration_run_id   BIGINT,                                       -- 이관 작업 ID
    in_version            INT,                                          -- 읽을 때의 version (U)
    INOUT o_sub_no        BIGINT,                                       -- 반환: 새 신청번호
    INOUT rtn_cod         VARCHAR,                                      -- 반환: 1 성공, 0 실패
    INOUT err_msg         VARCHAR                                       -- 반환: 실패 사유 (한국어)
)
LANGUAGE plpgsql AS $$
DECLARE
    v_rows INT;
BEGIN
    rtn_cod := '0';
    err_msg := '';

    -- 공통 입력값 확인
    IF nullif(trim(in_operation), '') IS NULL THEN err_msg := '작업 구분(in_operation)이 비어 있습니다.'; RETURN; END IF;

    IF in_operation = 'C' THEN
        -- 등록: 필수값·참조 확인 후 추가, 같은 원본 키(기관 코드 + regnoold)는 거부
        IF nullif(trim(in_inst_code), '') IS NULL OR nullif(trim(in_regnoold), '') IS NULL THEN err_msg := '기관 코드와 구 신청번호가 필요합니다.'; RETURN; END IF;
        IF nullif(trim(in_title), '') IS NULL THEN err_msg := '신청 제목이 비어 있습니다. (' || in_inst_code || '/' || in_regnoold || ')'; RETURN; END IF;
        IF NOT EXISTS (SELECT 1 FROM project WHERE project_no = in_project_no) THEN err_msg := '과제 ' || coalesce(in_project_no, '-') || '이(가) 새 시스템에 없습니다.'; RETURN; END IF;
        IF NOT EXISTS (SELECT 1 FROM iuser WHERE user_id = in_applicant_user_id) THEN err_msg := '신청자 ' || coalesce(in_applicant_user_id, '-') || '이(가) 새 시스템에 없습니다.'; RETURN; END IF;
        IF EXISTS (SELECT 1 FROM sub_mastinfo WHERE inst_code = in_inst_code AND regnoold = in_regnoold) THEN
            err_msg := '이미 이관된 신청입니다. (' || in_inst_code || '/' || in_regnoold || ')';
            RETURN;
        END IF;
        INSERT INTO sub_mastinfo (inst_code, regnoold, project_no, applicant_user_id, title, status, submitted_at, updated_at, source, migration_run_id)
        VALUES (in_inst_code, in_regnoold, in_project_no, in_applicant_user_id, trim(in_title), in_status, in_submitted_at, in_updated_at, in_source, in_migration_run_id)
        RETURNING sub_no INTO o_sub_no;
    ELSIF in_operation = 'U' THEN
        -- 수정: version이 다르면 다른 작업이 먼저 고친 것으로 보고 거부
        IF in_sub_no IS NULL THEN err_msg := '수정할 신청번호가 비어 있습니다.'; RETURN; END IF;
        IF NOT EXISTS (SELECT 1 FROM project WHERE project_no = in_project_no) THEN err_msg := '과제 ' || coalesce(in_project_no, '-') || '이(가) 새 시스템에 없습니다.'; RETURN; END IF;
        UPDATE sub_mastinfo
           SET project_no = in_project_no, title = trim(in_title), status = in_status, updated_at = in_updated_at, version = version + 1
         WHERE sub_no = in_sub_no AND version = in_version AND NOT deleted;
        GET DIAGNOSTICS v_rows = ROW_COUNT;
        IF v_rows = 0 THEN
            err_msg := '신청 ' || in_sub_no || '을(를) 수정하지 못했습니다. 없거나, 삭제됐거나, 다른 작업이 먼저 수정했습니다.';
            RETURN;
        END IF;
        o_sub_no := in_sub_no;
    ELSIF in_operation = 'D' THEN
        -- 삭제: 행은 남기고 삭제 표시
        IF in_sub_no IS NULL THEN err_msg := '삭제할 신청번호가 비어 있습니다.'; RETURN; END IF;
        UPDATE sub_mastinfo
           SET deleted = true, updated_at = coalesce(in_updated_at, updated_at), version = version + 1
         WHERE sub_no = in_sub_no AND NOT deleted;
        GET DIAGNOSTICS v_rows = ROW_COUNT;
        IF v_rows = 0 THEN err_msg := '삭제할 신청이 없거나 이미 삭제됐습니다. (' || in_sub_no || ')'; RETURN; END IF;
        o_sub_no := in_sub_no;
    ELSE
        err_msg := '작업 구분은 C, U, D 중 하나여야 합니다. (입력값: ' || in_operation || ')';
        RETURN;
    END IF;

    -- 작업 로그 기록
    PERFORM fn_write_work_log('sp_submission_cud', in_operation, 'sub_mastinfo', o_sub_no::TEXT,
                              coalesce(in_inst_code, '') || '/' || coalesce(in_regnoold, '') || ' ' || coalesce(in_status, ''), in_worker, in_workip);
    rtn_cod := '1';
EXCEPTION
    WHEN others THEN
        rtn_cod := '0';
        err_msg := left('DB 오류: ' || SQLERRM, 1000);
END;
$$;

-- =====================================================================================
-- 6. 심의 이력 등록·수정·삭제 (sp_review_cud)
-- 설명: 원본 키(기관 코드 + 구 심의 ID)로 대상을 찾는다
-- =====================================================================================
CREATE OR REPLACE PROCEDURE sp_review_cud(
    in_operation         VARCHAR,                                       -- 작업 구분 (C, U, D)
    in_worker            VARCHAR,                                       -- 작업자
    in_workip            VARCHAR,                                       -- 작업 IP
    in_inst_code         VARCHAR,                                       -- 원본 기관 코드
    in_legacy_review_id  VARCHAR,                                       -- 구 심의 ID
    in_sub_no            BIGINT,                                        -- 새 신청번호
    in_reviewer_user_id  VARCHAR,                                       -- 심의위원 새 사용자 ID
    in_result            VARCHAR,                                       -- 심의 결과
    in_opinion           VARCHAR,                                       -- 심의 의견
    in_reviewed_at       TIMESTAMPTZ,                                   -- 심의일시
    in_source            VARCHAR,                                       -- 적재 경로 (CSV, CDC)
    in_migration_run_id  BIGINT,                                        -- 이관 작업 ID
    INOUT rtn_cod        VARCHAR,                                       -- 반환: 1 성공, 0 실패
    INOUT err_msg        VARCHAR                                        -- 반환: 실패 사유 (한국어)
)
LANGUAGE plpgsql AS $$
DECLARE
    v_rows INT;
    v_key  VARCHAR := coalesce(in_inst_code, '') || '/' || coalesce(in_legacy_review_id, '');
BEGIN
    rtn_cod := '0';
    err_msg := '';

    -- 공통 입력값 확인
    IF nullif(trim(in_operation), '') IS NULL THEN err_msg := '작업 구분(in_operation)이 비어 있습니다.'; RETURN; END IF;
    IF nullif(trim(in_inst_code), '') IS NULL OR nullif(trim(in_legacy_review_id), '') IS NULL THEN err_msg := '기관 코드와 구 심의 ID가 필요합니다.'; RETURN; END IF;
    IF in_operation IN ('C', 'U') THEN
        IF NOT EXISTS (SELECT 1 FROM sub_mastinfo WHERE sub_no = in_sub_no) THEN err_msg := '신청 ' || coalesce(in_sub_no::TEXT, '-') || '이(가) 새 시스템에 없습니다. (' || v_key || ')'; RETURN; END IF;
        IF nullif(trim(in_result), '') IS NULL THEN err_msg := '심의 결과가 비어 있습니다. (' || v_key || ')'; RETURN; END IF;
        IF in_reviewer_user_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM iuser WHERE user_id = in_reviewer_user_id) THEN
            err_msg := '심의위원 ' || in_reviewer_user_id || '이(가) 새 시스템에 없습니다.';
            RETURN;
        END IF;
    END IF;

    IF in_operation = 'C' THEN
        -- 등록
        IF EXISTS (SELECT 1 FROM sub_review WHERE inst_code = in_inst_code AND legacy_review_id = in_legacy_review_id) THEN
            err_msg := '이미 이관된 심의 이력입니다. (' || v_key || ')';
            RETURN;
        END IF;
        INSERT INTO sub_review (sub_no, inst_code, legacy_review_id, reviewer_user_id, result, opinion, reviewed_at, source, migration_run_id)
        VALUES (in_sub_no, in_inst_code, in_legacy_review_id, in_reviewer_user_id, in_result, nullif(trim(in_opinion), ''), in_reviewed_at, in_source, in_migration_run_id);
    ELSIF in_operation = 'U' THEN
        -- 수정
        UPDATE sub_review
           SET sub_no = in_sub_no, reviewer_user_id = in_reviewer_user_id, result = in_result,
               opinion = nullif(trim(in_opinion), ''), reviewed_at = in_reviewed_at
         WHERE inst_code = in_inst_code AND legacy_review_id = in_legacy_review_id;
        GET DIAGNOSTICS v_rows = ROW_COUNT;
        IF v_rows = 0 THEN err_msg := '수정할 심의 이력이 없습니다. (' || v_key || ')'; RETURN; END IF;
    ELSIF in_operation = 'D' THEN
        -- 삭제
        DELETE FROM sub_review WHERE inst_code = in_inst_code AND legacy_review_id = in_legacy_review_id;
        GET DIAGNOSTICS v_rows = ROW_COUNT;
        IF v_rows = 0 THEN err_msg := '삭제할 심의 이력이 없습니다. (' || v_key || ')'; RETURN; END IF;
    ELSE
        err_msg := '작업 구분은 C, U, D 중 하나여야 합니다. (입력값: ' || in_operation || ')';
        RETURN;
    END IF;

    -- 작업 로그 기록
    PERFORM fn_write_work_log('sp_review_cud', in_operation, 'sub_review', v_key, coalesce(in_result, ''), in_worker, in_workip);
    rtn_cod := '1';
EXCEPTION
    WHEN others THEN
        rtn_cod := '0';
        err_msg := left('DB 오류: ' || SQLERRM, 1000);
END;
$$;

-- =====================================================================================
-- 7. 첨부파일 등록·수정·삭제 (sp_attachment_cud)
-- 설명: C는 PENDING 상태로 메타데이터를 만들고 첨부파일 ID를 돌려준다.
--       U는 저장 상태를 바꾼다. READY는 본문(attachment_content)이 있어야만 허용한다
-- =====================================================================================
CREATE OR REPLACE PROCEDURE sp_attachment_cud(
    in_operation         VARCHAR,                                       -- 작업 구분 (C, U, D)
    in_worker            VARCHAR,                                       -- 작업자
    in_workip            VARCHAR,                                       -- 작업 IP
    in_attachment_id     BIGINT,                                        -- 첨부파일 ID (U, D)
    in_sub_no            BIGINT,                                        -- 새 신청번호
    in_request_id        VARCHAR,                                       -- 업로드 요청 ID
    in_origin_inst_code  VARCHAR,                                       -- 원본 기관 코드
    in_origin_file_id    VARCHAR,                                       -- 원본 파일 ID
    in_origin_regno      VARCHAR,                                       -- 원본 신청번호
    in_origin_seq        INT,                                           -- 원본 첨부 순번
    in_file_name         VARCHAR,                                       -- 원본 파일명
    in_content_type      VARCHAR,                                       -- 콘텐츠 타입
    in_byte_size         BIGINT,                                        -- 바이트 크기
    in_sha256            VARCHAR,                                       -- SHA-256
    in_bucket            VARCHAR,                                       -- MinIO 버킷
    in_object_key        VARCHAR,                                       -- MinIO 객체 키
    in_status            VARCHAR,                                       -- 저장 상태 (U)
    in_last_error        VARCHAR,                                       -- 실패 사유 (U)
    INOUT o_attachment_id BIGINT,                                       -- 반환: 첨부파일 ID
    INOUT rtn_cod        VARCHAR,                                       -- 반환: 1 성공, 0 실패
    INOUT err_msg        VARCHAR                                        -- 반환: 실패 사유 (한국어)
)
LANGUAGE plpgsql AS $$
DECLARE
    v_rows INT;
BEGIN
    rtn_cod := '0';
    err_msg := '';

    -- 공통 입력값 확인
    IF nullif(trim(in_operation), '') IS NULL THEN err_msg := '작업 구분(in_operation)이 비어 있습니다.'; RETURN; END IF;

    IF in_operation = 'C' THEN
        -- 등록: 신청 확인, 같은 요청 ID는 거부 (재시도는 기존 행을 U로 처리)
        IF nullif(trim(in_request_id), '') IS NULL THEN err_msg := '요청 ID가 비어 있습니다.'; RETURN; END IF;
        IF NOT EXISTS (SELECT 1 FROM sub_mastinfo WHERE sub_no = in_sub_no) THEN err_msg := '신청 ' || coalesce(in_sub_no::TEXT, '-') || '이(가) 없습니다.'; RETURN; END IF;
        IF EXISTS (SELECT 1 FROM attachment WHERE request_id = in_request_id) THEN err_msg := '이미 등록된 요청 ID입니다. (' || in_request_id || ')'; RETURN; END IF;
        INSERT INTO attachment (sub_no, request_id, origin_inst_code, origin_file_id, origin_regno, origin_seq, file_name, content_type,
                                byte_size, sha256, bucket, object_key, status)
        VALUES (in_sub_no, in_request_id, in_origin_inst_code, in_origin_file_id, in_origin_regno, in_origin_seq, in_file_name, in_content_type,
                in_byte_size, in_sha256, in_bucket, in_object_key, 'PENDING')
        RETURNING attachment_id INTO o_attachment_id;
    ELSIF in_operation = 'U' THEN
        -- 수정: 저장 상태 변경, READY는 본문 저장이 끝난 경우만
        IF in_status NOT IN ('PENDING', 'READY', 'FAILED') THEN err_msg := '저장 상태는 PENDING, READY, FAILED 중 하나여야 합니다.'; RETURN; END IF;
        IF in_status = 'READY' AND NOT EXISTS (SELECT 1 FROM attachment_content WHERE attachment_id = in_attachment_id) THEN
            err_msg := '첨부파일 ' || in_attachment_id || '의 DB 본문이 없어 저장 완료(READY)로 바꿀 수 없습니다.';
            RETURN;
        END IF;
        UPDATE attachment SET status = in_status, last_error = in_last_error, updated_at = now() WHERE attachment_id = in_attachment_id;
        GET DIAGNOSTICS v_rows = ROW_COUNT;
        IF v_rows = 0 THEN err_msg := '수정할 첨부파일이 없습니다. (' || coalesce(in_attachment_id::TEXT, '-') || ')'; RETURN; END IF;
        o_attachment_id := in_attachment_id;
    ELSIF in_operation = 'D' THEN
        -- 삭제: 본문은 ON DELETE CASCADE로 함께 지워진다
        DELETE FROM attachment WHERE attachment_id = in_attachment_id;
        GET DIAGNOSTICS v_rows = ROW_COUNT;
        IF v_rows = 0 THEN err_msg := '삭제할 첨부파일이 없습니다. (' || coalesce(in_attachment_id::TEXT, '-') || ')'; RETURN; END IF;
        o_attachment_id := in_attachment_id;
    ELSE
        err_msg := '작업 구분은 C, U, D 중 하나여야 합니다. (입력값: ' || in_operation || ')';
        RETURN;
    END IF;

    -- 작업 로그 기록
    PERFORM fn_write_work_log('sp_attachment_cud', in_operation, 'attachment', o_attachment_id::TEXT,
                              coalesce(in_file_name, '') || ' ' || coalesce(in_status, 'PENDING'), in_worker, in_workip);
    rtn_cod := '1';
EXCEPTION
    WHEN others THEN
        rtn_cod := '0';
        err_msg := left('DB 오류: ' || SQLERRM, 1000);
END;
$$;

-- =====================================================================================
-- 8. 첨부파일 본문 저장 (sp_attachment_content_u)
-- 설명: 파일 전체 바이트를 BYTEA로 저장한다. 같은 첨부파일로 다시 호출하면 덮어쓴다(재시도 복구)
-- =====================================================================================
CREATE OR REPLACE PROCEDURE sp_attachment_content_u(
    in_worker         VARCHAR,                                          -- 작업자
    in_workip         VARCHAR,                                          -- 작업 IP
    in_attachment_id  BIGINT,                                           -- 첨부파일 ID
    in_file_content   BYTEA,                                            -- 파일 전체 바이트
    INOUT rtn_cod     VARCHAR,                                          -- 반환: 1 성공, 0 실패
    INOUT err_msg     VARCHAR                                           -- 반환: 실패 사유 (한국어)
)
LANGUAGE plpgsql AS $$
DECLARE
    v_size BIGINT;
BEGIN
    rtn_cod := '0';
    err_msg := '';

    -- 입력값 확인: 메타데이터의 크기와 본문 크기가 같아야 한다
    IF in_file_content IS NULL THEN err_msg := '파일 본문이 비어 있습니다.'; RETURN; END IF;
    SELECT byte_size INTO v_size FROM attachment WHERE attachment_id = in_attachment_id;
    IF v_size IS NULL THEN err_msg := '첨부파일 ' || coalesce(in_attachment_id::TEXT, '-') || '이(가) 없습니다.'; RETURN; END IF;
    IF v_size <> length(in_file_content) THEN
        err_msg := '본문 크기(' || length(in_file_content) || ')가 메타데이터 크기(' || v_size || ')와 다릅니다.';
        RETURN;
    END IF;

    -- 본문 저장 (있으면 덮어쓰기)
    INSERT INTO attachment_content (attachment_id, file_content)
    VALUES (in_attachment_id, in_file_content)
    ON CONFLICT (attachment_id) DO UPDATE SET file_content = EXCLUDED.file_content, saved_at = now();

    -- 작업 로그 기록
    PERFORM fn_write_work_log('sp_attachment_content_u', 'U', 'attachment_content', in_attachment_id::TEXT,
                              'bytes=' || length(in_file_content), in_worker, in_workip);
    rtn_cod := '1';
EXCEPTION
    WHEN others THEN
        rtn_cod := '0';
        err_msg := left('DB 오류: ' || SQLERRM, 1000);
END;
$$;

-- =====================================================================================
-- 9. 첨부파일 이관 작업 등록·수정 (sp_file_transfer_cud)
-- 설명: C는 같은 원본(기관 코드 + 파일 ID + 객체 키)이 있으면 새로 만들지 않고 기존 ID를 돌려준다(o_created=false).
--       U는 처리 상태와 결과를 기록한다
-- =====================================================================================
CREATE OR REPLACE PROCEDURE sp_file_transfer_cud(
    in_operation          VARCHAR,                                      -- 작업 구분 (C, U)
    in_worker             VARCHAR,                                      -- 작업자
    in_workip             VARCHAR,                                      -- 작업 IP
    in_transfer_id        BIGINT,                                       -- 이관 작업 ID (U)
    in_source             VARCHAR,                                      -- 생성 경로 (CSV, CDC)
    in_batch_id           BIGINT,                                       -- CSV 묶음 ID
    in_cdc_run_id         BIGINT,                                       -- CDC 실행 ID
    in_inst_code          VARCHAR,                                      -- 원본 기관 코드
    in_legacy_file_id     VARCHAR,                                      -- 원본 파일 ID
    in_legacy_regno       VARCHAR,                                      -- 구 신청번호
    in_seq                INT,                                          -- 원본 첨부 순번
    in_file_name          VARCHAR,                                      -- 원본 파일명
    in_content_type       VARCHAR,                                      -- 콘텐츠 타입
    in_source_bucket      VARCHAR,                                      -- 구 MinIO 버킷
    in_source_object_key  VARCHAR,                                      -- 구 MinIO 객체 키
    in_status             VARCHAR,                                      -- 처리 상태 (U)
    in_attachment_id      BIGINT,                                       -- 생성된 첨부파일 ID (U)
    in_last_error         VARCHAR,                                      -- 처리 사유 (U)
    INOUT o_transfer_id   BIGINT,                                       -- 반환: 이관 작업 ID
    INOUT o_created       BOOLEAN,                                      -- 반환: 새로 만들었는지 여부
    INOUT rtn_cod         VARCHAR,                                      -- 반환: 1 성공, 0 실패
    INOUT err_msg         VARCHAR                                       -- 반환: 실패 사유 (한국어)
)
LANGUAGE plpgsql AS $$
DECLARE
    v_rows INT;
BEGIN
    rtn_cod := '0';
    err_msg := '';
    o_created := false;

    -- 공통 입력값 확인
    IF nullif(trim(in_operation), '') IS NULL THEN err_msg := '작업 구분(in_operation)이 비어 있습니다.'; RETURN; END IF;

    IF in_operation = 'C' THEN
        -- 등록: 필수값 확인, 같은 원본이면 기존 작업을 돌려준다
        IF nullif(trim(in_inst_code), '') IS NULL OR nullif(trim(in_legacy_file_id), '') IS NULL THEN err_msg := '기관 코드와 원본 파일 ID가 필요합니다.'; RETURN; END IF;
        IF nullif(trim(in_source_bucket), '') IS NULL OR nullif(trim(in_source_object_key), '') IS NULL THEN
            err_msg := '구 MinIO 버킷과 객체 키가 필요합니다. (' || in_inst_code || '/' || in_legacy_file_id || ')';
            RETURN;
        END IF;
        SELECT transfer_id INTO o_transfer_id
          FROM file_transfer
         WHERE inst_code = in_inst_code AND legacy_file_id = in_legacy_file_id AND source_object_key = in_source_object_key;
        IF o_transfer_id IS NULL THEN
            INSERT INTO file_transfer (source, batch_id, cdc_run_id, inst_code, legacy_file_id, legacy_regno, seq, file_name, content_type,
                                       source_bucket, source_object_key, status)
            VALUES (in_source, in_batch_id, in_cdc_run_id, in_inst_code, in_legacy_file_id, in_legacy_regno, in_seq, in_file_name, in_content_type,
                    in_source_bucket, in_source_object_key, 'PENDING')
            RETURNING transfer_id INTO o_transfer_id;
            o_created := true;
        END IF;
    ELSIF in_operation = 'U' THEN
        -- 수정: 처리 상태와 결과 기록
        IF in_status NOT IN ('PENDING', 'DONE', 'UNMAPPED', 'FAILED') THEN err_msg := '처리 상태는 PENDING, DONE, UNMAPPED, FAILED 중 하나여야 합니다.'; RETURN; END IF;
        UPDATE file_transfer
           SET status = in_status, attachment_id = coalesce(in_attachment_id, attachment_id), last_error = in_last_error,
               attempts = attempts + 1, updated_at = now(),
               completed_at = CASE WHEN in_status = 'DONE' THEN now() ELSE completed_at END
         WHERE transfer_id = in_transfer_id;
        GET DIAGNOSTICS v_rows = ROW_COUNT;
        IF v_rows = 0 THEN err_msg := '수정할 이관 작업이 없습니다. (' || coalesce(in_transfer_id::TEXT, '-') || ')'; RETURN; END IF;
        o_transfer_id := in_transfer_id;
    ELSE
        err_msg := '작업 구분은 C, U 중 하나여야 합니다. (입력값: ' || in_operation || ')';
        RETURN;
    END IF;

    -- 작업 로그 기록
    PERFORM fn_write_work_log('sp_file_transfer_cud', in_operation, 'file_transfer', o_transfer_id::TEXT,
                              coalesce(in_inst_code, '') || '/' || coalesce(in_legacy_file_id, '') || ' ' || coalesce(in_status, 'PENDING'),
                              in_worker, in_workip);
    rtn_cod := '1';
EXCEPTION
    WHEN others THEN
        rtn_cod := '0';
        err_msg := left('DB 오류: ' || SQLERRM, 1000);
END;
$$;
