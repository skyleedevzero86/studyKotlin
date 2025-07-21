package com.kominioai.domain.auth.application.dto

data class SocialAccountInfo(
    val provider: String,
    val providerUserId: String,
    val displayName: String?,
    val profileImageUrl: String?
)