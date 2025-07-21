package com.kominioai.domain.auth.application.dto

import jakarta.validation.constraints.NotBlank

data class EnableTwoFactorRequest(
    @field:NotBlank(message = "OTP 코드는 필수입니다.")
    val otpCode: String
)