package com.kominioai.domain.reward.application.dto

import com.kominioai.domain.reward.domain.model.RewardType

data class GetRewardsRequest(
    val type: RewardType? = null,
    val isActive: Boolean? = null,
    val page: Int = 0,
    val size: Int = 20
)
