package com.kominioai.domain.auth.application.dto

import java.time.LocalDateTime

data class AdminUserListItem(
    val id: String,
    val email: String,
    val username: String,
    val firstName: String?,
    val lastName: String?,
    val accountStatus: String,
    val emailVerified: Boolean,
    val twoFactorEnabled: Boolean,
    val lastLoginAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val roles: List<String>
)