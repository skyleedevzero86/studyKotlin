package com.kochat.adapter.inbound.web.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "공통 API 메시지 응답")
data class ApiMessageResponse(
    @field:Schema(description = "처리 결과 메시지", example = "처리가 완료되었습니다.")
    val message: String,
    @field:Schema(description = "회원 상태 (선택)", example = "ACTIVE")
    val status: String? = null,
    @field:Schema(description = "회원 권한 (선택)", example = "USER")
    val role: String? = null,
)
