package com.kominioai.domain.auth.application.dto

import java.time.LocalDateTime

data class SecurityLogEntry(
    val id: String,
    val eventType: String,
    val eventDescription: String,
    val ipAddress: String?,
    val userAgent: String?,
    val success: Boolean,
    val failureReason: String?,
    val createdAt: LocalDateTime
)