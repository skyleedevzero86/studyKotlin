package com.kominioai.domain.auth.application.dto

data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Long
)