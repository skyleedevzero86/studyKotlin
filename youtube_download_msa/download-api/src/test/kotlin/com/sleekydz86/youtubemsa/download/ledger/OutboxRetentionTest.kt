package com.sleekydz86.youtubemsa.download.ledger

import org.junit.jupiter.api.DisplayName
import java.time.Duration
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

@DisplayName("단위: outbox 보관 삭제")
class OutboxRetentionTest {
    @Test
    @DisplayName("7일이 지나지 않은 행은 남긴다")
    fun keepsFreshRows() {
        val now = Instant.parse("2026-10-15T00:00:00Z")
        val rows = mutableListOf(Stored("fresh", now.minus(Duration.ofDays(6))))

        val removed = purgeOutbox(now) { cutoff, _ -> dropOlderThan(rows, cutoff) }

        assertEquals(0, removed)
        assertEquals(listOf("fresh"), rows.map { it.id })
    }

    @Test
    @DisplayName("지난 행만 1,000건씩 지우고 기준 시각의 행은 남긴다")
    fun deletesExpiredInBatches() {
        val now = Instant.parse("2026-10-15T00:00:00Z")
        val cutoff = now.minus(Duration.ofDays(outboxRetentionDays))
        val rows = mutableListOf<Stored>()
        repeat(outboxPurgeBatchSize + 1) { index ->
            rows.add(Stored("old-$index", cutoff.minusSeconds(index + 1L)))
        }
        rows.add(Stored("boundary", cutoff))
        val batchSizes = mutableListOf<Int>()

        val removed = purgeOutbox(now) { limit, batchSize ->
            assertEquals(cutoff, limit)
            assertEquals(outboxPurgeBatchSize, batchSize)
            val deleted = dropOlderThan(rows, limit, batchSize)
            batchSizes.add(deleted)
            deleted
        }

        assertEquals(listOf(outboxPurgeBatchSize, 1), batchSizes)
        assertEquals(outboxPurgeBatchSize + 1, removed)
        assertEquals(listOf("boundary"), rows.map { it.id })
    }
}

private data class Stored(val id: String, val createdAt: Instant)

private fun dropOlderThan(rows: MutableList<Stored>, cutoff: Instant, limit: Int = rows.size): Int {
    val doomed = rows.filter { it.createdAt < cutoff }.take(limit)
    rows.removeAll(doomed)
    return doomed.size
}
