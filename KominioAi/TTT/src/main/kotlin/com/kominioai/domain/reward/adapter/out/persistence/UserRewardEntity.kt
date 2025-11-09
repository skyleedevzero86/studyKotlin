package com.kominioai.domain.reward.adapter.out.persistence

import com.kominioai.domain.reward.domain.model.UserReward
import com.kominioai.domain.reward.domain.model.UserRewardId
import com.kominioai.domain.reward.domain.model.RewardStatus
import com.kominioai.domain.reward.domain.model.ClaimCode
import com.kominioai.domain.reward.domain.model.RewardId
import com.kominioai.domain.reward.domain.model.RewardType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("user_rewards")
data class UserRewardEntity(
    @Id
    val id: String,
    val userId: String,
    val rewardId: String,
    val rewardType: String,
    val rewardValue: Int,
    val rewardDescription: String,
    val claimCode: String,
    val status: String,
    val wonAt: LocalDateTime,
    val claimedAt: LocalDateTime?,
    val expiredAt: LocalDateTime?
) {
    fun toDomain(): UserReward {
        val reward = com.kominioai.domain.reward.domain.model.Reward(
            id = RewardId(rewardId),
            type = RewardType.valueOf(rewardType),
            value = rewardValue,
            description = rewardDescription,
            imageUrl = null,
            probability = 1.0,
            isActive = true,
            createdAt = wonAt,
            updatedAt = wonAt
        )
        
        return UserReward(
            id = UserRewardId(id),
            userId = userId,
            rewardId = RewardId(rewardId),
            reward = reward,
            claimCode = ClaimCode(claimCode),
            status = RewardStatus.valueOf(status),
            wonAt = wonAt,
            claimedAt = claimedAt,
            expiredAt = expiredAt
        )
    }
    
    companion object {
        fun fromDomain(userReward: UserReward): UserRewardEntity {
            return UserRewardEntity(
                id = userReward.id.value,
                userId = userReward.userId,
                rewardId = userReward.rewardId.value,
                rewardType = userReward.reward.type.name,
                rewardValue = userReward.reward.value,
                rewardDescription = userReward.reward.description,
                claimCode = userReward.claimCode.value,
                status = userReward.status.name,
                wonAt = userReward.wonAt,
                claimedAt = userReward.claimedAt,
                expiredAt = userReward.expiredAt
            )
        }
    }
}
