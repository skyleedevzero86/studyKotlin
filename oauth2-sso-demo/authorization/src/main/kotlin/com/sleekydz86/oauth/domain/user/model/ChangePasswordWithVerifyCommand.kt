package com.sleekydz86.oauth.domain.user.model

data class ChangePasswordWithVerifyCommand(
    val username: String,
    val currentPassword: String,
    val newPassword: String,
)
