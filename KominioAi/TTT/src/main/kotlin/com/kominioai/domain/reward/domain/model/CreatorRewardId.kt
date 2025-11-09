package com.kominioai.domain.reward.domain.model

import java.util.UUID

@JvmInline
value class CreatorRewardId(val value: String) {
    companion object {
        fun generate(): CreatorRewardId = CreatorRewardId(UUID.randomUUID().toString())
    }
}
