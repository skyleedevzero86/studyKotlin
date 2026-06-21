package com.sleekydz86.oauth.adapter.inbound.web.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "로그인 성공 응답")
data class LoginResponse(
    @field:Schema(description = "JWT 액세스 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    val accessToken: String,
)
