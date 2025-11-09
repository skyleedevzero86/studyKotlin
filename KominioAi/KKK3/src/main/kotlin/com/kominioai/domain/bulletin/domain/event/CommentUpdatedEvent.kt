package com.kominioai.domain.bulletin.domain.event

import com.kominioai.domain.bulletin.domain.model.CommentId
import java.time.LocalDateTime

data class CommentUpdatedEvent(
    val commentId: CommentId,
    val content: String,
    val authorId: String,
    val updatedAt: LocalDateTime
)
