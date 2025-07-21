package com.kominioai.domain.auth.application.dto

import jakarta.validation.constraints.NotBlank

data class AdminUserRoleRequest(
    @field:NotBlank(message = "역할명은 필수입니다.")
    val roleName: String,

    val action: String = "GRANT"
)