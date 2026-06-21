package com.sleekydz86.oauth.domain.user.model

data class ApproveUserCommand(
    val username: String,
    val role: UserRole,
)
