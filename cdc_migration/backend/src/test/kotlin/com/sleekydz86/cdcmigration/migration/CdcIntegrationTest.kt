package com.sleekydz86.cdcmigration.migration

import com.sleekydz86.cdcmigration.migration.application.CdcEventRepository
import com.sleekydz86.cdcmigration.migration.application.CdcRunRepository
import com.sleekydz86.cdcmigration.migration.application.MigrationRunRepository
import com.sleekydz86.cdcmigration.migration.application.cdc.CdcCoordinator
import com.sleekydz86.cdcmigration.migration.domain.MigrationMode
import com.sleekydz86.cdcmigration.migration.domain.cdc.CdcRun
import com.sleekydz86.cdcmigration.migration.domain.cdc.CdcRunStatus
import com.sleekydz86.cdcmigration.support.IntegrationTestSupport
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import tools.jackson.databind.json.JsonMapper
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset

@DisplayName("CDC 통합 테스트 (PostgreSQL + Kafka, Debezium 형식 이벤트)")
class CdcIntegrationTest : IntegrationTestSupport() {

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @Autowired
    private lateinit var runs: MigrationRunRepository

    @Autowired
    private lateinit var cdcRuns: CdcRunRepository

    @Autowired
    private lateinit var events: CdcEventRepository

    @Autowired
    private lateinit var coordinator: CdcCoordinator

    private val json = JsonMapper.builder().build()
    private lateinit var run: CdcRun
    private var lsn = 1000L

    @BeforeEach
    fun startRun() {
        resetTarget()
        run = cdcRuns.create(runs.create(MigrationMode.CDC, "CDC 통합 테스트").id)
    }

    @Test
    @DisplayName("완료 알림이 와도 다른 토픽의 스냅샷 이벤트가 남아 있으면 기다렸다가, 모두 스테이징에 반영된 뒤 초기 적재한다")
    fun waitsForAllSnapshotEventsBeforeInitialLoad() {
        sendSnapshotExceptLastReview()
        sendScanNotifications()

        await().pollDelay(Duration.ofSeconds(2)).atMost(Duration.ofSeconds(20)).until { status() == CdcRunStatus.SNAPSHOTTING && completedNotified() }
        val blockers = coordinator.gate(cdcRuns.find(run.id)!!).blockers()
        sendSnapshot("tb_review", review("V-0002", "1220", "M"))

        assertThat(blockers).anySatisfy { assertThat(it).contains("심의 이력(tb_review) 스냅샷 이벤트 1/2건") }
        await().atMost(Duration.ofSeconds(30)).until { status() == CdcRunStatus.STREAMING }
        assertThat(count("SELECT count(*) FROM sub_mastinfo")).isEqualTo(1)
        assertThat(count("SELECT count(*) FROM sub_review")).isEqualTo(2)
    }

    @Test
    @DisplayName("초기 적재 후 구 신청 1220이 접수에서 승인으로 바뀌면 새 신청번호가 달라도 같은 행이 갱신된다")
    fun appliesStatusChangeByRegnoold() {
        startStreaming()
        val subNo = subNo("1220")

        sendChange("tb_appl", "u", application("1220", "01"), application("1220", "03", updatedAt = "2026-11-11T10:00:00"))

        await().atMost(Duration.ofSeconds(20)).until { status("1220") == "APPROVED" }
        assertThat(subNo("1220")).isEqualTo(subNo)
        assertThat(subNo).isGreaterThanOrEqualTo(8501)
        assertThat(count("SELECT count(*) FROM work_log WHERE procedure_nm = 'sp_submission_cud' AND worker = 'CDC'")).isEqualTo(1)
    }

    @Test
    @DisplayName("같은 이벤트를 다시 받으면 처리 기록으로 걸러 한 번만 반영한다")
    fun ignoresDuplicateEvent() {
        startStreaming()
        val change = changeMessage("tb_appl", "u", application("1220", "01"), application("1220", "02", updatedAt = "2026-11-11T09:00:00"), 5555L)

        kafkaTemplate.send("${run.topicPrefix}.public.tb_appl", change).get()
        kafkaTemplate.send("${run.topicPrefix}.public.tb_appl", change).get()

        await().atMost(Duration.ofSeconds(20)).until { events.duplicateTotal(run.id) == 1 }
        assertThat(status("1220")).isEqualTo("UNDER_REVIEW")
        assertThat(count("SELECT version FROM sub_mastinfo WHERE regnoold = '1220'")).isEqualTo(1)
        assertThat(count("SELECT count(*) FROM cdc_event WHERE cdc_run_id = ? AND NOT snapshot", run.id)).isEqualTo(1)
    }

    @Test
    @DisplayName("신청보다 심의 이력이 먼저 오면 재처리 대기로 남겼다가 신청이 반영된 뒤 적용한다")
    fun reviewArrivingBeforeApplicationWaits() {
        startStreaming()

        sendChange("tb_review", "c", null, review("V-0100", "1230", "A"))
        await().atMost(Duration.ofSeconds(20)).until { eventStatus("V-0100") == "WAITING" }
        sendChange("tb_appl", "c", null, application("1230", "01", seq = 30))

        await().atMost(Duration.ofSeconds(30)).until { eventStatus("V-0100") == "APPLIED" }
        assertThat(count("SELECT count(*) FROM sub_review r JOIN sub_mastinfo m ON m.sub_no = r.sub_no WHERE m.regnoold = '1230'")).isEqualTo(1)
    }

    private fun startStreaming() {
        sendSnapshotExceptLastReview()
        sendSnapshot("tb_review", review("V-0002", "1220", "M"))
        sendScanNotifications()
        await().atMost(Duration.ofSeconds(30)).until { status() == CdcRunStatus.STREAMING }
    }

    private fun sendSnapshotExceptLastReview() {
        sendSnapshot("tb_user", mapOf("inst_code" to "H01", "user_id" to "u1001", "user_name" to "김하늘", "email" to null, "dept_name" to "내분비내과"))
        sendSnapshot("tb_user", mapOf("inst_code" to "H01", "user_id" to "r2001", "user_name" to "최심의", "email" to null, "dept_name" to "심의 사무국"))
        sendSnapshot("tb_project", mapOf("inst_code" to "H01", "research_no" to "R-2019-001", "title" to "식이 기록 관찰 연구", "pi_user_id" to "u1001", "start_date" to 17956, "end_date" to null))
        sendSnapshot("tb_appl", application("1220", "01"))
        sendSnapshot("tb_review", review("V-0001", "1220", "A"))
    }

    private fun sendScanNotifications() {
        val expected = mapOf("tb_user" to 2, "tb_project" to 1, "tb_appl" to 1, "tb_review" to 2, "tb_attach" to 0)
        expected.forEach { (table, rows) -> sendNotification("TABLE_SCAN_COMPLETED", "public.$table", rows) }
        sendNotification("COMPLETED", null, null)
    }

    private fun application(regno: String, status: String, updatedAt: String = "2019-09-02T14:20:00", seq: Int = 3): Map<String, Any?> = mapOf(
        "appl_seq" to seq, "inst_code" to "H01", "regno" to regno, "research_no" to "R-2019-001", "applicant_id" to "u1001",
        "title" to "식이 기록 관찰 연구 $regno", "status_code" to status, "submitted_at" to millis("2019-09-02T14:20:00"), "updated_at" to millis(updatedAt),
    )

    private fun review(reviewId: String, regno: String, result: String): Map<String, Any?> = mapOf(
        "inst_code" to "H01", "review_id" to reviewId, "regno" to regno, "reviewer_id" to "r2001", "result_code" to result,
        "opinion" to "합성 의견", "reviewed_at" to millis("2019-09-10T10:00:00"),
    )

    private fun millis(text: String): Long = LocalDateTime.parse(text).toInstant(ZoneOffset.UTC).toEpochMilli()

    private fun sendSnapshot(table: String, row: Map<String, Any?>) {
        kafkaTemplate.send("${run.topicPrefix}.public.$table", message(table, "r", null, row, null, "true")).get()
    }

    private fun sendChange(table: String, op: String, before: Map<String, Any?>?, after: Map<String, Any?>?) {
        kafkaTemplate.send("${run.topicPrefix}.public.$table", changeMessage(table, op, before, after, ++lsn)).get()
    }

    private fun changeMessage(table: String, op: String, before: Map<String, Any?>?, after: Map<String, Any?>?, sourceLsn: Long): String =
        message(table, op, before, after, sourceLsn, "false")

    private fun message(table: String, op: String, before: Map<String, Any?>?, after: Map<String, Any?>?, sourceLsn: Long?, snapshot: String): String =
        json.writeValueAsString(
            mapOf(
                "before" to before,
                "after" to after,
                "source" to mapOf("connector" to "postgresql", "name" to run.topicPrefix, "schema" to "public", "table" to table, "lsn" to sourceLsn, "snapshot" to snapshot),
                "op" to op,
                "ts_ms" to System.currentTimeMillis(),
            ),
        )

    private fun sendNotification(type: String, collection: String?, rows: Int?) {
        val data = buildMap<String, String> {
            put("connector_name", run.connectorName)
            collection?.let { put("data_collection", it) }
            rows?.let { put("total_rows_scanned", it.toString()) }
        }
        kafkaTemplate.send(
            "${run.topicPrefix}.notifications",
            json.writeValueAsString(mapOf("id" to "n-$type-$collection", "aggregate_type" to "Initial Snapshot", "type" to type, "additional_data" to data)),
        ).get()
    }

    private fun status(): CdcRunStatus = cdcRuns.find(run.id)!!.status

    private fun completedNotified(): Boolean = cdcRuns.find(run.id)!!.snapshotCompleted

    private fun subNo(regno: String): Long = jdbc.queryForObject("SELECT sub_no FROM sub_mastinfo WHERE inst_code = 'H01' AND regnoold = ?", Long::class.java, regno)!!

    private fun status(regno: String): String? =
        jdbc.queryForList("SELECT status FROM sub_mastinfo WHERE inst_code = 'H01' AND regnoold = ?", String::class.java, regno).firstOrNull()

    private fun eventStatus(reviewId: String): String? =
        jdbc.queryForList("SELECT status FROM cdc_event WHERE cdc_run_id = ? AND table_name = 'tb_review' AND source_key = ?", String::class.java, run.id, "H01/$reviewId")
            .firstOrNull()
}
