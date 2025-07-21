package com.kominioai.domain.auth.application.dto

import java.time.LocalDateTime

data class UserSessionResponse(
    val sessionId: String,
    val deviceInfo: String?,
    val ipAddress: String?,
    val userAgent: String?,
    val issuedAt: LocalDateTime,
    val expiresAt: LocalDateTime,
    val isCurrentSession: Boolean
)