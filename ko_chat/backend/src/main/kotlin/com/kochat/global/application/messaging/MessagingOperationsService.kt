package com.kochat.global.application.messaging

import com.kochat.adapter.outbound.persistence.messaging.DlqEventJpaRepository
import com.kochat.adapter.outbound.persistence.messaging.ProcessedEventJpaRepository
import com.kochat.adapter.outbound.persistence.outbox.OutboxEventJpaRepository
import com.kochat.domain.messaging.model.DlqEventStatus
import com.kochat.domain.messaging.model.OutboxEventStatus
import com.kochat.global.application.messaging.dto.DlqStatusSnapshot
import com.kochat.global.application.messaging.dto.MessagingOperationsSnapshot
import com.kochat.global.application.messaging.dto.OutboxStatusSnapshot
import com.kochat.global.application.messaging.dto.ProcessedEventSnapshot
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@ConditionalOnProperty(prefix = "app.kafka", name = ["enabled"], havingValue = "true")
class MessagingOperationsService(
    private val outboxEventJpaRepository: OutboxEventJpaRepository,
    private val processedEventJpaRepository: ProcessedEventJpaRepository,
    private val dlqEventJpaRepository: DlqEventJpaRepository,
    private val kafkaLagMonitorService: KafkaLagMonitorService,
    private val outboxRecoveryService: OutboxRecoveryService,
    private val dlqReplayService: DlqReplayService,
) {
    fun getSnapshot(): MessagingOperationsSnapshot {
        val lag = kafkaLagMonitorService.collectLag()
        return MessagingOperationsSnapshot(
            outbox = OutboxStatusSnapshot(
                pending = outboxEventJpaRepository.countByStatus(OutboxEventStatus.PENDING),
                published = outboxEventJpaRepository.countByStatus(OutboxEventStatus.PUBLISHED),
                failed = outboxEventJpaRepository.countByStatus(OutboxEventStatus.FAILED),
            ),
            processedEvents = ProcessedEventSnapshot(
                total = processedEventJpaRepository.count(),
                byConsumer = mapOf(
                    "audit-consumer" to processedEventJpaRepository.countByConsumerName("audit-consumer"),
                    "search-index-consumer" to processedEventJpaRepository.countByConsumerName("search-index-consumer"),
                    "attachment-consumer" to processedEventJpaRepository.countByConsumerName("attachment-consumer"),
                    "milvus-index-consumer" to processedEventJpaRepository.countByConsumerName("milvus-index-consumer"),
                ),
            ),
            dlq = DlqStatusSnapshot(
                open = dlqEventJpaRepository.countByStatus(DlqEventStatus.OPEN),
                replayed = dlqEventJpaRepository.countByStatus(DlqEventStatus.REPLAYED),
            ),
            consumerLag = lag.snapshots,
            lagHealthy = lag.healthy,
            checkedAt = LocalDateTime.now(),
        )
    }

    @Transactional
    fun requeueFailedOutbox(limit: Int): Int = outboxRecoveryService.requeueFailed(limit)

    @Transactional
    fun replayDlqEvent(dlqEventId: Long): Boolean = dlqReplayService.replay(dlqEventId)
}
