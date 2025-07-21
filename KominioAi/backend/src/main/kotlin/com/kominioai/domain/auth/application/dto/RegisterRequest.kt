package com.kominioai.domain.auth.application.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank(message = "이메일은 필수입니다.")
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    val email: String,

    @field:NotBlank(message = "사용자명은 필수입니다.")
    @field:Size(min = 3, max = 100, message = "사용자명은 3-100자 사이여야 합니다.")
    @field:Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "사용자명은 영문, 숫자, 언더스코어, 하이픈만 사용 가능합니다.")
    val username: String,

    @field:NotBlank(message = "비밀번호는 필수입니다.")
    @field:Size(min = 8, max = 100, message = "비밀번호는 8-100자 사이여야 합니다.")
    val password: String,

    @field:NotBlank(message = "비밀번호 확인은 필수입니다.")
    val confirmPassword: String,

    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
    val agreeToTerms: Boolean = false,
    val marketingConsent: Boolean = false
)