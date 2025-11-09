package com.kominioai.domain.reward.domain.model

import java.util.UUID

@JvmInline
value class RewardPoolId(val value: String) {
    companion object {
        fun generate(): RewardPoolId = RewardPoolId(UUID.randomUUID().toString())
    }
}
