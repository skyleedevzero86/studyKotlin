package com.kominioai.domain.auth.application.dto

import java.time.LocalDateTime

data class AdminUserRoleInfo(
    val roleName: String,
    val grantedAt: LocalDateTime,
    val grantedBy: String?,
    val active: Boolean
)