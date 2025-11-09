package com.kominioai.domain.reward.domain.service

import com.kominioai.domain.reward.domain.model.CreatorReward
import com.kominioai.domain.reward.domain.model.RewardType
import org.springframework.stereotype.Service

@Service
class CreatorRewardCalculationService {
    
    fun calculateCreatorReward(
        surveyId: String,
        creatorId: String,
        participationCount: Int,
        targetCount: Int,
        baseRewardValue: Int = 1000
    ): CreatorReward? {
        val participationRate = if (targetCount > 0) {
            participationCount.toDouble() / targetCount
        } else {
            0.0
        }
        
        // 80% 이상 달성 시 리워드 지급
        if (participationRate < 0.8) {
            return null
        }
        
        val rewardValue = calculateRewardValue(participationRate, baseRewardValue)
        val description = "설문 참여율 달성 리워드 (${(participationRate * 100).toInt()}%)"
        
        return CreatorReward.create(
            surveyId = surveyId,
            creatorId = creatorId,
            rewardType = RewardType.CASH,
            rewardValue = rewardValue,
            description = description,
            participationRate = participationRate,
            targetParticipationRate = 0.8
        )
    }
    
    private fun calculateRewardValue(participationRate: Double, baseValue: Int): Int {
        // 참여율에 따른 리워드 계산
        return when {
            participationRate >= 1.0 -> (baseValue * 1.5).toInt() // 100% 이상
            participationRate >= 0.9 -> (baseValue * 1.2).toInt() // 90% 이상
            participationRate >= 0.8 -> baseValue // 80% 이상
            else -> 0
        }
    }
}
