package com.kominioai.domain.reward.application.dto

import com.kominioai.domain.reward.domain.model.RewardType
import com.kominioai.domain.reward.domain.model.RewardStatus
import java.time.LocalDateTime

data class UserRewardResponse(
    val id: String,
    val rewardType: RewardType,
    val rewardValue: Int,
    val description: String,
    val claimCode: String,
    val status: RewardStatus,
    val wonAt: LocalDateTime,
    val claimedAt: LocalDateTime?,
    val expiredAt: LocalDateTime?
)
