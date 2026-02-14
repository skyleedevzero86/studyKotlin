package com.sleekydz86.komfa.domain.user

data class ResetPasswordDTO(
    val token: String,
    val newPassword: String,
)
