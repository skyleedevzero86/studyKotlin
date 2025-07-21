package com.kominioai.domain.auth.application.dto

data class SocialUserInfo(
    val provider: String,
    val providerUserId: String,
    val email: String?,
    val displayName: String?,
    val profileImageUrl: String?,
    val firstName: String?,
    val lastName: String?
)