package com.kominioai.domain.auth.application.dto

import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank(message = "이메일 또는 사용자명은 필수입니다.")
    val emailOrUsername: String,

    @field:NotBlank(message = "비밀번호는 필수입니다.")
    val password: String,

    val rememberMe: Boolean = false,
    val twoFactorCode: String? = null,
    val deviceInfo: String? = null
)