package com.kominioai.domain.auth.application.dto

import jakarta.validation.constraints.Size

data class UserProfileRequest(
    @field:Size(max = 100, message = "이름은 100자 이하여야 합니다.")
    val firstName: String? = null,

    @field:Size(max = 100, message = "성은 100자 이하여야 합니다.")
    val lastName: String? = null,

    @field:Size(max = 500, message = "프로필 이미지 URL은 500자 이하여야 합니다.")
    val profileImageUrl: String? = null,

    @field:Size(max = 20, message = "전화번호는 20자 이하여야 합니다.")
    val phone: String? = null
)