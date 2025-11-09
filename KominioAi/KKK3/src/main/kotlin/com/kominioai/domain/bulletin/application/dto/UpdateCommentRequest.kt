package com.kominioai.domain.bulletin.application.dto

import jakarta.validation.constraints.*

data class UpdateCommentRequest(
    @field:NotBlank(message = "댓글 내용은 필수입니다")
    @field:Size(max = 1000, message = "댓글은 1000자를 초과할 수 없습니다")
    val content: String
)
