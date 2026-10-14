package com.sleekydz86.youtubemsa.download.ledger

import java.time.Duration
import java.time.Instant

const val outboxRetentionDays = 7L
const val outboxPurgeBatchSize = 1000

fun purgeOutbox(now: Instant, deleteBatch: (Instant, Int) -> Int): Int {
    val cutoff = now.minus(Duration.ofDays(outboxRetentionDays))
    var total = 0
    while (true) {
        val deleted = deleteBatch(cutoff, outboxPurgeBatchSize)
        total += deleted
        if (deleted < outboxPurgeBatchSize) return total
    }
}
