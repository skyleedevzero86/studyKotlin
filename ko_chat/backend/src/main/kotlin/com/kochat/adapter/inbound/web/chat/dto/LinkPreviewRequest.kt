package com.kochat.adapter.inbound.web.chat.dto

import jakarta.validation.constraints.NotBlank

data class LinkPreviewRequest(
    @field:NotBlank(message = "URL은 필수입니다")
    val url: String,
)
