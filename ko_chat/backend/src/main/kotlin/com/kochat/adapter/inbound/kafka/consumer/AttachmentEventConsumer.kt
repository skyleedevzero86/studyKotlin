package com.kochat.adapter.inbound.kafka.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.kochat.adapter.outbound.persistence.chat.MessageAttachmentJpaRepository
import com.kochat.adapter.outbound.persistence.messaging.AttachmentProcessLogJpaEntity
import com.kochat.adapter.outbound.persistence.messaging.AttachmentProcessLogJpaRepository
import com.kochat.domain.messaging.model.AttachmentUploadedEvent
import com.kochat.global.application.messaging.EventIdempotencyService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
@ConditionalOnProperty(prefix = "app.kafka", name = ["enabled"], havingValue = "true")
class AttachmentEventConsumer(
    private val objectMapper: ObjectMapper,
    private val eventIdempotencyService: EventIdempotencyService,
    private val messageAttachmentJpaRepository: MessageAttachmentJpaRepository,
    private val attachmentProcessLogJpaRepository: AttachmentProcessLogJpaRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val consumerName = "attachment-consumer"

    @KafkaListener(
        topics = ["\${app.kafka.topics.attachment-events:chat.attachment.events}"],
        groupId = "\${app.kafka.consumer-groups.attachment:ko-chat-attachment}",
        containerFactory = "kafkaListenerContainerFactory",
    )
    @Transactional
    fun consume(payload: String) {
        val event = objectMapper.readValue(payload, AttachmentUploadedEvent::class.java)
        if (!eventIdempotencyService.markProcessed(event.eventId, consumerName)) {
            logger.debug("중복 attachment 이벤트 건너뜀: {}", event.eventId)
            return
        }

        val attachment = messageAttachmentJpaRepository.findById(event.attachmentId).orElse(null)
            ?: throw IllegalStateException("첨부파일을 찾을 수 없습니다: ${event.attachmentId}")

        attachmentProcessLogJpaRepository.save(
            AttachmentProcessLogJpaEntity().apply {
                eventId = event.eventId
                attachmentId = event.attachmentId
                roomId = event.roomId
                messageId = event.messageId
                objectKey = attachment.objectKey
                processedAt = LocalDateTime.now()
            },
        )
        logger.info(
            "첨부파일 후처리 완료: attachmentId={}, objectKey={}",
            event.attachmentId,
            attachment.objectKey,
        )
    }
}
