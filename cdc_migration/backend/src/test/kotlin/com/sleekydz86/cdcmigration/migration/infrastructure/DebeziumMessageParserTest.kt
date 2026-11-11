package com.sleekydz86.cdcmigration.migration.infrastructure

import com.sleekydz86.cdcmigration.migration.domain.cdc.ChangeOperation
import com.sleekydz86.cdcmigration.migration.domain.cdc.LegacyTable
import com.sleekydz86.cdcmigration.migration.infrastructure.kafka.DebeziumMessageParser
import com.sleekydz86.cdcmigration.migration.infrastructure.kafka.ParsedMessage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Debezium 메시지 해석")
class DebeziumMessageParserTest {

    private val parser = DebeziumMessageParser()

    @Test
    @DisplayName("변경 이벤트의 일시·날짜 숫자 값을 CSV와 같은 문자열 형식으로 바꾼다")
    fun convertsTemporalValues() {
        val value = """
            {"before":null,"after":{"appl_seq":3,"inst_code":"H01","regno":"1220","status_code":"01","submitted_at":1567434000000,"updated_at":null},
             "source":{"table":"tb_appl","lsn":123456,"snapshot":"false"},"op":"c"}
        """.trimIndent()

        val parsed = parser.parse("legacy_r7.public.tb_appl", 0, 10, value) as ParsedMessage.Change

        assertThat(parsed.event.topicPrefix).isEqualTo("legacy_r7")
        assertThat(parsed.event.table).isEqualTo(LegacyTable.SUBMISSION)
        assertThat(parsed.event.op).isEqualTo(ChangeOperation.CREATE)
        assertThat(parsed.event.row["submitted_at"]).isEqualTo("2019-09-02 14:20:00")
        assertThat(parsed.event.row["appl_seq"]).isEqualTo("3")
        assertThat(parsed.event.eventKey()).isEqualTo("c:tb_appl:3:123456")
    }

    @Test
    @DisplayName("스냅샷 알림 토픽의 테이블 스캔 완료와 건수를 읽는다")
    fun parsesNotification() {
        val value = """{"id":"1","aggregate_type":"Initial Snapshot","type":"TABLE_SCAN_COMPLETED","additional_data":{"data_collection":"legacy_db.public.tb_user","total_rows_scanned":"9"}}"""

        val parsed = parser.parse("legacy_r7.notifications", 0, 1, value) as ParsedMessage.Notification

        assertThat(parsed.notification.type).isEqualTo("TABLE_SCAN_COMPLETED")
        assertThat(parsed.notification.table()).isEqualTo(LegacyTable.USER)
        assertThat(parsed.notification.totalRows).isEqualTo(9L)
    }

    @Test
    @DisplayName("값이 없는 톰스톤 메시지는 건너뛴다")
    fun ignoresTombstone() {
        val value: String? = null

        val parsed = parser.parse("legacy_r7.public.tb_user", 0, 1, value)

        assertThat(parsed).isInstanceOf(ParsedMessage.Ignored::class.java)
    }
}
