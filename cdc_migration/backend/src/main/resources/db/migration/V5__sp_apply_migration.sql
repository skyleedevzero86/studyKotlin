-- =====================================================================================
-- Flyway 마이그레이션: 일괄 적재 프로시저
-- 프로젝트: CDC Migration Practice (cdc_migration)
-- 마이그레이션 버전: V5
-- 작성일: 2026-11-11
-- 설명: 검증을 통과한 스테이징 데이터를 사용자 → 과제 → 심의 신청 → 심의 이력 순서로 INSERT INTO … SELECT … 한다. 커밋·롤백은 호출한 서비스 트랜잭션이 맡는다
-- =====================================================================================

-- =====================================================================================
-- 1. 일괄 적재 (sp_apply_migration)
-- 설명: 원본 식별자 유니크 제약과 ON CONFLICT DO NOTHING으로 재실행해도 중복이 생기지 않는다
-- =====================================================================================
CREATE OR REPLACE PROCEDURE sp_apply_migration(
    p_run_id BIGINT,                                                   -- 이관 작업 ID
    INOUT o_users INT DEFAULT 0,                                       -- 반환: 새로 넣은 사용자 수
    INOUT o_projects INT DEFAULT 0,                                    -- 반환: 새로 넣은 과제 수 (가상 과제 포함)
    INOUT o_applications INT DEFAULT 0,                                -- 반환: 새로 넣은 심의 신청 수
    INOUT o_reviews INT DEFAULT 0                                      -- 반환: 새로 넣은 심의 이력 수
)
LANGUAGE plpgsql AS $$
DECLARE
    v_source VARCHAR(10);
    v_virtual INT;
BEGIN
    -- 작업 확인 (적재 경로를 CSV/CDC로 기록)
    SELECT mode INTO v_source FROM migration_run WHERE id = p_run_id;
    IF v_source IS NULL THEN
        RAISE EXCEPTION '이관 작업 %을(를) 찾을 수 없습니다.', p_run_id;
    END IF;

    -- 1단계 사용자: 새 사용자 ID 매핑 생성
    INSERT INTO map_user (inst_code, legacy_user_id, user_id, migration_run_id)
    SELECT k.inst_code, k.legacy_user_id, fn_new_user_id(), p_run_id
      FROM (SELECT DISTINCT trim(s.inst_code) AS inst_code, trim(s.legacy_user_id) AS legacy_user_id
              FROM stg_user s
             WHERE s.migration_run_id = p_run_id
               AND NOT fn_row_has_error(p_run_id, 'stg_user', s.row_no)) k
     WHERE NOT EXISTS (SELECT 1 FROM map_user m WHERE m.inst_code = k.inst_code AND m.legacy_user_id = k.legacy_user_id)
     ORDER BY k.inst_code, k.legacy_user_id;

    -- 1단계 사용자: 사용자 적재
    INSERT INTO iuser (user_id, inst_code, user_name, email, dept_name)
    SELECT m.user_id, m.inst_code, trim(s.user_name), nullif(trim(s.email), ''), nullif(trim(s.dept_name), '')
      FROM stg_user s
      JOIN map_user m ON m.inst_code = trim(s.inst_code) AND m.legacy_user_id = trim(s.legacy_user_id)
     WHERE s.migration_run_id = p_run_id
       AND NOT fn_row_has_error(p_run_id, 'stg_user', s.row_no)
    ON CONFLICT (user_id) DO NOTHING;
    GET DIAGNOSTICS o_users = ROW_COUNT;

    -- 2단계 과제: 새 과제번호 매핑 생성
    INSERT INTO map_project (inst_code, legacy_research_no, project_no, is_virtual, migration_run_id)
    SELECT k.inst_code, k.legacy_research_no, fn_new_project_no(), false, p_run_id
      FROM (SELECT DISTINCT trim(p.inst_code) AS inst_code, trim(p.legacy_research_no) AS legacy_research_no
              FROM stg_project p
             WHERE p.migration_run_id = p_run_id
               AND NOT fn_row_has_error(p_run_id, 'stg_project', p.row_no)) k
     WHERE NOT EXISTS (SELECT 1 FROM map_project m WHERE m.inst_code = k.inst_code AND m.legacy_research_no = k.legacy_research_no)
     ORDER BY k.inst_code, k.legacy_research_no;

    -- 2단계 과제: 처리 기준이 있는 신청의 기관별 가상 과제 매핑 생성
    INSERT INTO map_project (inst_code, legacy_research_no, project_no, is_virtual, migration_run_id)
    SELECT DISTINCT trim(a.inst_code), '__VIRTUAL__', fn_virtual_project_no(a.inst_code), true, p_run_id
      FROM stg_submission a
      JOIN mig_virtual_project_rule r ON r.inst_code = trim(a.inst_code) AND r.legacy_regno = trim(a.legacy_regno)
     WHERE a.migration_run_id = p_run_id
       AND nullif(trim(a.legacy_research_no), '') IS NULL
       AND NOT fn_row_has_error(p_run_id, 'stg_submission', a.row_no)
    ON CONFLICT (inst_code, legacy_research_no) DO NOTHING;

    -- 2단계 과제: 과제 적재
    INSERT INTO project (project_no, inst_code, title, pi_user_id, start_date, end_date, is_virtual)
    SELECT mp.project_no, mp.inst_code, trim(p.title), mu.user_id, fn_try_date(p.start_date), fn_try_date(p.end_date), false
      FROM stg_project p
      JOIN map_project mp ON mp.inst_code = trim(p.inst_code) AND mp.legacy_research_no = trim(p.legacy_research_no)
      LEFT JOIN map_user mu ON mu.inst_code = mp.inst_code AND mu.legacy_user_id = trim(p.pi_legacy_user_id)
     WHERE p.migration_run_id = p_run_id
       AND NOT fn_row_has_error(p_run_id, 'stg_project', p.row_no)
    ON CONFLICT (project_no) DO NOTHING;
    GET DIAGNOSTICS o_projects = ROW_COUNT;

    -- 2단계 과제: 가상 과제 적재
    INSERT INTO project (project_no, inst_code, title, is_virtual)
    SELECT mp.project_no, mp.inst_code, '가상 과제 (구시스템 과제 미연결)', true
      FROM map_project mp
     WHERE mp.is_virtual
    ON CONFLICT (project_no) DO NOTHING;
    GET DIAGNOSTICS v_virtual = ROW_COUNT;
    o_projects := o_projects + v_virtual;

    -- 3단계 심의 신청: 구 신청번호를 regnoold에 남기며 적재
    INSERT INTO sub_mastinfo (inst_code, regnoold, project_no, applicant_user_id, title, status, submitted_at, updated_at, source, migration_run_id)
    SELECT trim(a.inst_code), trim(a.legacy_regno), mp.project_no, mu.user_id, trim(a.title),
           fn_map_code('APPL_STATUS', a.inst_code, a.legacy_status_code),
           fn_try_timestamp(a.submitted_at), fn_try_timestamp(a.updated_at), v_source, p_run_id
      FROM stg_submission a
      JOIN map_user mu ON mu.inst_code = trim(a.inst_code) AND mu.legacy_user_id = trim(a.applicant_legacy_user_id)
      JOIN map_project mp ON mp.inst_code = trim(a.inst_code)
                         AND mp.legacy_research_no = coalesce(nullif(trim(a.legacy_research_no), ''), '__VIRTUAL__')
     WHERE a.migration_run_id = p_run_id
       AND NOT fn_row_has_error(p_run_id, 'stg_submission', a.row_no)
     ORDER BY trim(a.inst_code), trim(a.legacy_regno)
    ON CONFLICT ON CONSTRAINT uq_sub_mastinfo_source DO NOTHING;
    GET DIAGNOSTICS o_applications = ROW_COUNT;

    -- 4단계 심의 이력: 신청을 기관 코드 + regnoold로 찾아 적재
    INSERT INTO sub_review (sub_no, inst_code, legacy_review_id, reviewer_user_id, result, opinion, reviewed_at, source, migration_run_id)
    SELECT m.sub_no, trim(r.inst_code), trim(r.legacy_review_id), mu.user_id,
           fn_map_code('REVIEW_RESULT', r.inst_code, r.result_code),
           nullif(trim(r.opinion), ''), fn_try_timestamp(r.reviewed_at), v_source, p_run_id
      FROM stg_review r
      JOIN sub_mastinfo m ON m.inst_code = trim(r.inst_code) AND m.regnoold = trim(r.legacy_regno)
      LEFT JOIN map_user mu ON mu.inst_code = trim(r.inst_code) AND mu.legacy_user_id = trim(r.reviewer_legacy_user_id)
     WHERE r.migration_run_id = p_run_id
       AND NOT fn_row_has_error(p_run_id, 'stg_review', r.row_no)
     ORDER BY trim(r.inst_code), trim(r.legacy_review_id)
    ON CONFLICT ON CONSTRAINT uq_sub_review_source DO NOTHING;
    GET DIAGNOSTICS o_reviews = ROW_COUNT;
END;
$$;
