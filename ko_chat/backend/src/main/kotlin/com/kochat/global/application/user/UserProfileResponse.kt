package com.kochat.global.application.user

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "내 프로필 조회 응답")
data class UserProfileResponse(
    @field:Schema(description = "아이디", example = "user1")
    val username: String,
    @field:Schema(description = "표시 이름", example = "홍길동")
    val displayName: String?,
    @field:Schema(description = "권한", example = "USER")
    val role: String,
    @field:Schema(description = "상태", example = "ACTIVE")
    val status: String,
    @field:Schema(description = "가입일 (yyyy-MM-dd HH:mm:ss)", example = "2026-01-01 09:30:00")
    val createdAt: String,
    @field:Schema(description = "비밀번호 변경일", example = "2026-01-15 14:20:00")
    val passwordChangedAt: String,
    @field:Schema(description = "비밀번호 변경 실패 횟수", example = "0")
    val passwordChangeFailCount: Int,
    @field:Schema(description = "로그인 실패 횟수", example = "2")
    val loginFailCount: Int,
    @field:Schema(description = "마지막 로그인", example = "2026-06-22 15:00:00")
    val lastLoginAt: String?,
    @field:Schema(description = "비밀번호 만료 여부")
    val passwordExpired: Boolean,
    @field:Schema(description = "비밀번호 변경까지 남은 일수")
    val daysUntilPasswordChange: Long,
)
