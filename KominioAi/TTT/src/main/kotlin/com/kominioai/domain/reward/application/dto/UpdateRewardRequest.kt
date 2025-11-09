package com.kominioai.domain.reward.application.dto

import com.kominioai.domain.reward.domain.model.RewardType
import jakarta.validation.constraints.*

data class UpdateRewardRequest(
    val type: RewardType? = null,
    
    @field:Min(value = 1, message = "리워드 가치는 1 이상이어야 합니다")
    val value: Int? = null,
    
    val description: String? = null,
    val imageUrl: String? = null,
    
    @field:DecimalMin(value = "0.0", message = "확률은 0 이상이어야 합니다")
    @field:DecimalMax(value = "1.0", message = "확률은 1 이하여야 합니다")
    val probability: Double? = null,
    
    val isActive: Boolean? = null
)
