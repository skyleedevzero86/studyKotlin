package com.sleekydz86.cdcmigration.migration

import com.sleekydz86.cdcmigration.global.demo.DemoFaults
import com.sleekydz86.cdcmigration.migration.application.MigrationQueryService
import com.sleekydz86.cdcmigration.migration.domain.MigrationCounts
import com.sleekydz86.cdcmigration.migration.domain.MigrationRunStatus
import com.sleekydz86.cdcmigration.support.IntegrationTestSupport
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@DisplayName("CSV 이관 통합 테스트 (PostgreSQL)")
class CsvMigrationIntegrationTest : IntegrationTestSupport() {

    @Autowired
    private lateinit var queries: MigrationQueryService

    @Autowired
    private lateinit var faults: DemoFaults

    @BeforeEach
    fun cleanTarget() {
        resetTarget()
    }

    @Test
    @DisplayName("샘플 CSV를 올리고 프로시저로 이관하면 검증 오류 행만 빠지고 사용자 → 과제 → 신청 → 심의 이력 순으로 적재된다")
    fun migratesSampleCsv() {
        val runId = createSampleRun()

        val run = execution.execute(runId)

        assertThat(run.status).isEqualTo(MigrationRunStatus.COMPLETED_WITH_ISSUES)
        assertThat(run.counts).isEqualTo(MigrationCounts(users = 8, projects = 6, submissions = 8, reviews = 6))
        assertThat(run.validation.errorCount).isEqualTo(10)
        assertThat(run.validation.infoCount).isEqualTo(1)
        assertThat(count("SELECT count(*) FROM sub_mastinfo WHERE regnoold = '1220'")).isEqualTo(2)
        assertThat(count("SELECT count(*) FROM sub_mastinfo m JOIN project p ON p.project_no = m.project_no WHERE m.regnoold = '1223' AND p.is_virtual")).isEqualTo(1)
    }

    @Test
    @DisplayName("적재 전에 중복 원본 키, 필수값 누락, 과제 미연결과 과제 매핑 실패를 구분해 사유를 남긴다")
    fun recordsIssueReasons() {
        val runId = createSampleRun()

        execution.execute(runId)
        val issues = queries.issues(runId, null, "ERROR").associate { it.sourceKey to it.issueType }

        assertThat(issues["H01/1228"]).isEqualTo("DUPLICATE_SOURCE_KEY")
        assertThat(issues["H01/1227"]).isEqualTo("REQUIRED_MISSING")
        assertThat(issues["H01/1224"]).isEqualTo("NO_PROJECT_IN_SOURCE")
        assertThat(issues["H01/1225"]).isEqualTo("PROJECT_MAPPING_FAILED")
        assertThat(issues["H01/1226"]).isEqualTo("USER_MAPPING_FAILED")
        assertThat(issues["H01/V-0006"]).isEqualTo("PARENT_NOT_LOADED")
        assertThat(queries.issues(runId, null, "INFO").single().sourceKey).isEqualTo("H01/1223")
    }

    @Test
    @DisplayName("같은 작업을 재실행하거나 같은 CSV로 새 작업을 돌려도 중복 행이 생기지 않고 최종 검증을 통과한다")
    fun rerunDoesNotDuplicate() {
        val firstRunId = createSampleRun()
        execution.execute(firstRunId)

        val rerun = execution.execute(firstRunId)
        val secondRun = execution.execute(createSampleRun())
        val report = queries.verify(firstRunId)

        assertThat(rerun.counts).isEqualTo(MigrationCounts())
        assertThat(secondRun.counts).isEqualTo(MigrationCounts())
        assertThat(count("SELECT count(*) FROM sub_mastinfo")).isEqualTo(8)
        assertThat(count("SELECT count(*) FROM sub_review")).isEqualTo(6)
        assertThat(report.passed).isTrue()
        assertThat(report.tables).allSatisfy { assertThat(it.missing).isZero() }
    }

    @Test
    @DisplayName("실행 중 오류가 나면 적재가 모두 롤백되고 실패 작업으로 남으며, 재처리하면 정상 적재된다")
    fun failedRunCanBeReprocessed() {
        val runId = createSampleRun()
        faults.arm(migration = true, contentSave = null)

        val failed = execution.execute(runId)
        val loadedAfterFailure = count("SELECT count(*) FROM sub_mastinfo")
        val retried = execution.execute(runId)

        assertThat(failed.status).isEqualTo(MigrationRunStatus.FAILED)
        assertThat(failed.errorMessage).contains("실습용 장애 주입")
        assertThat(loadedAfterFailure).isZero()
        assertThat(retried.status).isEqualTo(MigrationRunStatus.COMPLETED_WITH_ISSUES)
        assertThat(retried.attemptCount).isEqualTo(2)
        assertThat(count("SELECT count(*) FROM sub_mastinfo")).isEqualTo(8)
    }

    @Test
    @DisplayName("구 사용자 ID와 구 연구번호는 기관 코드와 함께 새 ID 체계로 매핑된다")
    fun storesMappings() {
        val runId = createSampleRun()

        execution.execute(runId)

        assertThat(count("SELECT count(*) FROM map_user WHERE legacy_user_id = 'u1001'")).isEqualTo(2)
        assertThat(count("SELECT count(*) FROM map_user WHERE user_id ~ '^U[0-9]{7}$'")).isEqualTo(8)
        assertThat(count("SELECT count(*) FROM map_project WHERE legacy_research_no = 'R-2019-001'")).isEqualTo(2)
        assertThat(count("SELECT count(*) FROM work_log")).isZero()
    }
}
