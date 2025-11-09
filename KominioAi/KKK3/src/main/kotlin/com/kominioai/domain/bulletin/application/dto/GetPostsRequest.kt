package com.kominioai.domain.bulletin.application.dto

import com.kominioai.domain.bulletin.domain.model.PostCategory

data class GetPostsRequest(
    val category: PostCategory? = null,
    val searchTerm: String? = null,
    val authorId: String? = null,
    val pinned: Boolean? = null,
    val page: Int = 0,
    val size: Int = 20,
    val sortBy: String = "createdAt",
    val sortDirection: String = "DESC",
    val userRole: String? = null
)
