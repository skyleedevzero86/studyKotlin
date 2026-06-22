package com.sleekydz86.oauth.global.application.user

data class UserSensitivePayload(
    val displayName: String?,
    val role: String,
    val status: String,
    val createdAt: String,
    val passwordChangedAt: String,
    val passwordChangeFailCount: Int,
    val loginFailCount: Int,
    val lastLoginAt: String?,
    val passwordExpired: Boolean,
    val daysUntilPasswordChange: Long,
)
