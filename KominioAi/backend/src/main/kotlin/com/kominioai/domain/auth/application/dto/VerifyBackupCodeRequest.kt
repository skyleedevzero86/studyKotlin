package com.kominioai.domain.auth.application.dto

import jakarta.validation.constraints.NotBlank

data class VerifyBackupCodeRequest(
    @field:NotBlank(message = "백업 코드는 필수입니다.")
    val backupCode: String
)