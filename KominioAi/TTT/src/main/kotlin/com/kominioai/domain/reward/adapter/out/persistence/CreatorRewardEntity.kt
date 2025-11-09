package com.kominioai.domain.reward.adapter.out.persistence

import com.kominioai.domain.reward.domain.model.CreatorReward
import com.kominioai.domain.reward.domain.model.CreatorRewardId
import com.kominioai.domain.reward.domain.model.RewardType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("creator_rewards")
data class CreatorRewardEntity(
    @Id
    val id: String,
    val surveyId: String,
    val creatorId: String,
    val rewardType: String,
    val rewardValue: Int,
    val description: String,
    val participationRate: Double,
    val targetParticipationRate: Double,
    val isClaimed: Boolean,
    val claimedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    fun toDomain(): CreatorReward {
        return CreatorReward(
            id = CreatorRewardId(id),
            surveyId = surveyId,
            creatorId = creatorId,
            rewardType = RewardType.valueOf(rewardType),
            rewardValue = rewardValue,
            description = description,
            participationRate = participationRate,
            targetParticipationRate = targetParticipationRate,
            isClaimed = isClaimed,
            claimedAt = claimedAt,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    companion object {
        fun fromDomain(creatorReward: CreatorReward): CreatorRewardEntity {
            return CreatorRewardEntity(
                id = creatorReward.id.value,
                surveyId = creatorReward.surveyId,
                creatorId = creatorReward.creatorId,
                rewardType = creatorReward.rewardType.name,
                rewardValue = creatorReward.rewardValue,
                description = creatorReward.description,
                participationRate = creatorReward.participationRate,
                targetParticipationRate = creatorReward.targetParticipationRate,
                isClaimed = creatorReward.isClaimed,
                claimedAt = creatorReward.claimedAt,
                createdAt = creatorReward.createdAt,
                updatedAt = creatorReward.updatedAt
            )
        }
    }
}
