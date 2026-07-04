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
import com.kochat.global.config.KafkaProperties
import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class MessagingOperationsService(
    private val outboxEventJpaRepository: OutboxEventJpaRepository,
    private val processedEventJpaRepository: ProcessedEventJpaRepository,
    private val dlqEventJpaRepository: DlqEventJpaRepository,
    private val kafkaProperties: KafkaProperties,
    private val kafkaLagMonitorService: ObjectProvider<KafkaLagMonitorService>,
    private val outboxRecoveryService: ObjectProvider<OutboxRecoveryService>,
    private val dlqReplayService: ObjectProvider<DlqReplayService>,
) {
    fun getSnapshot(): MessagingOperationsSnapshot {
        val kafkaEnabled = kafkaProperties.enabled
        val lag = if (kafkaEnabled) {
            kafkaLagMonitorService.ifAvailable?.collectLag()
        } else {
            null
        }

        return MessagingOperationsSnapshot(
            kafkaEnabled = kafkaEnabled,
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
            consumerLag = lag?.snapshots ?: emptyList(),
            lagHealthy = lag?.healthy ?: true,
            checkedAt = LocalDateTime.now(),
        )
    }

    @Transactional
    fun requeueFailedOutbox(limit: Int): Int {
        requireKafkaEnabled()
        return outboxRecoveryService.getObject().requeueFailed(limit)
    }

    @Transactional
    fun replayDlqEvent(dlqEventId: Long): Boolean {
        requireKafkaEnabled()
        return dlqReplayService.getObject().replay(dlqEventId)
    }

    private fun requireKafkaEnabled() {
        if (!kafkaProperties.enabled) {
            throw IllegalStateException("Kafka 메시징이 비활성화되어 있습니다.")
        }
    }
}
