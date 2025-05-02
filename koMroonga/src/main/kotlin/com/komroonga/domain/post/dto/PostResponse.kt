package com.komroonga.domain.post.dto

import com.komroonga.domain.post.entity.NoticeType

data class PostResponse(
    val id: Long,
    val title: String,
    val content: String,
    val authorUsername: String,
    val authorId: Long,
    val isPrivate: Boolean,
    val noticeType: NoticeType
)
