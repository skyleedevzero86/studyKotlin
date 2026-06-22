package com.sleekydz86.oauth.domain.user.model

data class CreateUserByAdminCommand(
    val username: String,
    val password: String,
    val role: UserRole,
    val displayName: String?,
    val activateImmediately: Boolean,
)
