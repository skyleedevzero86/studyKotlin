package com.kominioai.domain.auth.application.dto

import jakarta.validation.constraints.NotBlank

data class AdminUserStatusRequest(
    @field:NotBlank(message = "계정 상태는 필수입니다.")
    val status: String,

    val reason: String? = null
)