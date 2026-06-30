package com.kochat.adapter.inbound.web.chat.dto

import com.kochat.domain.chat.model.MessageType
import java.time.LocalDateTime

data class AttachmentUploadResponse(
    val messageType: MessageType,
    val metadata: MessageMetadataDto,
    val content: String?,
)
