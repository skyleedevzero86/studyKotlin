package com.kominioai.domain.bulletin.domain.event

import com.kominioai.domain.bulletin.domain.model.CommentId
import java.time.LocalDateTime

data class CommentDeletedEvent(
    val commentId: CommentId,
    val postId: String,
    val authorId: String,
    val deletedAt: LocalDateTime
)
