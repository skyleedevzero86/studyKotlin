package com.kominioai.domain.auth.domain.event

import com.kominioai.domain.auth.domain.model.User
import java.time.LocalDateTime

data class UserLoggedInEvent(
    val userId: String,
    val email: String,
    val username: String,
    val loginAt: LocalDateTime,
    val ipAddress: String?,
    val userAgent: String?,
    val deviceInfo: String?
) {
    companion object {
        fun from(
            user: User,
            ipAddress: String?,
            userAgent: String?,
            deviceInfo: String?
        ): UserLoggedInEvent {
            return UserLoggedInEvent(
                userId = user.id.value,
                email = user.email.value,
                username = user.username.value,
                loginAt = LocalDateTime.now(),
                ipAddress = ipAddress,
                userAgent = userAgent,
                deviceInfo = deviceInfo
            )
        }
    }
}