package com.kochat.adapter.inbound.kafka.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.kochat.adapter.outbound.persistence.chat.MessageJpaRepository
import com.kochat.adapter.outbound.persistence.messaging.MessageSearchIndexJpaEntity
import com.kochat.adapter.outbound.persistence.messaging.MessageSearchIndexJpaRepository
import com.kochat.domain.messaging.model.ChatMessageSentEvent
import com.kochat.global.application.messaging.EventIdempotencyService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
@ConditionalOnProperty(prefix = "app.kafka", name = ["enabled"], havingValue = "true")
class SearchIndexEventConsumer(
    private val objectMapper: ObjectMapper,
    private val eventIdempotencyService: EventIdempotencyService,
    private val messageJpaRepository: MessageJpaRepository,
    private val messageSearchIndexJpaRepository: MessageSearchIndexJpaRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val consumerName = "search-index-consumer"

    @KafkaListener(
        topics = ["\${app.kafka.topics.message-events:chat.message.events}"],
        groupId = "\${app.kafka.consumer-groups.search-index:ko-chat-search-index}",
        containerFactory = "kafkaListenerContainerFactory",
    )
    @Transactional
    fun consume(payload: String) {
        val event = objectMapper.readValue(payload, ChatMessageSentEvent::class.java)
        if (!eventIdempotencyService.markProcessed(event.eventId, consumerName)) {
            logger.debug("중복 search-index 이벤트 건너뜀: {}", event.eventId)
            return
        }

        val message = messageJpaRepository.findById(event.messageId).orElse(null)
        val preview = message?.content?.take(500) ?: ""

        messageSearchIndexJpaRepository.save(
            MessageSearchIndexJpaEntity().apply {
                messageId = event.messageId
                roomId = event.roomId
                senderId = event.senderId
                messageType = event.messageType
                contentPreview = preview
                indexedAt = LocalDateTime.now()
            },
        )
        logger.debug("검색 인덱스 저장 완료: messageId={}", event.messageId)
    }
}
