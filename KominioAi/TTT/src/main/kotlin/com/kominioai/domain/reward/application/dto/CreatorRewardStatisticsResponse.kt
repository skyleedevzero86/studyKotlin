package com.kominioai.domain.reward.application.dto

import com.kominioai.domain.reward.domain.model.RewardType
import java.time.LocalDateTime

data class CreatorRewardStatisticsResponse(
    val totalRewards: Long,
    val totalValue: Long,
    val eligibleRewards: Long,
    val claimedRewards: Long,
    val averageParticipationRate: Double,
    val rewardsByType: Map<RewardType, Long>,
    val monthlyEarnings: List<MonthlyEarningsResponse>
)

data class MonthlyEarningsResponse(
    val month: String,
    val totalEarnings: Long,
    val rewardCount: Long,
    val averageParticipationRate: Double
)
