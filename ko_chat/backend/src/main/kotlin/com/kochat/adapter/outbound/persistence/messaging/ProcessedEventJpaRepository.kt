package com.kochat.adapter.outbound.persistence.messaging

import org.springframework.data.jpa.repository.JpaRepository

interface ProcessedEventJpaRepository : JpaRepository<ProcessedEventJpaEntity, Long> {
    fun existsByEventIdAndConsumerName(eventId: String, consumerName: String): Boolean

    fun countByConsumerName(consumerName: String): Long
}
