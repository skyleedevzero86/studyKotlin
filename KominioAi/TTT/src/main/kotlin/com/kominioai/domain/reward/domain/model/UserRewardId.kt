package com.kominioai.domain.reward.domain.model

import java.util.UUID

@JvmInline
value class UserRewardId(val value: String) {
    companion object {
        fun generate(): UserRewardId = UserRewardId(UUID.randomUUID().toString())
    }
}
