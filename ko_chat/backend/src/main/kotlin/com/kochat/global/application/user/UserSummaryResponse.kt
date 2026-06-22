package com.kochat.global.application.user

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "관리자 사용자 목록 항목 (민감 정보 AES-256 암호화)")
data class UserSummaryResponse(
    @field:Schema(description = "아이디 (평문)", example = "user1")
    val username: String,
    @field:Schema(
        description = "민감 정보 JSON을 AES-256-GCM으로 암호화한 Base64 문자열 (role, status, 날짜 등)",
        example = "Base64EncodedCipherText...",
    )
    val encryptedPayload: String,
)
