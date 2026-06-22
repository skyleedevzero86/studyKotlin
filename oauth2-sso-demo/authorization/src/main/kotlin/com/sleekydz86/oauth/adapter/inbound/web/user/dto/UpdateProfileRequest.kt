package com.sleekydz86.oauth.adapter.inbound.web.user.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size

@Schema(description = "프로필 수정 요청")
data class UpdateProfileRequest(
    @field:Schema(description = "표시 이름", example = "홍길동")
    @field:Size(max = 50, message = "표시 이름은 50자 이하여야 합니다.")
    val displayName: String?,
)
