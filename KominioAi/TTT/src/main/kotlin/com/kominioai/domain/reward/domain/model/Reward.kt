package com.kominioai.domain.reward.domain.model

import java.time.LocalDateTime

data class Reward(
    val id: RewardId,
    val type: RewardType,
    val value: Int,
    val description: String,
    val imageUrl: String?,
    val probability: Double,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun create(
            type: RewardType,
            value: Int,
            description: String,
            imageUrl: String? = null,
            probability: Double = 1.0
        ): Reward {
            val now = LocalDateTime.now()
            return Reward(
                id = RewardId.generate(),
                type = type,
                value = value,
                description = description,
                imageUrl = imageUrl,
                probability = probability,
                isActive = true,
                createdAt = now,
                updatedAt = now
            )
        }
    }
    
    fun update(
        type: RewardType? = null,
        value: Int? = null,
        description: String? = null,
        imageUrl: String? = null,
        probability: Double? = null,
        isActive: Boolean? = null
    ): Reward {
        return copy(
            type = type ?: this.type,
            value = value ?: this.value,
            description = description ?: this.description,
            imageUrl = imageUrl ?: this.imageUrl,
            probability = probability ?: this.probability,
            isActive = isActive ?: this.isActive,
            updatedAt = LocalDateTime.now()
        )
    }
}
