package com.kominioai.domain.reward.domain.model

import java.util.UUID

@JvmInline
value class RewardId(val value: String) {
    companion object {
        fun generate(): RewardId = RewardId(UUID.randomUUID().toString())
    }
}
