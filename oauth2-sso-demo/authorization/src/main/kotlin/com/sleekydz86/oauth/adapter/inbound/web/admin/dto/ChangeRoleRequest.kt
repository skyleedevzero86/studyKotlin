package com.sleekydz86.oauth.adapter.inbound.web.admin.dto

import com.sleekydz86.oauth.domain.user.model.UserRole
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

@Schema(description = "회원 권한 변경 요청")
data class ChangeRoleRequest(
    @field:Schema(description = "변경할 권한", example = "ADMIN", requiredMode = Schema.RequiredMode.REQUIRED)
    @field:NotNull(message = "권한은 필수입니다.")
    val role: UserRole,
)
