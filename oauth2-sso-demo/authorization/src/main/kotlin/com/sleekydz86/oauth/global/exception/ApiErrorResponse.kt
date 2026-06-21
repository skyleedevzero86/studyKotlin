package com.sleekydz86.oauth.global.exception

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "API 오류 응답")
data class ApiErrorResponse(
    @field:Schema(description = "오류 메시지", example = "아이디 또는 비밀번호가 올바르지 않습니다.")
    val error: String,
    @field:Schema(description = "오류 코드", example = "AUTHENTICATION_FAILED")
    val code: String? = null,
)
