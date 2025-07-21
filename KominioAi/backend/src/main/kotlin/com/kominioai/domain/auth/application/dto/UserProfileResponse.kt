package com.kominioai.domain.auth.application.dto

import java.time.LocalDateTime

data class UserProfileResponse(
    val id: String,
    val email: String,
    val username: String,
    val firstName: String?,
    val lastName: String?,
    val profileImageUrl: String?,
    val phone: String?,
    val emailVerified: Boolean,
    val twoFactorEnabled: Boolean,
    val accountStatus: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val lastLoginAt: LocalDateTime?
)