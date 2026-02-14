package com.sleekydz86.komfa.ui.dto

data class MeResponse(
    val username: String,
    val email: String?,
    val createdAt: String,
    val updatedAt: String,
    val requirePasswordChange: Boolean,
)
