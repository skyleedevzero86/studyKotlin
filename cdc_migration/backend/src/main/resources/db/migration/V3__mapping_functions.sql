-- =====================================================================================
-- Flyway 마이그레이션: 매핑 공통 함수
-- 프로젝트: CDC Migration Practice (cdc_migration)
-- 마이그레이션 버전: V3
-- 작성일: 2026-11-11
-- 설명: 날짜 변환, 코드 매핑, 새 ID 채번, 매핑 생성, 검증용 판별 함수를 만든다. 프로시저와 CDC 반영 코드가 같은 함수를 쓴다
-- =====================================================================================

-- =====================================================================================
-- 1. 일시 변환 (fn_try_timestamp)
-- 설명: 원본 문자열을 Asia/Seoul 기준 일시로 바꾼다. 형식이 틀리면 NULL
-- =====================================================================================
CREATE OR REPLACE FUNCTION fn_try_timestamp(p_value TEXT) RETURNS TIMESTAMPTZ
LANGUAGE plpgsql IMMUTABLE AS $$
BEGIN
    IF p_value IS NULL OR trim(p_value) = '' THEN
        RETURN NULL;
    END IF;
    RETURN (trim(p_value)::TIMESTAMP AT TIME ZONE 'Asia/Seoul');
EXCEPTION
    WHEN others THEN
        RETURN NULL;
END;
$$;

-- =====================================================================================
-- 2. 날짜 변환 (fn_try_date)
-- 설명: 원본 문자열을 날짜로 바꾼다. 형식이 틀리면 NULL
-- =====================================================================================
CREATE OR REPLACE FUNCTION fn_try_date(p_value TEXT) RETURNS DATE
LANGUAGE plpgsql IMMUTABLE AS $$
BEGIN
    IF p_value IS NULL OR trim(p_value) = '' THEN
        RETURN NULL;
    END IF;
    RETURN trim(p_value)::DATE;
EXCEPTION
    WHEN others THEN
        RETURN NULL;
END;
$$;

-- =====================================================================================
-- 3. 일시 형식 오류 판별 (fn_is_invalid_timestamp)
-- 설명: 값이 있는데 일시로 바꿀 수 없으면 true
-- =====================================================================================
CREATE OR REPLACE FUNCTION fn_is_invalid_timestamp(p_value TEXT) RETURNS BOOLEAN
LANGUAGE sql IMMUTABLE AS $$
    SELECT nullif(trim(p_value), '') IS NOT NULL AND fn_try_timestamp(p_value) IS NULL
$$;

-- =====================================================================================
-- 4. 날짜 형식 오류 판별 (fn_is_invalid_date)
-- 설명: 값이 있는데 날짜로 바꿀 수 없으면 true
-- =====================================================================================
CREATE OR REPLACE FUNCTION fn_is_invalid_date(p_value TEXT) RETURNS BOOLEAN
LANGUAGE sql IMMUTABLE AS $$
    SELECT nullif(trim(p_value), '') IS NOT NULL AND fn_try_date(p_value) IS NULL
$$;

-- =====================================================================================
-- 5. 코드 매핑 (fn_map_code)
-- 설명: 기관별 코드가 있으면 우선 쓰고, 없으면 공통(*) 코드를 쓴다
-- =====================================================================================
CREATE OR REPLACE FUNCTION fn_map_code(p_group VARCHAR, p_inst_code VARCHAR, p_legacy_code TEXT) RETURNS VARCHAR
LANGUAGE sql STABLE AS $$
    SELECT c.new_code
      FROM code_map c
     WHERE c.code_group = p_group
       AND c.legacy_code = trim(p_legacy_code)
       AND c.inst_code IN (trim(p_inst_code), '*')
     ORDER BY c.inst_code = '*'
     LIMIT 1
$$;

-- =====================================================================================
-- 6. 새 사용자 ID 채번 (fn_new_user_id)
-- 설명: U + 7자리 순번
-- =====================================================================================
CREATE OR REPLACE FUNCTION fn_new_user_id() RETURNS VARCHAR
LANGUAGE sql VOLATILE AS $$
    SELECT 'U' || lpad(nextval('seq_new_user_id')::TEXT, 7, '0')
$$;

-- =====================================================================================
-- 7. 새 과제번호 채번 (fn_new_project_no)
-- 설명: P-연도-5자리 순번
-- =====================================================================================
CREATE OR REPLACE FUNCTION fn_new_project_no() RETURNS VARCHAR
LANGUAGE sql VOLATILE AS $$
    SELECT 'P-' || to_char(now(), 'YYYY') || '-' || lpad(nextval('seq_new_project_no')::TEXT, 5, '0')
$$;

-- =====================================================================================
-- 8. 가상 과제번호 (fn_virtual_project_no)
-- 설명: 기관별 가상 과제번호 P-VIRTUAL-{기관 코드}
-- =====================================================================================
CREATE OR REPLACE FUNCTION fn_virtual_project_no(p_inst_code VARCHAR) RETURNS VARCHAR
LANGUAGE sql IMMUTABLE AS $$
    SELECT 'P-VIRTUAL-' || trim(p_inst_code)
$$;

-- =====================================================================================
-- 9. 사용자 매핑 생성 (fn_ensure_user_mapping)
-- 설명: 구 사용자 ID에 대응하는 새 사용자 ID를 찾고, 없으면 만든다 (CDC 반영용)
-- =====================================================================================
CREATE OR REPLACE FUNCTION fn_ensure_user_mapping(p_inst_code VARCHAR, p_legacy_user_id VARCHAR, p_run_id BIGINT) RETURNS VARCHAR
LANGUAGE plpgsql AS $$
DECLARE
    v_user_id VARCHAR;
BEGIN
    SELECT m.user_id INTO v_user_id FROM map_user m WHERE m.inst_code = trim(p_inst_code) AND m.legacy_user_id = trim(p_legacy_user_id);
    IF v_user_id IS NULL THEN
        INSERT INTO map_user (inst_code, legacy_user_id, user_id, migration_run_id)
        VALUES (trim(p_inst_code), trim(p_legacy_user_id), fn_new_user_id(), p_run_id)
        ON CONFLICT (inst_code, legacy_user_id) DO NOTHING;
        SELECT m.user_id INTO v_user_id FROM map_user m WHERE m.inst_code = trim(p_inst_code) AND m.legacy_user_id = trim(p_legacy_user_id);
    END IF;
    RETURN v_user_id;
END;
$$;

-- =====================================================================================
-- 10. 과제 매핑 생성 (fn_ensure_project_mapping)
-- 설명: 구 연구번호에 대응하는 새 과제번호를 찾고, 없으면 만든다 (CDC 반영용)
-- =====================================================================================
CREATE OR REPLACE FUNCTION fn_ensure_project_mapping(p_inst_code VARCHAR, p_legacy_research_no VARCHAR, p_run_id BIGINT) RETURNS VARCHAR
LANGUAGE plpgsql AS $$
DECLARE
    v_project_no VARCHAR;
BEGIN
    SELECT m.project_no INTO v_project_no FROM map_project m WHERE m.inst_code = trim(p_inst_code) AND m.legacy_research_no = trim(p_legacy_research_no);
    IF v_project_no IS NULL THEN
        INSERT INTO map_project (inst_code, legacy_research_no, project_no, is_virtual, migration_run_id)
        VALUES (trim(p_inst_code), trim(p_legacy_research_no), fn_new_project_no(), false, p_run_id)
        ON CONFLICT (inst_code, legacy_research_no) DO NOTHING;
        SELECT m.project_no INTO v_project_no FROM map_project m WHERE m.inst_code = trim(p_inst_code) AND m.legacy_research_no = trim(p_legacy_research_no);
    END IF;
    RETURN v_project_no;
END;
$$;

-- =====================================================================================
-- 11. 검증 오류 행 판별 (fn_row_has_error)
-- 설명: 스테이징 행에 ERROR 검증 오류가 있으면 true
-- =====================================================================================
CREATE OR REPLACE FUNCTION fn_row_has_error(p_run_id BIGINT, p_table VARCHAR, p_row_no INT) RETURNS BOOLEAN
LANGUAGE sql STABLE AS $$
    SELECT EXISTS (
        SELECT 1 FROM migration_issue i
         WHERE i.migration_run_id = p_run_id AND i.table_name = p_table AND i.row_no = p_row_no AND i.severity = 'ERROR')
$$;

-- =====================================================================================
-- 12. 사용자 매핑 가능 판별 (fn_user_resolvable)
-- 설명: 이미 매핑됐거나 같은 작업의 정상 스테이징 사용자가 있으면 true
-- =====================================================================================
CREATE OR REPLACE FUNCTION fn_user_resolvable(p_run_id BIGINT, p_inst_code TEXT, p_legacy_user_id TEXT) RETURNS BOOLEAN
LANGUAGE sql STABLE AS $$
    SELECT EXISTS (SELECT 1 FROM map_user m WHERE m.inst_code = trim(p_inst_code) AND m.legacy_user_id = trim(p_legacy_user_id))
        OR EXISTS (SELECT 1 FROM stg_user s
                    WHERE s.migration_run_id = p_run_id
                      AND trim(s.inst_code) = trim(p_inst_code)
                      AND trim(s.legacy_user_id) = trim(p_legacy_user_id)
                      AND NOT fn_row_has_error(p_run_id, 'stg_user', s.row_no))
$$;

-- =====================================================================================
-- 13. 과제 매핑 가능 판별 (fn_project_resolvable)
-- 설명: 이미 매핑됐거나 같은 작업의 정상 스테이징 과제가 있으면 true
-- =====================================================================================
CREATE OR REPLACE FUNCTION fn_project_resolvable(p_run_id BIGINT, p_inst_code TEXT, p_legacy_research_no TEXT) RETURNS BOOLEAN
LANGUAGE sql STABLE AS $$
    SELECT EXISTS (SELECT 1 FROM map_project m
                    WHERE m.inst_code = trim(p_inst_code) AND m.legacy_research_no = trim(p_legacy_research_no) AND NOT m.is_virtual)
        OR EXISTS (SELECT 1 FROM stg_project p
                    WHERE p.migration_run_id = p_run_id
                      AND trim(p.inst_code) = trim(p_inst_code)
                      AND trim(p.legacy_research_no) = trim(p_legacy_research_no)
                      AND NOT fn_row_has_error(p_run_id, 'stg_project', p.row_no))
$$;

-- =====================================================================================
-- 14. 신청 적재 가능 판별 (fn_application_resolvable)
-- 설명: 이미 적재됐거나 같은 작업의 정상 스테이징 신청이 있으면 true
-- =====================================================================================
CREATE OR REPLACE FUNCTION fn_application_resolvable(p_run_id BIGINT, p_inst_code TEXT, p_legacy_regno TEXT) RETURNS BOOLEAN
LANGUAGE sql STABLE AS $$
    SELECT EXISTS (SELECT 1 FROM sub_mastinfo m WHERE m.inst_code = trim(p_inst_code) AND m.regnoold = trim(p_legacy_regno))
        OR EXISTS (SELECT 1 FROM stg_submission a
                    WHERE a.migration_run_id = p_run_id
                      AND trim(a.inst_code) = trim(p_inst_code)
                      AND trim(a.legacy_regno) = trim(p_legacy_regno)
                      AND NOT fn_row_has_error(p_run_id, 'stg_submission', a.row_no))
$$;
