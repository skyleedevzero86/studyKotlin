package com.kochat.domain.user.model

data class JoinCommand(
    val username: String,
    val password: String,
    val displayName: String? = null,
)
