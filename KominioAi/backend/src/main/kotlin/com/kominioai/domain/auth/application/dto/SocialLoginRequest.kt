package com.kominioai.domain.auth.application.dto

import jakarta.validation.constraints.NotBlank

data class SocialLoginRequest(
    @field:NotBlank(message = "프로바이더는 필수입니다.")
    val provider: String,

    @field:NotBlank(message = "액세스 토큰은 필수입니다.")
    val accessToken: String,

    val deviceInfo: String? = null
)