package com.kominioai.domain.reward.domain.model

import java.time.LocalDateTime

data class RewardPool(
    val id: RewardPoolId,
    val name: String,
    val description: String,
    val isActive: Boolean,
    val totalBudget: Int,
    val usedBudget: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    val remainingBudget: Int
        get() = totalBudget - usedBudget
    
    val usageRate: Double
        get() = if (totalBudget > 0) usedBudget.toDouble() / totalBudget else 0.0
    
    companion object {
        fun create(
            name: String,
            description: String,
            totalBudget: Int
        ): RewardPool {
            val now = LocalDateTime.now()
            return RewardPool(
                id = RewardPoolId.generate(),
                name = name,
                description = description,
                isActive = true,
                totalBudget = totalBudget,
                usedBudget = 0,
                createdAt = now,
                updatedAt = now
            )
        }
    }
    
    fun updateBudget(newBudget: Int): RewardPool {
        return copy(
            totalBudget = newBudget,
            updatedAt = LocalDateTime.now()
        )
    }
    
    fun addUsedBudget(amount: Int): RewardPool {
        return copy(
            usedBudget = usedBudget + amount,
            updatedAt = LocalDateTime.now()
        )
    }
    
    fun activate(): RewardPool {
        return copy(
            isActive = true,
            updatedAt = LocalDateTime.now()
        )
    }
    
    fun deactivate(): RewardPool {
        return copy(
            isActive = false,
            updatedAt = LocalDateTime.now()
        )
    }
}
