package com.kominioai.domain.reward.domain.model

import java.time.LocalDateTime

data class UserReward(
    val id: UserRewardId,
    val userId: String,
    val rewardId: RewardId,
    val reward: Reward,
    val claimCode: ClaimCode,
    val status: RewardStatus,
    val wonAt: LocalDateTime,
    val claimedAt: LocalDateTime?,
    val expiredAt: LocalDateTime?
) {
    companion object {
        fun create(
            userId: String,
            reward: Reward
        ): UserReward {
            val now = LocalDateTime.now()
            val claimCode = ClaimCode.generate(reward.type)
            val expiredAt = now.plusDays(30) // 30일 후 만료
            
            return UserReward(
                id = UserRewardId.generate(),
                userId = userId,
                rewardId = reward.id,
                reward = reward,
                claimCode = claimCode,
                status = RewardStatus.PENDING,
                wonAt = now,
                claimedAt = null,
                expiredAt = expiredAt
            )
        }
    }
    
    fun claim(): UserReward {
        if (status != RewardStatus.PENDING) {
            throw IllegalStateException("이미 처리된 리워드입니다.")
        }
        
        return copy(
            status = RewardStatus.CLAIMED,
            claimedAt = LocalDateTime.now()
        )
    }
    
    fun expire(): UserReward {
        if (status != RewardStatus.PENDING) {
            throw IllegalStateException("이미 처리된 리워드입니다.")
        }
        
        return copy(
            status = RewardStatus.EXPIRED
        )
    }
    
    fun cancel(): UserReward {
        if (status != RewardStatus.PENDING) {
            throw IllegalStateException("이미 처리된 리워드입니다.")
        }
        
        return copy(
            status = RewardStatus.CANCELLED
        )
    }
    
    fun isExpired(): Boolean {
        return expiredAt?.isBefore(LocalDateTime.now()) ?: false
    }
}
