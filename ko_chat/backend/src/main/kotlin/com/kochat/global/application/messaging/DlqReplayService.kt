package com.kochat.global.application.messaging

import com.kochat.adapter.outbound.persistence.messaging.DlqEventJpaRepository
import com.kochat.adapter.outbound.persistence.messaging.DlqEventStatus
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@ConditionalOnProperty(prefix = "app.kafka", name = ["enabled"], havingValue = "true")
class DlqReplayService(
    private val dlqEventJpaRepository: DlqEventJpaRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>,
) {
    @Transactional
    fun replay(dlqEventId: Long): Boolean {
        val dlqEvent = dlqEventJpaRepository.findById(dlqEventId).orElse(null) ?: return false
        if (dlqEvent.status != DlqEventStatus.OPEN) {
            return false
        }

        val sourceTopic = dlqEvent.sourceTopic.removeSuffix(".dlq")
        kafkaTemplate.send(
            sourceTopic,
            dlqEvent.recordKey ?: "",
            dlqEvent.payload,
        ).get()

        dlqEvent.status = DlqEventStatus.REPLAYED
        dlqEvent.replayedAt = LocalDateTime.now()
        return true
    }
}
