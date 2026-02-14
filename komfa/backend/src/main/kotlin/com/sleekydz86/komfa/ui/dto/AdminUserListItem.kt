package com.sleekydz86.komfa.ui.dto

data class AdminUserListItem(
    val id: Long,
    val username: String,
    val emailMasked: String?,
    val roles: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
)
