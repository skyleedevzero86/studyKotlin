package com.kochat.domain.user.model

data class ApproveUserCommand(
    val username: String,
    val role: UserRole,
)
