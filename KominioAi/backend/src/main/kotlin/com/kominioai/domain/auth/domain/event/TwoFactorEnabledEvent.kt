package com.kominioai.domain.auth.domain.event

import com.kominioai.domain.auth.domain.model.User
import java.time.LocalDateTime

data class TwoFactorEnabledEvent(
    val userId: String,
    val email: String,
    val enabledAt: LocalDateTime,
    val ipAddress: String?
) {
    companion object {
        fun from(user: User, ipAddress: String?): TwoFactorEnabledEvent {
            return TwoFactorEnabledEvent(
                userId = user.id.value,
                email = user.email.value,
                enabledAt = LocalDateTime.now(),
                ipAddress = ipAddress
            )
        }
    }
}