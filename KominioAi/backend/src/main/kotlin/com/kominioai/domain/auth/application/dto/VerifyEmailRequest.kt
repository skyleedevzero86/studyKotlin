package com.kominioai.domain.auth.application.dto

import jakarta.validation.constraints.NotBlank

data class VerifyEmailRequest(
    @field:NotBlank(message = "토큰은 필수입니다.")
    val token: String
)