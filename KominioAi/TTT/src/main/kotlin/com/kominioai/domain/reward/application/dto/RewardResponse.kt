package com.kominioai.domain.reward.application.dto

import com.kominioai.domain.reward.domain.model.RewardType
import java.time.LocalDateTime

data class RewardResponse(
    val id: String,
    val type: RewardType,
    val value: Int,
    val description: String,
    val imageUrl: String?,
    val probability: Double,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
