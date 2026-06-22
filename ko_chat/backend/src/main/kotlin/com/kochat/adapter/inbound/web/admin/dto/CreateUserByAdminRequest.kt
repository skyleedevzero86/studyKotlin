package com.kochat.adapter.inbound.web.admin.dto

import com.kochat.domain.user.model.UserRole
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "관리자 회원 등록 요청")
data class CreateUserByAdminRequest(
    @field:Schema(description = "아이디", example = "newuser", requiredMode = Schema.RequiredMode.REQUIRED)
    @field:NotBlank(message = "아이디는 필수입니다.")
    @field:Size(min = 4, max = 50, message = "아이디는 4~50자여야 합니다.")
    val username: String,
    @field:Schema(description = "비밀번호", example = "pass1234!", requiredMode = Schema.RequiredMode.REQUIRED)
    @field:NotBlank(message = "비밀번호는 필수입니다.")
    @field:Size(min = 8, max = 100, message = "비밀번호는 8~100자여야 합니다.")
    val password: String,
    @field:Schema(description = "표시 이름", example = "신규회원")
    @field:Size(max = 50, message = "표시 이름은 50자 이하여야 합니다.")
    val displayName: String? = null,
    @field:Schema(description = "권한", example = "USER", defaultValue = "USER")
    val role: UserRole = UserRole.USER,
    @field:Schema(description = "즉시 활성화 여부", example = "true")
    val activateImmediately: Boolean = true,
)
