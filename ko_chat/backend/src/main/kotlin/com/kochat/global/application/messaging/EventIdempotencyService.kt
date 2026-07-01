package com.kochat.global.application.messaging

import com.kochat.adapter.outbound.persistence.messaging.ProcessedEventJpaEntity
import com.kochat.adapter.outbound.persistence.messaging.ProcessedEventJpaRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class EventIdempotencyService(
    private val processedEventJpaRepository: ProcessedEventJpaRepository,
) {
    fun isProcessed(eventId: String, consumerName: String): Boolean =
        processedEventJpaRepository.existsByEventIdAndConsumerName(eventId, consumerName)

    @Transactional
    fun markProcessed(eventId: String, consumerName: String): Boolean {
        if (isProcessed(eventId, consumerName)) {
            return false
        }

        return try {
            processedEventJpaRepository.save(
                ProcessedEventJpaEntity().apply {
                    this.eventId = eventId
                    this.consumerName = consumerName
                    this.processedAt = LocalDateTime.now()
                },
            )
            true
        } catch (_: DataIntegrityViolationException) {
            false
        }
    }
}
