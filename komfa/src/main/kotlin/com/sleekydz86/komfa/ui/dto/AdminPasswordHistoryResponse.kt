package com.sleekydz86.komfa.ui.dto

data class AdminPasswordHistoryResponse(
    val userId: Long,
    val username: String,
    val content: List<PasswordHistoryItem>,
    val totalElements: Long,
    val totalPages: Int,
    val number: Int,
    val size: Int,
)
