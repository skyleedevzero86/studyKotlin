package com.kochat.domain.messaging.model

import java.time.LocalDateTime

data class AttachmentUploadedEvent(
    val eventId: String,
    val eventType: ChatEventType = ChatEventType.ATTACHMENT_UPLOADED,
    val roomId: Long,
    val messageId: Long,
    val attachmentId: Long,
    val objectKey: String,
    val fileName: String,
    val mimeType: String,
    val size: Long,
    val createdAt: LocalDateTime,
)
