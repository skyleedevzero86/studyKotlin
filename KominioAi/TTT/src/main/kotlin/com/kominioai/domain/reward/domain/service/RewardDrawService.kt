package com.kominioai.domain.reward.domain.service

import com.kominioai.domain.reward.domain.model.Reward
import com.kominioai.domain.reward.domain.model.ParticipantReward
import org.springframework.stereotype.Service
import java.security.SecureRandom

@Service
class RewardDrawService {
    
    private val random = SecureRandom()
    
    fun shouldWinReward(participantReward: ParticipantReward): Boolean {
        if (!participantReward.enabled) {
            return false
        }
        
        val randomValue = random.nextDouble()
        return randomValue <= participantReward.probability
    }
    
    fun selectRandomReward(rewards: List<Reward>): Reward? {
        if (rewards.isEmpty()) {
            return null
        }
        
        val activeRewards = rewards.filter { it.isActive }
        if (activeRewards.isEmpty()) {
            return null
        }
        
        val randomIndex = random.nextInt(activeRewards.size)
        return activeRewards[randomIndex]
    }
    
    fun calculateRewardProbability(
        baseProbability: Double,
        participationCount: Int,
        targetCount: Int
    ): Double {
        val participationRate = if (targetCount > 0) {
            participationCount.toDouble() / targetCount
        } else {
            0.0
        }
        
        // 참여율이 높을수록 확률 증가 (최대 2배까지)
        val multiplier = 1.0 + participationRate
        return (baseProbability * multiplier).coerceAtMost(1.0)
    }
}
