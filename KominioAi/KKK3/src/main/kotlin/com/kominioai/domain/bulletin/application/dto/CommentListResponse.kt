package com.kominioai.domain.bulletin.application.dto

data class CommentListResponse(
    val comments: List<CommentResponse>,
    val totalCount: Long
)
