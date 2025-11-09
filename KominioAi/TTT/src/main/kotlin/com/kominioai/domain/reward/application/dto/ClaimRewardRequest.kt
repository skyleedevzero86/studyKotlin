package com.kominioai.domain.reward.application.dto

import jakarta.validation.constraints.NotBlank

data class ClaimRewardRequest(
    @field:NotBlank(message = "클레임 코드는 필수입니다")
    val claimCode: String
)
