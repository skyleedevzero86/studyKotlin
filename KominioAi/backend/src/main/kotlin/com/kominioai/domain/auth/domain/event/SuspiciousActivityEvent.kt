package com.kominioai.domain.auth.domain.event

import java.time.LocalDateTime

data class SuspiciousActivityEvent(
    val userId: String?,
    val email: String?,
    val activityType: String,
    val description: String,
    val ipAddress: String?,
    val userAgent: String?,
    val locationInfo: String?,
    val detectedAt: LocalDateTime
) {
    companion object {
        fun create(
            userId: String?,
            email: String?,
            activityType: String,
            description: String,
            ipAddress: String?,
            userAgent: String?,
            locationInfo: String?
        ): SuspiciousActivityEvent {
            return SuspiciousActivityEvent(
                userId = userId,
                email = email,
                activityType = activityType,
                description = description,
                ipAddress = ipAddress,
                userAgent = userAgent,
                locationInfo = locationInfo,
                detectedAt = LocalDateTime.now()
            )
        }
    }
}