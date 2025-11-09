package com.kominioai.domain.reward.application.dto

data class RewardListResponse(
    val rewards: List<RewardResponse>,
    val totalCount: Long,
    val page: Int,
    val size: Int,
    val totalPages: Int
)
