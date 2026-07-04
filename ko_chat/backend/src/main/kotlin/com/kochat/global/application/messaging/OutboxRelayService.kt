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
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

@Service
@ConditionalOnProperty(prefix = "app.kafka", name = ["enabled"], havingValue = "true")
class OutboxRelayService(
    private val outboxEventJpaRepository: OutboxEventJpaRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val kafkaProperties: KafkaProperties,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val consecutiveFailures = AtomicInteger(0)
    private val circuitOpenUntil = AtomicLong(0)

    companion object {
        private const val CIRCUIT_OPEN_THRESHOLD = 3
        private const val CIRCUIT_COOLDOWN_MS = 30_000L
    }

    @Scheduled(fixedDelayString = "\${app.kafka.outbox-relay-interval-ms:3000}")
    @Transactional
    fun relayPendingEvents() {
        if (isCircuitOpen()) {
            return
        }

        val pendingEvents = outboxEventJpaRepository.findPendingForRelay(
            OutboxEventStatus.PENDING,
            PageRequest.of(0, kafkaProperties.outboxBatchSize),
        )

        if (pendingEvents.isEmpty()) return

        var batchFailed = false
        pendingEvents.forEach { outboxEvent ->
            if (batchFailed) return@forEach

            try {
                kafkaTemplate.send(
                    outboxEvent.topic,
                    outboxEvent.partitionKey,
                    outboxEvent.payload,
                ).get()

                outboxEvent.status = OutboxEventStatus.PUBLISHED
                outboxEvent.publishedAt = LocalDateTime.now()
                outboxEvent.lastError = null
                onSuccess()
            } catch (ex: Exception) {
                batchFailed = true
                outboxEvent.retryCount += 1
                outboxEvent.lastError = ex.message?.take(1000)
                if (outboxEvent.retryCount >= kafkaProperties.outboxMaxRetries) {
                    outboxEvent.status = OutboxEventStatus.FAILED
                    publishOutboxDlq(outboxEvent)
                }
                onFailure(ex)
            }
        }
    }

    private fun isCircuitOpen(): Boolean {
        val openUntil = circuitOpenUntil.get()
        if (openUntil == 0L) return false
        if (System.currentTimeMillis() >= openUntil) {
            circuitOpenUntil.set(0)
            logger.info("Kafka 서킷브레이커 HALF-OPEN: 연결 재시도")
            return false
        }
        return true
    }

    private fun onSuccess() {
        val prev = consecutiveFailures.getAndSet(0)
        if (prev >= CIRCUIT_OPEN_THRESHOLD) {
            logger.info("Kafka 연결 복구됨 — 서킷브레이커 CLOSED")
        }
    }

    private fun onFailure(ex: Exception) {
        val failures = consecutiveFailures.incrementAndGet()
        if (failures == CIRCUIT_OPEN_THRESHOLD) {
            circuitOpenUntil.set(System.currentTimeMillis() + CIRCUIT_COOLDOWN_MS)
            logger.warn(
                "Kafka 연결 불가 — 서킷브레이커 OPEN ({}초간 relay 중단): {}",
                CIRCUIT_COOLDOWN_MS / 1000,
                ex.message?.take(200),
            )
        } else if (failures < CIRCUIT_OPEN_THRESHOLD) {
            logger.warn("Outbox relay 실패(연속 {}/{}): {}", failures, CIRCUIT_OPEN_THRESHOLD, ex.message?.take(200))
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
