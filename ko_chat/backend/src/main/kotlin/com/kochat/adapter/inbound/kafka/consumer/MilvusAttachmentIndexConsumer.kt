package com.kochat.adapter.inbound.kafka.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.kochat.adapter.outbound.persistence.chat.MessageAttachmentJpaRepository
import com.kochat.adapter.outbound.storage.MilvusAttachmentIndexService
import com.kochat.domain.messaging.model.AttachmentUploadedEvent
import com.kochat.global.application.messaging.EventIdempotencyService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@ConditionalOnProperty(prefix = "app.kafka", name = ["enabled"], havingValue = "true")
class MilvusAttachmentIndexConsumer(
    private val objectMapper: ObjectMapper,
    private val eventIdempotencyService: EventIdempotencyService,
    private val messageAttachmentJpaRepository: MessageAttachmentJpaRepository,
    @Autowired(required = false) private val milvusAttachmentIndexService: MilvusAttachmentIndexService? = null,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val consumerName = "milvus-index-consumer"

    @KafkaListener(
        topics = ["\${app.kafka.topics.attachment-events:chat.attachment.events}"],
        groupId = "\${app.kafka.consumer-groups.milvus:ko-chat-milvus}",
        containerFactory = "kafkaListenerContainerFactory",
    )
    @Transactional
    fun consume(payload: String) {
        val event = objectMapper.readValue(payload, AttachmentUploadedEvent::class.java)
        if (!eventIdempotencyService.markProcessed(event.eventId, consumerName)) {
            logger.debug("중복 milvus 이벤트 건너뜀: {}", event.eventId)
            return
        }

        val attachment = messageAttachmentJpaRepository.findById(event.attachmentId).orElse(null)
            ?: throw IllegalStateException("첨부파일을 찾을 수 없습니다: ${event.attachmentId}")

        val milvusService = milvusAttachmentIndexService
        if (milvusService == null) {
            logger.warn("Milvus 서비스를 사용할 수 없어 인덱싱을 건너뜁니다. attachmentId={}", event.attachmentId)
            return
        }

        milvusService.indexAttachment(attachment)
        logger.info("Milvus 인덱싱 완료: attachmentId={}", event.attachmentId)
    }
}
