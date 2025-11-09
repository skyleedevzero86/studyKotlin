package com.kominioai.domain.reward.domain.event

import com.kominioai.domain.reward.domain.model.CreatorRewardId
import com.kominioai.domain.reward.domain.model.RewardType
import java.time.LocalDateTime

data class CreatorRewardEarnedEvent(
    val creatorId: String,
    val surveyId: String,
    val creatorRewardId: CreatorRewardId,
    val rewardType: RewardType,
    val rewardValue: Int,
    val participationRate: Double,
    val earnedAt: LocalDateTime
)
