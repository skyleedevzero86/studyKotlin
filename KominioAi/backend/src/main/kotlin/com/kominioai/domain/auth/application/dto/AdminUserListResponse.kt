package com.kominioai.domain.auth.application.dto

import java.time.LocalDateTime

data class AdminUserListResponse(
    val users: List<AdminUserListItem>,
    val totalCount: Long,
    val page: Int,
    val size: Int,
    val totalPages: Int
)