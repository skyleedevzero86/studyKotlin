package com.kochat.global.application.messaging

import com.kochat.adapter.outbound.persistence.outbox.OutboxEventJpaEntity
import com.kochat.adapter.outbound.persistence.outbox.OutboxEventJpaRepository
import com.kochat.domain.messaging.model.OutboxEventStatus
import com.kochat.global.config.KafkaProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.domain.PageRequest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@ConditionalOnProperty(prefix = "app.kafka", name = ["enabled"], havingValue = "true")
class OutboxRelayService(
    private val outboxEventJpaRepository: OutboxEventJpaRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val kafkaProperties: KafkaProperties,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelayString = "\${app.kafka.outbox-relay-interval-ms:3000}")
    @Transactional
    fun relayPendingEvents() {
        val pendingEvents = outboxEventJpaRepository.findPendingForRelay(
            OutboxEventStatus.PENDING,
            PageRequest.of(0, kafkaProperties.outboxBatchSize),
        )

        pendingEvents.forEach { outboxEvent ->
            try {
                kafkaTemplate.send(
                    outboxEvent.topic,
                    outboxEvent.partitionKey,
                    outboxEvent.payload,
                ).get()

                outboxEvent.status = OutboxEventStatus.PUBLISHED
                outboxEvent.publishedAt = LocalDateTime.now()
                outboxEvent.lastError = null
            } catch (ex: Exception) {
                outboxEvent.retryCount += 1
                outboxEvent.lastError = ex.message?.take(1000)
                if (outboxEvent.retryCount >= kafkaProperties.outboxMaxRetries) {
                    outboxEvent.status = OutboxEventStatus.FAILED
                    publishOutboxDlq(outboxEvent)
                }
                logger.warn(
                    "Outbox relay 실패(eventId={}, topic={}, retry={}): {}",
                    outboxEvent.eventId,
                    outboxEvent.topic,
                    outboxEvent.retryCount,
                    ex.message,
                )
            }
        }
    }

    private fun publishOutboxDlq(outboxEvent: OutboxEventJpaEntity) {
        try {
            kafkaTemplate.send(
                kafkaProperties.topics.outboxDlq,
                outboxEvent.partitionKey,
                outboxEvent.payload,
            ).get()
        } catch (ex: Exception) {
            logger.error(
                "Outbox DLQ 발행 실패(eventId={}): {}",
                outboxEvent.eventId,
                ex.message,
            )
        }
    }
}
