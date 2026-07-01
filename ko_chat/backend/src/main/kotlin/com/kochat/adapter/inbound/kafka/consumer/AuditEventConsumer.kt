package com.kochat.adapter.inbound.kafka.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.kochat.adapter.outbound.persistence.messaging.ChatAuditLogJpaEntity
import com.kochat.adapter.outbound.persistence.messaging.ChatAuditLogJpaRepository
import com.kochat.domain.messaging.model.ChatMessageSentEvent
import com.kochat.global.application.messaging.EventIdempotencyService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@ConditionalOnProperty(prefix = "app.kafka", name = ["enabled"], havingValue = "true")
class AuditEventConsumer(
    private val objectMapper: ObjectMapper,
    private val eventIdempotencyService: EventIdempotencyService,
    private val chatAuditLogJpaRepository: ChatAuditLogJpaRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val consumerName = "audit-consumer"

    @KafkaListener(
        topics = ["\${app.kafka.topics.message-events:chat.message.events}"],
        groupId = "\${app.kafka.consumer-groups.audit:ko-chat-audit}",
        containerFactory = "kafkaListenerContainerFactory",
    )
    @Transactional
    fun consume(payload: String) {
        val event = objectMapper.readValue(payload, ChatMessageSentEvent::class.java)
        if (!eventIdempotencyService.markProcessed(event.eventId, consumerName)) {
            logger.debug("중복 audit 이벤트 건너뜀: {}", event.eventId)
            return
        }

        chatAuditLogJpaRepository.save(
            ChatAuditLogJpaEntity().apply {
                eventId = event.eventId
                roomId = event.roomId
                messageId = event.messageId
                eventType = event.eventType.name
                senderId = event.senderId
                messageType = event.messageType
                createdAt = event.createdAt
            },
        )
        logger.debug("감사 로그 저장 완료: messageId={}", event.messageId)
    }
}
