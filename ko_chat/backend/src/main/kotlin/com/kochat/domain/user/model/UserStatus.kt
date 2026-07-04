package com.kochat.domain.user.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "회원 상태")
enum class UserStatus {
    @Schema(description = "승인 대기")
    PENDING,

    @Schema(description = "정상 이용")
    ACTIVE,

    @Schema(description = "탈퇴")
    WITHDRAWN,

    @Schema(description = "이용 정지")
    SUSPENDED,

    @Schema(description = "비밀번호 변경 잠금")
    PASSWORD_LOCKED,

    @Schema(description = "로그인 실패 잠금")
    LOGIN_LOCKED,
}
