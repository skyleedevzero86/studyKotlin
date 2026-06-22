package com.kochat.domain.user.model

data class ChangePasswordWithVerifyCommand(
    val username: String,
    val currentPassword: String,
    val newPassword: String,
)
