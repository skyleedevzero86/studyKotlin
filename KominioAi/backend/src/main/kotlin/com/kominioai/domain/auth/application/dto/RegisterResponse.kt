package com.kominioai.domain.auth.application.dto

import java.time.LocalDateTime

data class RegisterResponse(
    val userId: String,
    val email: String,
    val username: String,
    val message: String,
    val requiresEmailVerification: Boolean,
    val registeredAt: LocalDateTime
)