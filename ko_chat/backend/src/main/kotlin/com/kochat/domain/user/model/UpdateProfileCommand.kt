package com.kochat.domain.user.model

data class UpdateProfileCommand(
    val username: String,
    val displayName: String?,
)
