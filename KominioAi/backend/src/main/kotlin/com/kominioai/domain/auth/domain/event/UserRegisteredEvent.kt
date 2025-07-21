package com.kominioai.domain.auth.domain.event

import com.kominioai.domain.auth.domain.model.User
import java.time.LocalDateTime

data class UserRegisteredEvent(
    val userId: String,
    val email: String,
    val username: String,
    val registeredAt: LocalDateTime
) {
    companion object {
        fun from(user: User): UserRegisteredEvent {
            return UserRegisteredEvent(
                userId = user.id.value,
                email = user.email.value,
                username = user.username.value,
                registeredAt = user.createdAt
            )
        }
    }
}