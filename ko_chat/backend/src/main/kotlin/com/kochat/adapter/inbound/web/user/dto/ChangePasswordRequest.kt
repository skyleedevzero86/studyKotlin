package com.kochat.adapter.inbound.web.user.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "비밀번호 변경 요청")
data class ChangePasswordRequest(
    @field:Schema(description = "아이디", example = "user1", requiredMode = Schema.RequiredMode.REQUIRED)
    @field:NotBlank(message = "아이디는 필수입니다.")
    val username: String,
    @field:Schema(description = "현재 비밀번호", example = "pass1234!", requiredMode = Schema.RequiredMode.REQUIRED)
    @field:NotBlank(message = "현재 비밀번호는 필수입니다.")
    val currentPassword: String,
    @field:Schema(description = "새 비밀번호", example = "newpass5678!", requiredMode = Schema.RequiredMode.REQUIRED)
    @field:NotBlank(message = "새 비밀번호는 필수입니다.")
    @field:Size(min = 8, max = 100, message = "새 비밀번호는 8~100자여야 합니다.")
    val newPassword: String,
)
