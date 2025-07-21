package com.kominioai.domain.auth.application.dto

data class EnableTwoFactorResponse(
    val secret: String,
    val qrCodeUrl: String,
    val backupCodes: List<String>
)