package com.kominioai.domain.bulletin.domain.event

import com.kominioai.domain.bulletin.domain.model.PostId
import com.kominioai.domain.bulletin.domain.model.PostCategory
import java.time.LocalDateTime

data class PostCreatedEvent(
    val postId: PostId,
    val title: String,
    val category: PostCategory,
    val authorId: String,
    val authorName: String,
    val pinned: Boolean,
    val createdAt: LocalDateTime
)
