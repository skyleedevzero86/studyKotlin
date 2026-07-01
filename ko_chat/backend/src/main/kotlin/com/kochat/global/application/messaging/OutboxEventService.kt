package com.kochat.global.application.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.kochat.adapter.outbound.persistence.chat.MessageAttachmentJpaEntity
import com.kochat.adapter.outbound.persistence.chat.MessageJpaEntity
import com.kochat.adapter.outbound.persistence.outbox.OutboxEventJpaEntity
import com.kochat.adapter.outbound.persistence.outbox.OutboxEventJpaRepository
import com.kochat.domain.messaging.model.AttachmentUploadedEvent
import com.kochat.domain.messaging.model.ChatEventType
import com.kochat.domain.messaging.model.ChatMessageSentEvent
import com.kochat.domain.messaging.model.OutboxEventStatus
import com.kochat.global.config.KafkaProperties
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class OutboxEventService(
    private val outboxEventJpaRepository: OutboxEventJpaRepository,
    private val objectMapper: ObjectMapper,
    private val kafkaProperties: KafkaProperties,
) {
    fun enqueueChatMessageSent(
        message: MessageJpaEntity,
        roomId: Long,
        senderId: Long,
    ) {
        if (!kafkaProperties.enabled) {
            return
        }

        val messageId = message.id ?: return
        val event = ChatMessageSentEvent(
            eventId = newEventId(),
            roomId = roomId,
            messageId = messageId,
            sequenceNumber = message.sequenceNumber,
            senderId = senderId,
            messageType = message.type.name,
            createdAt = message.createdAt,
        )

        saveOutbox(
            event = event,
            topic = kafkaProperties.topics.messageEvents,
            partitionKey = roomId.toString(),
            eventType = ChatEventType.CHAT_MESSAGE_SENT.name,
        )
    }

    fun enqueueAttachmentUploaded(attachment: MessageAttachmentJpaEntity) {
        if (!kafkaProperties.enabled) {
            return
        }

        val attachmentId = attachment.id ?: return
        val messageId = attachment.messageId ?: return
        val event = AttachmentUploadedEvent(
            eventId = newEventId(),
            roomId = attachment.chatRoomId,
            messageId = messageId,
            attachmentId = attachmentId,
            objectKey = attachment.objectKey,
            fileName = attachment.fileName,
            mimeType = attachment.mimeType,
            size = attachment.size,
            createdAt = attachment.createdAt,
        )

        saveOutbox(
            event = event,
            topic = kafkaProperties.topics.attachmentEvents,
            partitionKey = attachment.chatRoomId.toString(),
            eventType = ChatEventType.ATTACHMENT_UPLOADED.name,
        )
    }

    private fun saveOutbox(
        event: Any,
        topic: String,
        partitionKey: String,
        eventType: String,
    ) {
        val eventId = when (event) {
            is ChatMessageSentEvent -> event.eventId
            is AttachmentUploadedEvent -> event.eventId
            else -> newEventId()
        }

        outboxEventJpaRepository.save(
            OutboxEventJpaEntity().apply {
                this.eventId = eventId
                this.topic = topic
                this.partitionKey = partitionKey
                this.eventType = eventType
                this.payload = objectMapper.writeValueAsString(event)
                this.status = OutboxEventStatus.PENDING
            },
        )
    }

    private fun newEventId(): String = "evt-${UUID.randomUUID()}"
}
