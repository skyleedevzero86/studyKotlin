package com.kochat.domain.user.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "회원 권한")
enum class UserRole {
    @Schema(description = "관리자")
    ADMIN,

    @Schema(description = "일반 사용자")
    USER,
}
