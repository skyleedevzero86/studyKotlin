package com.kominioai.domain.reward.domain.model

import java.security.SecureRandom
import java.util.*

@JvmInline
value class ClaimCode(val value: String) {
    companion object {
        private const val CODE_LENGTH = 12
        private const val PREFIX_LENGTH = 3
        
        fun generate(rewardType: RewardType): ClaimCode {
            val random = SecureRandom()
            val prefix = when (rewardType) {
                RewardType.GIFTCARD -> "GFT"
                RewardType.POINTS -> "PNT"
                RewardType.COUPON -> "CPN"
                RewardType.CASH -> "CSH"
                RewardType.DIGITAL -> "DGT"
                RewardType.PHYSICAL -> "PHY"
            }
            
            val randomPart = (1..CODE_LENGTH - PREFIX_LENGTH)
                .map { random.nextInt(10) }
                .joinToString("")
            
            return ClaimCode("$prefix$randomPart")
        }
    }
}
