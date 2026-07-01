package com.kochat.adapter.outbound.persistence.outbox

import com.kochat.domain.messaging.model.OutboxEventStatus
import jakarta.persistence.LockModeType
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query

interface OutboxEventJpaRepository : JpaRepository<OutboxEventJpaEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        SELECT event FROM OutboxEventJpaEntity event
        WHERE event.status = :status
        ORDER BY event.createdAt ASC
        """,
    )
    fun findPendingForRelay(status: OutboxEventStatus, pageable: Pageable): List<OutboxEventJpaEntity>

    fun countByStatus(status: OutboxEventStatus): Long
}
