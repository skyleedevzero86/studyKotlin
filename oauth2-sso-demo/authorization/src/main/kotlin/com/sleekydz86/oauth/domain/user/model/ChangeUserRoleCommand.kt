package com.sleekydz86.oauth.domain.user.model

data class ChangeUserRoleCommand(
    val username: String,
    val role: UserRole,
)
