package com.kominioai.domain.bulletin.domain.event

import com.kominioai.domain.bulletin.domain.model.PostId
import java.time.LocalDateTime

data class PostDeletedEvent(
    val postId: PostId,
    val title: String,
    val authorId: String,
    val deletedAt: LocalDateTime
)
