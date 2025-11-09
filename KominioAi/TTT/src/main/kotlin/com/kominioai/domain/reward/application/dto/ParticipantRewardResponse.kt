package com.kominioai.domain.reward.application.dto

import com.kominioai.domain.reward.domain.model.RewardType
import com.kominioai.domain.reward.domain.model.RewardStatus
import java.time.LocalDateTime

data class ParticipantRewardResponse(
    val won: Boolean,
    val userRewardId: String?,
    val rewardType: RewardType?,
    val rewardValue: Int?,
    val description: String?,
    val claimCode: String?,
    val status: RewardStatus?,
    val wonAt: LocalDateTime?,
    val expiredAt: LocalDateTime?
)
