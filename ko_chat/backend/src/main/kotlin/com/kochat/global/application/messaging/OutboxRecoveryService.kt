package com.kochat.global.application.messaging

import com.kochat.adapter.outbound.persistence.outbox.OutboxEventJpaRepository
import com.kochat.domain.messaging.model.OutboxEventStatus
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@ConditionalOnProperty(prefix = "app.kafka", name = ["enabled"], havingValue = "true")
class OutboxRecoveryService(
    private val outboxEventJpaRepository: OutboxEventJpaRepository,
) {
    @Transactional
    fun requeueFailed(limit: Int): Int {
        val failedEvents = outboxEventJpaRepository.findPendingForRelay(
            OutboxEventStatus.FAILED,
            PageRequest.of(0, limit),
        )

        failedEvents.forEach { event ->
            event.status = OutboxEventStatus.PENDING
            event.retryCount = 0
            event.lastError = null
        }
        return failedEvents.size
    }
}
