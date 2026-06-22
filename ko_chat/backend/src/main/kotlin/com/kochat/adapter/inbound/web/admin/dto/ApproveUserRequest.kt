package com.kochat.adapter.inbound.web.admin.dto

import com.kochat.domain.user.model.UserRole
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "회원 승인 요청")
data class ApproveUserRequest(
    @field:Schema(description = "부여할 권한", example = "USER", defaultValue = "USER")
    val role: UserRole = UserRole.USER,
)
