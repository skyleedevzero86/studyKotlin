-- =====================================================================================
-- Flyway 마이그레이션: 적재 전 검증 프로시저
-- 프로젝트: CDC Migration Practice (cdc_migration)
-- 마이그레이션 버전: V4
-- 작성일: 2026-11-11
-- 설명: 스테이징 데이터의 중복 원본 키, 필수값 누락, 형식 오류, 코드·사용자·과제·신청 매핑 실패를 검사해 migration_issue에 사유를 남긴다
-- =====================================================================================

-- =====================================================================================
-- 1. 적재 전 검증 (sp_validate_migration)
-- 설명: ERROR는 적재에서 제외되고 INFO는 안내만 남긴다
-- =====================================================================================
CREATE OR REPLACE PROCEDURE sp_validate_migration(
    p_run_id BIGINT,                                                   -- 이관 작업 ID
    INOUT o_error_count INT DEFAULT 0,                                 -- 반환: 검증 오류(ERROR) 건수
    INOUT o_info_count INT DEFAULT 0                                   -- 반환: 검증 안내(INFO) 건수
)
LANGUAGE plpgsql AS $$
BEGIN
    -- 이전 검증 결과 삭제 (재실행 시 다시 계산)
    DELETE FROM migration_issue WHERE migration_run_id = p_run_id;

    -- 사용자: 필수값 누락
    INSERT INTO migration_issue (migration_run_id, table_name, row_no, source_key, issue_type, severity, message)
    SELECT p_run_id, 'stg_user', s.row_no, concat_ws('/', trim(s.inst_code), trim(s.legacy_user_id)), 'REQUIRED_MISSING', 'ERROR',
           '필수값 누락: ' || concat_ws(', ',
               CASE WHEN nullif(trim(s.inst_code), '') IS NULL THEN '기관 코드' END,
               CASE WHEN nullif(trim(s.legacy_user_id), '') IS NULL THEN '사용자 ID' END,
               CASE WHEN nullif(trim(s.user_name), '') IS NULL THEN '사용자 이름' END)
      FROM stg_user s
     WHERE s.migration_run_id = p_run_id
       AND (nullif(trim(s.inst_code), '') IS NULL OR nullif(trim(s.legacy_user_id), '') IS NULL OR nullif(trim(s.user_name), '') IS NULL);

    -- 사용자: 원본 키(기관 코드 + 사용자 ID) 중복
    INSERT INTO migration_issue (migration_run_id, table_name, row_no, source_key, issue_type, severity, message)
    SELECT p_run_id, 'stg_user', d.row_no, d.source_key, 'DUPLICATE_SOURCE_KEY', 'ERROR', '원본 키 중복: ' || d.source_key || ' (' || d.cnt || '건)'
      FROM (SELECT s.row_no, trim(s.inst_code) || '/' || trim(s.legacy_user_id) AS source_key,
                   count(*) OVER (PARTITION BY trim(s.inst_code), trim(s.legacy_user_id)) AS cnt
              FROM stg_user s
             WHERE s.migration_run_id = p_run_id
               AND nullif(trim(s.inst_code), '') IS NOT NULL AND nullif(trim(s.legacy_user_id), '') IS NOT NULL) d
     WHERE d.cnt > 1;

    -- 과제: 필수값 누락
    INSERT INTO migration_issue (migration_run_id, table_name, row_no, source_key, issue_type, severity, message)
    SELECT p_run_id, 'stg_project', p.row_no, concat_ws('/', trim(p.inst_code), trim(p.legacy_research_no)), 'REQUIRED_MISSING', 'ERROR',
           '필수값 누락: ' || concat_ws(', ',
               CASE WHEN nullif(trim(p.inst_code), '') IS NULL THEN '기관 코드' END,
               CASE WHEN nullif(trim(p.legacy_research_no), '') IS NULL THEN '연구번호' END,
               CASE WHEN nullif(trim(p.title), '') IS NULL THEN '과제명' END)
      FROM stg_project p
     WHERE p.migration_run_id = p_run_id
       AND (nullif(trim(p.inst_code), '') IS NULL OR nullif(trim(p.legacy_research_no), '') IS NULL OR nullif(trim(p.title), '') IS NULL);

    -- 과제: 원본 키(기관 코드 + 연구번호) 중복
    INSERT INTO migration_issue (migration_run_id, table_name, row_no, source_key, issue_type, severity, message)
    SELECT p_run_id, 'stg_project', d.row_no, d.source_key, 'DUPLICATE_SOURCE_KEY', 'ERROR', '원본 키 중복: ' || d.source_key || ' (' || d.cnt || '건)'
      FROM (SELECT p.row_no, trim(p.inst_code) || '/' || trim(p.legacy_research_no) AS source_key,
                   count(*) OVER (PARTITION BY trim(p.inst_code), trim(p.legacy_research_no)) AS cnt
              FROM stg_project p
             WHERE p.migration_run_id = p_run_id
               AND nullif(trim(p.inst_code), '') IS NOT NULL AND nullif(trim(p.legacy_research_no), '') IS NOT NULL) d
     WHERE d.cnt > 1;

    -- 과제: 날짜 형식 오류
    INSERT INTO migration_issue (migration_run_id, table_name, row_no, source_key, issue_type, severity, message)
    SELECT p_run_id, 'stg_project', p.row_no, trim(p.inst_code) || '/' || trim(p.legacy_research_no), 'INVALID_FORMAT', 'ERROR',
           '날짜 형식 오류: 시작일 ' || coalesce(p.start_date, '-') || ', 종료일 ' || coalesce(p.end_date, '-')
      FROM stg_project p
     WHERE p.migration_run_id = p_run_id
       AND (fn_is_invalid_date(p.start_date) OR fn_is_invalid_date(p.end_date));

    -- 과제: 책임연구자 매핑 실패
    INSERT INTO migration_issue (migration_run_id, table_name, row_no, source_key, issue_type, severity, message)
    SELECT p_run_id, 'stg_project', p.row_no, trim(p.inst_code) || '/' || trim(p.legacy_research_no), 'USER_MAPPING_FAILED', 'ERROR',
           '책임연구자 ' || trim(p.pi_legacy_user_id) || ' 매핑 실패: 구시스템 사용자가 없거나 사용자 데이터에 오류가 있습니다.'
      FROM stg_project p
     WHERE p.migration_run_id = p_run_id
       AND nullif(trim(p.pi_legacy_user_id), '') IS NOT NULL
       AND NOT fn_user_resolvable(p_run_id, p.inst_code, p.pi_legacy_user_id);

    -- 심의 신청: 필수값 누락
    INSERT INTO migration_issue (migration_run_id, table_name, row_no, source_key, issue_type, severity, message)
    SELECT p_run_id, 'stg_submission', a.row_no, concat_ws('/', trim(a.inst_code), trim(a.legacy_regno)), 'REQUIRED_MISSING', 'ERROR',
           '필수값 누락: ' || concat_ws(', ',
               CASE WHEN nullif(trim(a.inst_code), '') IS NULL THEN '기관 코드' END,
               CASE WHEN nullif(trim(a.legacy_regno), '') IS NULL THEN '신청번호' END,
               CASE WHEN nullif(trim(a.applicant_legacy_user_id), '') IS NULL THEN '신청자' END,
               CASE WHEN nullif(trim(a.title), '') IS NULL THEN '신청 제목' END,
               CASE WHEN nullif(trim(a.legacy_status_code), '') IS NULL THEN '상태 코드' END)
      FROM stg_submission a
     WHERE a.migration_run_id = p_run_id
       AND (nullif(trim(a.inst_code), '') IS NULL OR nullif(trim(a.legacy_regno), '') IS NULL
            OR nullif(trim(a.applicant_legacy_user_id), '') IS NULL OR nullif(trim(a.title), '') IS NULL
            OR nullif(trim(a.legacy_status_code), '') IS NULL);

    -- 심의 신청: 원본 키(기관 코드 + 신청번호) 중복
    INSERT INTO migration_issue (migration_run_id, table_name, row_no, source_key, issue_type, severity, message)
    SELECT p_run_id, 'stg_submission', d.row_no, d.source_key, 'DUPLICATE_SOURCE_KEY', 'ERROR',
           '원본 키 중복: ' || d.source_key || ' (' || d.cnt || '건). 어느 행이 맞는지 구시스템 담당자 확인이 필요합니다.'
      FROM (SELECT a.row_no, trim(a.inst_code) || '/' || trim(a.legacy_regno) AS source_key,
                   count(*) OVER (PARTITION BY trim(a.inst_code), trim(a.legacy_regno)) AS cnt
              FROM stg_submission a
             WHERE a.migration_run_id = p_run_id
               AND nullif(trim(a.inst_code), '') IS NOT NULL AND nullif(trim(a.legacy_regno), '') IS NOT NULL) d
     WHERE d.cnt > 1;

    -- 심의 신청: 일시 형식 오류
    INSERT INTO migration_issue (migration_run_id, table_name, row_no, source_key, issue_type, severity, message)
    SELECT p_run_id, 'stg_submission', a.row_no, trim(a.inst_code) || '/' || trim(a.legacy_regno), 'INVALID_FORMAT', 'ERROR',
           '일시 형식 오류: 신청 ' || coalesce(a.submitted_at, '-') || ', 수정 ' || coalesce(a.updated_at, '-')
      FROM stg_submission a
     WHERE a.migration_run_id = p_run_id
       AND (fn_is_invalid_timestamp(a.submitted_at) OR fn_is_invalid_timestamp(a.updated_at));

    -- 심의 신청: 상태 코드 매핑 실패
    INSERT INTO migration_issue (migration_run_id, table_name, row_no, source_key, issue_type, severity, message)
    SELECT p_run_id, 'stg_submission', a.row_no, trim(a.inst_code) || '/' || trim(a.legacy_regno), 'CODE_MAPPING_FAILED', 'ERROR',
           '상태 코드 ' || trim(a.legacy_status_code) || '에 대응하는 새 상태가 없습니다.'
      FROM stg_submission a
     WHERE a.migration_run_id = p_run_id
       AND nullif(trim(a.legacy_status_code), '') IS NOT NULL
       AND fn_map_code('APPL_STATUS', a.inst_code, a.legacy_status_code) IS NULL;

    -- 심의 신청: 구시스템부터 과제 연결이 없고 처리 기준이 있는 건 (가상 과제 연결 안내)
    INSERT INTO migration_issue (migration_run_id, table_name, row_no, source_key, issue_type, severity, message)
    SELECT p_run_id, 'stg_submission', a.row_no, trim(a.inst_code) || '/' || trim(a.legacy_regno), 'VIRTUAL_PROJECT_LINKED', 'INFO',
           '구시스템부터 과제 연결이 없음. 처리 기준에 따라 가상 과제 ' || fn_virtual_project_no(a.inst_code) || '에 연결: ' || r.reason
      FROM stg_submission a
      JOIN mig_virtual_project_rule r ON r.inst_code = trim(a.inst_code) AND r.legacy_regno = trim(a.legacy_regno)
     WHERE a.migration_run_id = p_run_id
       AND nullif(trim(a.legacy_research_no), '') IS NULL;

    -- 심의 신청: 구시스템부터 과제 연결이 없고 처리 기준도 없는 건
    INSERT INTO migration_issue (migration_run_id, table_name, row_no, source_key, issue_type, severity, message)
    SELECT p_run_id, 'stg_submission', a.row_no, trim(a.inst_code) || '/' || trim(a.legacy_regno), 'NO_PROJECT_IN_SOURCE', 'ERROR',
           '구시스템부터 과제 연결이 없고, 가상 과제 처리 기준도 없습니다.'
      FROM stg_submission a
     WHERE a.migration_run_id = p_run_id
       AND nullif(trim(a.legacy_research_no), '') IS NULL
       AND NOT EXISTS (SELECT 1 FROM mig_virtual_project_rule r WHERE r.inst_code = trim(a.inst_code) AND r.legacy_regno = trim(a.legacy_regno));

    -- 심의 신청: 구 과제번호는 있지만 매핑에 실패한 건
    INSERT INTO migration_issue (migration_run_id, table_name, row_no, source_key, issue_type, severity, message)
    SELECT p_run_id, 'stg_submission', a.row_no, trim(a.inst_code) || '/' || trim(a.legacy_regno), 'PROJECT_MAPPING_FAILED', 'ERROR',
           '구 과제번호 ' || trim(a.legacy_research_no) || '는 있지만 새 과제로 매핑하지 못했습니다.'
      FROM stg_submission a
     WHERE a.migration_run_id = p_run_id
       AND nullif(trim(a.legacy_research_no), '') IS NOT NULL
       AND NOT fn_project_resolvable(p_run_id, a.inst_code, a.legacy_research_no);

    -- 심의 신청: 신청자 매핑 실패
    INSERT INTO migration_issue (migration_run_id, table_name, row_no, source_key, issue_type, severity, message)
    SELECT p_run_id, 'stg_submission', a.row_no, trim(a.inst_code) || '/' || trim(a.legacy_regno), 'USER_MAPPING_FAILED', 'ERROR',
           '신청자 ' || trim(a.applicant_legacy_user_id) || ' 매핑 실패: 구시스템 사용자가 없거나 사용자 데이터에 오류가 있습니다.'
      FROM stg_submission a
     WHERE a.migration_run_id = p_run_id
       AND nullif(trim(a.applicant_legacy_user_id), '') IS NOT NULL
       AND NOT fn_user_resolvable(p_run_id, a.inst_code, a.applicant_legacy_user_id);

    -- 심의 이력: 필수값 누락
    INSERT INTO migration_issue (migration_run_id, table_name, row_no, source_key, issue_type, severity, message)
    SELECT p_run_id, 'stg_review', r.row_no, concat_ws('/', trim(r.inst_code), trim(r.legacy_review_id)), 'REQUIRED_MISSING', 'ERROR',
           '필수값 누락: ' || concat_ws(', ',
               CASE WHEN nullif(trim(r.inst_code), '') IS NULL THEN '기관 코드' END,
               CASE WHEN nullif(trim(r.legacy_review_id), '') IS NULL THEN '심의 ID' END,
               CASE WHEN nullif(trim(r.legacy_regno), '') IS NULL THEN '신청번호' END,
               CASE WHEN nullif(trim(r.result_code), '') IS NULL THEN '심의 결과' END)
      FROM stg_review r
     WHERE r.migration_run_id = p_run_id
       AND (nullif(trim(r.inst_code), '') IS NULL OR nullif(trim(r.legacy_review_id), '') IS NULL
            OR nullif(trim(r.legacy_regno), '') IS NULL OR nullif(trim(r.result_code), '') IS NULL);

    -- 심의 이력: 원본 키(기관 코드 + 심의 ID) 중복
    INSERT INTO migration_issue (migration_run_id, table_name, row_no, source_key, issue_type, severity, message)
    SELECT p_run_id, 'stg_review', d.row_no, d.source_key, 'DUPLICATE_SOURCE_KEY', 'ERROR', '원본 키 중복: ' || d.source_key || ' (' || d.cnt || '건)'
      FROM (SELECT r.row_no, trim(r.inst_code) || '/' || trim(r.legacy_review_id) AS source_key,
                   count(*) OVER (PARTITION BY trim(r.inst_code), trim(r.legacy_review_id)) AS cnt
              FROM stg_review r
             WHERE r.migration_run_id = p_run_id
               AND nullif(trim(r.inst_code), '') IS NOT NULL AND nullif(trim(r.legacy_review_id), '') IS NOT NULL) d
     WHERE d.cnt > 1;

    -- 심의 이력: 일시 형식 오류
    INSERT INTO migration_issue (migration_run_id, table_name, row_no, source_key, issue_type, severity, message)
    SELECT p_run_id, 'stg_review', r.row_no, trim(r.inst_code) || '/' || trim(r.legacy_review_id), 'INVALID_FORMAT', 'ERROR',
           '심의 일시 형식 오류: ' || r.reviewed_at
      FROM stg_review r
     WHERE r.migration_run_id = p_run_id
       AND fn_is_invalid_timestamp(r.reviewed_at);

    -- 심의 이력: 심의 결과 코드 매핑 실패
    INSERT INTO migration_issue (migration_run_id, table_name, row_no, source_key, issue_type, severity, message)
    SELECT p_run_id, 'stg_review', r.row_no, trim(r.inst_code) || '/' || trim(r.legacy_review_id), 'CODE_MAPPING_FAILED', 'ERROR',
           '심의 결과 코드 ' || trim(r.result_code) || '에 대응하는 새 결과가 없습니다.'
      FROM stg_review r
     WHERE r.migration_run_id = p_run_id
       AND nullif(trim(r.result_code), '') IS NOT NULL
       AND fn_map_code('REVIEW_RESULT', r.inst_code, r.result_code) IS NULL;

    -- 심의 이력: 심의위원 매핑 실패
    INSERT INTO migration_issue (migration_run_id, table_name, row_no, source_key, issue_type, severity, message)
    SELECT p_run_id, 'stg_review', r.row_no, trim(r.inst_code) || '/' || trim(r.legacy_review_id), 'USER_MAPPING_FAILED', 'ERROR',
           '심의위원 ' || trim(r.reviewer_legacy_user_id) || ' 매핑 실패: 구시스템 사용자가 없거나 사용자 데이터에 오류가 있습니다.'
      FROM stg_review r
     WHERE r.migration_run_id = p_run_id
       AND nullif(trim(r.reviewer_legacy_user_id), '') IS NOT NULL
       AND NOT fn_user_resolvable(p_run_id, r.inst_code, r.reviewer_legacy_user_id);

    -- 심의 이력: 신청이 적재되지 않은 건
    INSERT INTO migration_issue (migration_run_id, table_name, row_no, source_key, issue_type, severity, message)
    SELECT p_run_id, 'stg_review', r.row_no, trim(r.inst_code) || '/' || trim(r.legacy_review_id), 'PARENT_NOT_LOADED', 'ERROR',
           '신청 ' || trim(r.inst_code) || '/' || trim(r.legacy_regno) || '이(가) 적재되지 않아 심의 이력을 넣을 수 없습니다.'
      FROM stg_review r
     WHERE r.migration_run_id = p_run_id
       AND nullif(trim(r.legacy_regno), '') IS NOT NULL
       AND NOT fn_application_resolvable(p_run_id, r.inst_code, r.legacy_regno);

    -- 오류·안내 건수 반환
    SELECT count(*) FILTER (WHERE severity = 'ERROR'), count(*) FILTER (WHERE severity = 'INFO')
      INTO o_error_count, o_info_count
      FROM migration_issue
     WHERE migration_run_id = p_run_id;
END;
$$;
