package com.kominioai.domain.bulletin.application.dto

data class PostListResponse(
    val posts: List<PostResponse>,
    val totalCount: Long,
    val page: Int,
    val size: Int,
    val totalPages: Int
)
