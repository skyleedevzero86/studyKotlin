package com.kominioai.domain.reward.application.dto

import jakarta.validation.constraints.NotBlank

data class ClaimCreatorRewardRequest(
    @field:NotBlank(message = "생성자 리워드 ID는 필수입니다")
    val creatorRewardId: String
)
