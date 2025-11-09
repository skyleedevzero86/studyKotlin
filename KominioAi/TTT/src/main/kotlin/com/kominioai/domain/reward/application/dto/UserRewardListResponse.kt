package com.kominioai.domain.reward.application.dto

data class UserRewardListResponse(
    val rewards: List<UserRewardResponse>,
    val totalCount: Long,
    val pendingCount: Long,
    val claimedCount: Long,
    val expiredCount: Long
)
