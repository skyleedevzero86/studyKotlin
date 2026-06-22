package com.sleekydz86.oauth.domain.user.model

data class UpdateProfileCommand(
    val username: String,
    val displayName: String?,
)
