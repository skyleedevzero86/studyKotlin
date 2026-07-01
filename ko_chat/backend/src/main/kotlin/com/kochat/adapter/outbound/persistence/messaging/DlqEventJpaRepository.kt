package com.kochat.adapter.outbound.persistence.messaging

import org.springframework.data.jpa.repository.JpaRepository

interface DlqEventJpaRepository : JpaRepository<DlqEventJpaEntity, Long> {
    fun countByStatus(status: DlqEventStatus): Long
}
