package com.kominioai.domain.reward.application.dto

import com.kominioai.domain.reward.domain.model.RewardType
import java.time.LocalDateTime

data class ClaimCreatorRewardResponse(
    val success: Boolean,
    val message: String,
    val creatorRewardId: String?,
    val rewardType: RewardType?,
    val rewardValue: Int?,
    val claimedAt: LocalDateTime?
)
