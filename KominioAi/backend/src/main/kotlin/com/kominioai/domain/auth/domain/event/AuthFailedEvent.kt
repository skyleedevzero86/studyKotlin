package com.kominioai.domain.auth.domain.event

import java.time.LocalDateTime

data class AuthFailedEvent(
    val email: String?,
    val username: String?,
    val failureReason: String,
    val ipAddress: String?,
    val userAgent: String?,
    val failedAt: LocalDateTime
) {
    companion object {
        fun create(
            email: String?,
            username: String?,
            failureReason: String,
            ipAddress: String?,
            userAgent: String?
        ): AuthFailedEvent {
            return AuthFailedEvent(
                email = email,
                username = username,
                failureReason = failureReason,
                ipAddress = ipAddress,
                userAgent = userAgent,
                failedAt = LocalDateTime.now()
            )
        }
    }
}