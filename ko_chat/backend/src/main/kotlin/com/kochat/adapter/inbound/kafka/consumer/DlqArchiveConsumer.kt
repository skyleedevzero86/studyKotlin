package com.kochat.adapter.inbound.kafka.consumer

import com.kochat.adapter.outbound.persistence.messaging.DlqEventJpaEntity
import com.kochat.adapter.outbound.persistence.messaging.DlqEventJpaRepository
import com.kochat.domain.messaging.model.DlqEventStatus
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
@ConditionalOnProperty(prefix = "app.kafka", name = ["enabled"], havingValue = "true")
class DlqArchiveConsumer(
    private val dlqEventJpaRepository: DlqEventJpaRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = [
            "\${app.kafka.topics.message-events-dlq:chat.message.events.dlq}",
            "\${app.kafka.topics.attachment-events-dlq:chat.attachment.events.dlq}",
            "\${app.kafka.topics.outbox-dlq:chat.outbox.events.dlq}",
        ],
        groupId = "\${app.kafka.consumer-groups.dlq:ko-chat-dlq}",
        containerFactory = "kafkaListenerContainerFactory",
    )
    @Transactional
    fun consume(record: ConsumerRecord<String, String>) {
        val sourceTopic = record.topic()
        dlqEventJpaRepository.save(
            DlqEventJpaEntity().apply {
                this.sourceTopic = sourceTopic
                partitionNum = record.partition()
                recordOffset = record.offset()
                recordKey = record.key()
                payload = record.value()
                errorMessage = record.headers().lastHeader("kafka_dlt-exception-message")
                    ?.let { String(it.value()) }
                status = DlqEventStatus.OPEN
                receivedAt = LocalDateTime.now()
            },
        )
        logger.warn(
            "DLQ 보관 완료: topic={}, partition={}, offset={}",
            sourceTopic,
            record.partition(),
            record.offset(),
        )
    }
}
