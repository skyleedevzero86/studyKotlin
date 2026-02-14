package com.sleekydz86.komfa.ui.dto

data class AdminUserListResponse(
    val content: List<AdminUserListItem>,
    val totalElements: Long,
    val totalPages: Int,
    val number: Int,
    val size: Int,
)
