package com.sleekydz86.cdcmigration.migration.domain

import com.sleekydz86.cdcmigration.migration.domain.cdc.LegacyTable
import com.sleekydz86.cdcmigration.migration.domain.cdc.SnapshotGate
import com.sleekydz86.cdcmigration.migration.domain.cdc.SnapshotTableProgress
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("초기 적재 시작 조건")
class SnapshotGateTest {

    private fun progress(staged: Map<LegacyTable, Long>, expected: Long = 2) =
        LegacyTable.entries.map { SnapshotTableProgress(it, expected, true, staged[it] ?: expected) }

    @Test
    @DisplayName("완료 알림이 없으면 시작하지 않는다")
    fun needsCompletedNotification() {
        val gate = SnapshotGate(false, progress(emptyMap()))

        val ready = gate.ready()

        assertThat(ready).isFalse()
        assertThat(gate.blockers()).containsExactly("Debezium 초기 스냅샷 완료 알림(COMPLETED)을 기다리는 중")
    }

    @Test
    @DisplayName("완료 알림이 와도 스캔 건수만큼 스테이징되지 않은 테이블이 있으면 시작하지 않는다")
    fun waitsForEveryTable() {
        val gate = SnapshotGate(true, progress(mapOf(LegacyTable.REVIEW to 1L)))

        val blockers = gate.blockers()

        assertThat(gate.ready()).isFalse()
        assertThat(blockers).containsExactly("심의 이력(tb_review) 스냅샷 이벤트 1/2건만 반영됨")
    }

    @Test
    @DisplayName("모든 테이블의 스캔 완료 알림과 건수가 맞으면 시작한다")
    fun readyWhenAllStaged() {
        val gate = SnapshotGate(true, progress(emptyMap()))

        val ready = gate.ready()

        assertThat(ready).isTrue()
    }
}
