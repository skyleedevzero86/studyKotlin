package com.kominioai.domain.auth.application.dto

import java.time.LocalDateTime

data class AdminSocialAccountInfo(
    val provider: String,
    val providerUserId: String,
    val email: String?,
    val displayName: String?,
    val connectedAt: LocalDateTime,
    val active: Boolean
)