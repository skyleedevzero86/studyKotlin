package com.kominioai.domain.auth.domain.model

import java.time.LocalDateTime

data class UserSecurityLog(
    val id: String,
    val userId: String,
    val eventType: String,
    val eventDescription: String,
    val ipAddress: String?,
    val userAgent: String?,
    val success: Boolean,
    val failureReason: String?,
    val createdAt: LocalDateTime
)