package com.kominioai.domain.reward.domain.event

import com.kominioai.domain.reward.domain.model.RewardId
import com.kominioai.domain.reward.domain.model.RewardType
import com.kominioai.domain.reward.domain.model.ClaimCode
import java.time.LocalDateTime

data class RewardWonEvent(
    val userId: String,
    val rewardId: RewardId,
    val rewardType: RewardType,
    val rewardValue: Int,
    val claimCode: ClaimCode,
    val wonAt: LocalDateTime
)
