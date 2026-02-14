package com.sleekydz86.komfa.domain.user

data class ChangePasswordDTO(
    val currentPassword: String,
    val newPassword: String,
)
