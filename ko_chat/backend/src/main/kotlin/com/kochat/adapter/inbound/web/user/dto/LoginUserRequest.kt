package com.kochat.adapter.inbound.web.user.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "로그인 요청")
data class LoginUserRequest(
    @field:Schema(description = "아이디", example = "user1", requiredMode = Schema.RequiredMode.REQUIRED)
    @field:NotBlank(message = "아이디는 필수입니다.")
    val username: String,
    @field:Schema(description = "비밀번호", example = "pass1234!", requiredMode = Schema.RequiredMode.REQUIRED)
    @field:NotBlank(message = "비밀번호는 필수입니다.")
    val password: String,
)
