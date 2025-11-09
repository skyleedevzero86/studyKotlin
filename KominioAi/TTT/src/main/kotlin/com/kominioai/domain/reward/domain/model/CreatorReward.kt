package com.kominioai.domain.reward.domain.model

import java.time.LocalDateTime

data class CreatorReward(
    val id: CreatorRewardId,
    val surveyId: String,
    val creatorId: String,
    val rewardType: RewardType,
    val rewardValue: Int,
    val description: String,
    val participationRate: Double,
    val targetParticipationRate: Double,
    val isClaimed: Boolean,
    val claimedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun create(
            surveyId: String,
            creatorId: String,
            rewardType: RewardType,
            rewardValue: Int,
            description: String,
            participationRate: Double,
            targetParticipationRate: Double
        ): CreatorReward {
            val now = LocalDateTime.now()
            return CreatorReward(
                id = CreatorRewardId.generate(),
                surveyId = surveyId,
                creatorId = creatorId,
                rewardType = rewardType,
                rewardValue = rewardValue,
                description = description,
                participationRate = participationRate,
                targetParticipationRate = targetParticipationRate,
                isClaimed = false,
                claimedAt = null,
                createdAt = now,
                updatedAt = now
            )
        }
    }
    
    fun claim(): CreatorReward {
        if (isClaimed) {
            throw IllegalStateException("이미 수령한 리워드입니다.")
        }
        
        return copy(
            isClaimed = true,
            claimedAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
    
    fun isEligible(): Boolean {
        return participationRate >= targetParticipationRate && !isClaimed
    }
}
