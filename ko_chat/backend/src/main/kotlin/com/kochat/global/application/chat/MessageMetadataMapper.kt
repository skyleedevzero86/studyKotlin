package com.kochat.global.application.chat

import com.fasterxml.jackson.databind.ObjectMapper
import com.kochat.adapter.inbound.web.chat.dto.MessageMetadataDto
import org.springframework.stereotype.Component

@Component
class MessageMetadataMapper(
    private val objectMapper: ObjectMapper,
) {
    fun toJson(metadata: MessageMetadataDto?): String? =
        metadata?.let { objectMapper.writeValueAsString(it) }

    fun fromJson(raw: String?): MessageMetadataDto? {
        if (raw.isNullOrBlank()) {
            return null
        }
        return objectMapper.readValue(raw, MessageMetadataDto::class.java)
    }
}
