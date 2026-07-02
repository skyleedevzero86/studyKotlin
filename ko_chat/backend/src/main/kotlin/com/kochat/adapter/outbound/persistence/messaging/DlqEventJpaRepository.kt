package com.kochat.adapter.outbound.persistence.messaging

import com.kochat.domain.messaging.model.DlqEventStatus
import org.springframework.data.jpa.repository.JpaRepository

interface DlqEventJpaRepository : JpaRepository<DlqEventJpaEntity, Long> {
    fun countByStatus(status: DlqEventStatus): Long
}
