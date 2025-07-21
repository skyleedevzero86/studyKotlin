package com.kominioai.domain.auth.application.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ResetPasswordRequest(
    @field:NotBlank(message = "토큰은 필수입니다.")
    val token: String,

    @field:NotBlank(message = "새 비밀번호는 필수입니다.")
    @field:Size(min = 8, max = 100, message = "비밀번호는 8-100자 사이여야 합니다.")
    val newPassword: String,

    @field:NotBlank(message = "비밀번호 확인은 필수입니다.")
    val confirmPassword: String
)