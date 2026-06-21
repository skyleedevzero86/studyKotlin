package com.sleekydz86.oauth.global.application.user

data class UserSensitivePayload(
    val role: String,
    val status: String,
    val createdAt: String,
    val passwordChangedAt: String,
    val passwordChangeFailCount: Int,
    val lastLoginAt: String?,
    val passwordExpired: Boolean,
    val daysUntilPasswordChange: Long,
)
