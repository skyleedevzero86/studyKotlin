package com.komroonga.domain.post.dto

import com.komroonga.domain.post.entity.NoticeType

data class PostRequest(
    val title: String, 
    val content: String, 
    val authorId: Long?,
    val isPrivate: Boolean = false,
    val noticeType: NoticeType = NoticeType.NONE
)
