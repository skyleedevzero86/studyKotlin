package com.komroonga.domain.post.dto

import NoticeType

data class PostRequest(
    val title: String, 
    val content: String, 
    val authorId: Long?,
    val isPrivate: Boolean = false,
    val noticeType: NoticeType = NoticeType.NONE
)
