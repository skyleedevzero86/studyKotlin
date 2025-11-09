package com.kominioai.domain.reward.domain.event

import com.kominioai.domain.reward.domain.model.RewardId
import com.kominioai.domain.reward.domain.model.RewardType
import java.time.LocalDateTime

data class RewardClaimedEvent(
    val userId: String,
    val rewardId: RewardId,
    val rewardType: RewardType,
    val rewardValue: Int,
    val claimedAt: LocalDateTime
)
