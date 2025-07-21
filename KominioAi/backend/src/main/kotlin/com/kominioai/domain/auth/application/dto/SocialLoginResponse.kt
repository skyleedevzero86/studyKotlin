package com.kominioai.domain.auth.application.dto

import java.time.LocalDateTime

data class SocialLoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Long,
    val user: UserInfo?,
    val isNewUser: Boolean,
    val socialAccountInfo: SocialAccountInfo?
)