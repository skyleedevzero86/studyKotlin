package com.kochat.adapter.inbound.web.chat.dto

import java.time.LocalDateTime

data class MessageMetadataDto(
    val objectKey: String? = null,
    val url: String? = null,
    val fileName: String? = null,
    val mimeType: String? = null,
    val size: Long? = null,
    val expiresAt: LocalDateTime? = null,
    val linkUrl: String? = null,
    val title: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val siteName: String? = null,
    val domain: String? = null,
)
