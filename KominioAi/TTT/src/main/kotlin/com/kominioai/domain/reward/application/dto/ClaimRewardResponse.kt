package com.kominioai.domain.reward.application.dto

import com.kominioai.domain.reward.domain.model.RewardType
import com.kominioai.domain.reward.domain.model.RewardStatus
import java.time.LocalDateTime

data class ClaimRewardResponse(
    val success: Boolean,
    val message: String,
    val userRewardId: String?,
    val rewardType: RewardType?,
    val rewardValue: Int?,
    val status: RewardStatus?,
    val claimedAt: LocalDateTime?
)
