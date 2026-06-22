package com.sleekydz86.oauth.adapter.inbound.web.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "회원 가입 응답")
data class JoinResponse(
    @field:Schema(description = "안내 메시지", example = "관리자 승인 후 이용 가능합니다.")
    val message: String,
    @field:Schema(description = "회원 상태", example = "PENDING")
    val status: String,
    @field:Schema(description = "아이디", example = "user1")
    val username: String,
)
