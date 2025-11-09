package com.kominioai.domain.bulletin.application.dto

import java.time.LocalDateTime

data class CommentResponse(
    val id: String,
    val postId: String,
    val parentId: String?,
    val content: String,
    val authorId: String,
    val authorName: String,
    val likeCount: Int,
    val replyCount: Int,
    val depth: Int,
    val isReply: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
