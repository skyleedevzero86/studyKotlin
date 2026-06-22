package com.kochat.domain.user.model

data class ChangeUserRoleCommand(
    val username: String,
    val role: UserRole,
)
