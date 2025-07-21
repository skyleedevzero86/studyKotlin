package com.kominioai.domain.auth.application.dto

import java.time.LocalDateTime

data class AdminUserDetailResponse(
    val id: String,
    val email: String,
    val username: String,
    val firstName: String?,
    val lastName: String?,
    val profileImageUrl: String?,
    val phone: String?,
    val accountStatus: String,
    val emailVerified: Boolean,
    val twoFactorEnabled: Boolean,
    val lastLoginAt: LocalDateTime?,
    val failedLoginAttempts: Int,
    val accountLockedUntil: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val roles: List<AdminUserRoleInfo>,
    val socialAccounts: List<AdminSocialAccountInfo>,
    val recentSecurityLogs: List<SecurityLogEntry>
)