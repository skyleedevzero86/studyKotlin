package com.kominioai.domain.bulletin.domain.event

import com.kominioai.domain.bulletin.domain.model.PostId
import com.kominioai.domain.bulletin.domain.model.CommentId
import java.time.LocalDateTime

data class CommentCreatedEvent(
    val commentId: CommentId,
    val postId: PostId,
    val parentId: CommentId?,
    val content: String,
    val authorId: String,
    val authorName: String,
    val createdAt: LocalDateTime
)
