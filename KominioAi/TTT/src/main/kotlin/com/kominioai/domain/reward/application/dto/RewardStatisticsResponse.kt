package com.kominioai.domain.reward.application.dto

import com.kominioai.domain.reward.domain.model.RewardType
import java.time.LocalDateTime

data class RewardStatisticsResponse(
    val totalRewards: Long,
    val totalUserRewards: Long,
    val totalClaimedRewards: Long,
    val totalPendingRewards: Long,
    val totalExpiredRewards: Long,
    val rewardsByType: Map<RewardType, Long>,
    val recentWinners: List<RecentWinnerResponse>,
    val monthlyStats: List<MonthlyRewardStatsResponse>
)

data class RecentWinnerResponse(
    val userId: String,
    val rewardType: RewardType,
    val rewardValue: Int,
    val wonAt: LocalDateTime
)

data class MonthlyRewardStatsResponse(
    val month: String,
    val totalRewards: Long,
    val totalValue: Long,
    val claimedCount: Long
)
