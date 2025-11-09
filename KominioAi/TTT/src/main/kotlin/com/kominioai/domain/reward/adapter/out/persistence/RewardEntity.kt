package com.kominioai.domain.reward.adapter.out.persistence

import com.kominioai.domain.reward.domain.model.Reward
import com.kominioai.domain.reward.domain.model.RewardId
import com.kominioai.domain.reward.domain.model.RewardType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("rewards")
data class RewardEntity(
    @Id
    val id: String,
    val type: String,
    val value: Int,
    val description: String,
    val imageUrl: String?,
    val probability: Double,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    fun toDomain(): Reward {
        return Reward(
            id = RewardId(id),
            type = RewardType.valueOf(type),
            value = value,
            description = description,
            imageUrl = imageUrl,
            probability = probability,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    companion object {
        fun fromDomain(reward: Reward): RewardEntity {
            return RewardEntity(
                id = reward.id.value,
                type = reward.type.name,
                value = reward.value,
                description = reward.description,
                imageUrl = reward.imageUrl,
                probability = reward.probability,
                isActive = reward.isActive,
                createdAt = reward.createdAt,
                updatedAt = reward.updatedAt
            )
        }
    }
}
