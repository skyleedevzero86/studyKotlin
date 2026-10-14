package com.sleekydz86.youtubemsa.download.ledger

import com.sleekydz86.youtubemsa.download.persistence.OutboxRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Component
class OutboxRetentionJob(
    private val batch: OutboxRetentionBatch,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "0 20 4 * * *", zone = "Asia/Seoul")
    fun purgeExpired() {
        val removed = purgeOutbox(Instant.now(), batch::deleteOlderThan)
        if (removed == 0) return
        log.info("보관 기간이 지난 outbox 행을 지웠습니다. 건수={}", removed)
    }
}

@Component
class OutboxRetentionBatch(
    private val outbox: OutboxRepository,
) {
    @Transactional
    fun deleteOlderThan(cutoff: Instant, batchSize: Int): Int {
        val ids = outbox.findExpiredIds(cutoff, PageRequest.of(0, batchSize))
        if (ids.isEmpty()) return 0
        return outbox.deleteByIds(ids)
    }
}
