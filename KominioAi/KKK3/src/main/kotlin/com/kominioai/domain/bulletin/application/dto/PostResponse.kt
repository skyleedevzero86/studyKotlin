package com.kominioai.domain.bulletin.application.dto

import com.kominioai.domain.bulletin.domain.model.PostCategory
import java.time.LocalDateTime

data class PostResponse(
    val id: String,
    val title: String,
    val content: String,
    val category: PostCategory,
    val authorId: String,
    val authorName: String,
    val pinned: Boolean,
    val viewCount: Int,
    val likeCount: Int,
    val commentCount: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
