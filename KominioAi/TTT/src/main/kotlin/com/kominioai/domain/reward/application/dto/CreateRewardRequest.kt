package com.kominioai.domain.reward.application.dto

import com.kominioai.domain.reward.domain.model.RewardType
import jakarta.validation.constraints.*

data class CreateRewardRequest(
    @field:NotNull(message = "리워드 타입은 필수입니다")
    val type: RewardType,
    
    @field:Min(value = 1, message = "리워드 가치는 1 이상이어야 합니다")
    val value: Int,
    
    @field:NotBlank(message = "리워드 설명은 필수입니다")
    val description: String,
    
    val imageUrl: String? = null,
    
    @field:DecimalMin(value = "0.0", message = "확률은 0 이상이어야 합니다")
    @field:DecimalMax(value = "1.0", message = "확률은 1 이하여야 합니다")
    val probability: Double = 1.0
)
