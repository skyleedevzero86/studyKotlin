package com.kominioai.domain.reward.application.dto

import com.kominioai.domain.reward.domain.model.RewardType
import java.time.LocalDateTime

data class CreatorRewardResponse(
    val id: String,
    val surveyId: String,
    val creatorId: String,
    val rewardType: RewardType,
    val rewardValue: Int,
    val description: String,
    val participationRate: Double,
    val targetParticipationRate: Double,
    val isClaimed: Boolean,
    val claimedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
