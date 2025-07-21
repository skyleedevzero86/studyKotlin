package com.kominioai.domain.auth.application.dto

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Long,
    val user: UserInfo?,
    val requiresTwoFactor: Boolean = false
)