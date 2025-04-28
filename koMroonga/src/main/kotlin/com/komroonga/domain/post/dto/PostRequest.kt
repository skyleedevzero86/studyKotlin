package com.komroonga.domain.post.dto

data class PostRequest(
    val title: String,
    val content: String,
    val authorId: Long
)