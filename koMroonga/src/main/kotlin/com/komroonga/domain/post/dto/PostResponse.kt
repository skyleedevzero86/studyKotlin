package com.komroonga.domain.post.dto

data class PostResponse(
    val id: Long,
    val title: String,
    val content: String,
    val authorUsername: String
)