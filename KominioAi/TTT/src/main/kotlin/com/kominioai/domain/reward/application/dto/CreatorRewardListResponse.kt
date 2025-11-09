package com.kominioai.domain.reward.application.dto

data class CreatorRewardListResponse(
    val rewards: List<CreatorRewardResponse>,
    val totalCount: Long,
    val eligibleCount: Long,
    val claimedCount: Long,
    val totalValue: Long
)
