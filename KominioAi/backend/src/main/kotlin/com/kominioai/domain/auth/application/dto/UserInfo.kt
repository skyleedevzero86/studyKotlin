package com.kominioai.domain.auth.application.dto

import java.time.LocalDateTime

data class UserInfo(
    val id: String,
    val email: String,
    val username: String,
    val firstName: String?,
    val lastName: String?,
    val profileImageUrl: String?,
    val emailVerified: Boolean,
    val twoFactorEnabled: Boolean,
    val lastLoginAt: LocalDateTime?
)