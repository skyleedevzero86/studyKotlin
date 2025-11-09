package com.kominioai.domain.reward.application.service

import com.kominioai.domain.reward.application.dto.*
import com.kominioai.domain.reward.application.port.`in`.CreatorRewardUseCase
import com.kominioai.domain.reward.application.port.out.CreatorRewardPersistencePort
import com.kominioai.domain.reward.application.port.out.EventPublisherPort
import com.kominioai.domain.reward.domain.model.*
import com.kominioai.domain.reward.domain.service.CreatorRewardCalculationService
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class CreatorRewardApplicationService(
    private val creatorRewardPersistencePort: CreatorRewardPersistencePort,
    private val eventPublisherPort: EventPublisherPort,
    private val creatorRewardCalculationService: CreatorRewardCalculationService
) : CreatorRewardUseCase {

    override fun calculateCreatorReward(request: CalculateCreatorRewardRequest): Mono<CreatorRewardResponse> {
        val creatorReward = creatorRewardCalculationService.calculateCreatorReward(
            surveyId = request.surveyId,
            creatorId = request.creatorId,
            participationCount = request.participationCount,
            targetCount = request.targetCount,
            baseRewardValue = request.baseRewardValue
        )
        
        return if (creatorReward != null) {
            creatorRewardPersistencePort.save(creatorReward)
                .flatMap { savedReward ->
                    val event = CreatorRewardEarnedEvent(
                        creatorId = savedReward.creatorId,
                        surveyId = savedReward.surveyId,
                        creatorRewardId = savedReward.id,
                        rewardType = savedReward.rewardType,
                        rewardValue = savedReward.rewardValue,
                        participationRate = savedReward.participationRate,
                        earnedAt = savedReward.createdAt
                    )
                    
                    eventPublisherPort.publishCreatorRewardEarnedEvent(event)
                        .then(Mono.just(savedReward.toResponse()))
                }
        } else {
            Mono.error(IllegalArgumentException("리워드 지급 조건을 만족하지 않습니다"))
        }
    }

    override fun claimCreatorReward(request: ClaimCreatorRewardRequest): Mono<ClaimCreatorRewardResponse> {
        return creatorRewardPersistencePort.findById(CreatorRewardId(request.creatorRewardId))
            .switchIfEmpty(Mono.error(IllegalArgumentException("생성자 리워드를 찾을 수 없습니다")))
            .flatMap { creatorReward ->
                if (!creatorReward.isEligible()) {
                    return@flatMap Mono.just(ClaimCreatorRewardResponse(
                        success = false,
                        message = "리워드 수령 조건을 만족하지 않습니다",
                        creatorRewardId = null,
                        rewardType = null,
                        rewardValue = null,
                        claimedAt = null
                    ))
                }
                
                if (creatorReward.isClaimed) {
                    return@flatMap Mono.just(ClaimCreatorRewardResponse(
                        success = false,
                        message = "이미 수령한 리워드입니다",
                        creatorRewardId = null,
                        rewardType = null,
                        rewardValue = null,
                        claimedAt = null
                    ))
                }
                
                val claimedReward = creatorReward.claim()
                creatorRewardPersistencePort.save(claimedReward)
                    .map { savedReward ->
                        ClaimCreatorRewardResponse(
                            success = true,
                            message = "생성자 리워드가 성공적으로 수령되었습니다",
                            creatorRewardId = savedReward.id.value,
                            rewardType = savedReward.rewardType,
                            rewardValue = savedReward.rewardValue,
                            claimedAt = savedReward.claimedAt
                        )
                    }
            }
    }

    override fun getCreatorRewards(creatorId: String): Mono<CreatorRewardListResponse> {
        return creatorRewardPersistencePort.findByCreatorId(creatorId)
            .collectList()
            .flatMap { creatorRewards ->
                val totalCount = creatorRewards.size.toLong()
                val eligibleCount = creatorRewards.count { it.isEligible() }.toLong()
                val claimedCount = creatorRewards.count { it.isClaimed }.toLong()
                val totalValue = creatorRewards.sumOf { it.rewardValue }.toLong()
                
                Mono.just(CreatorRewardListResponse(
                    rewards = creatorRewards.map { it.toResponse() },
                    totalCount = totalCount,
                    eligibleCount = eligibleCount,
                    claimedCount = claimedCount,
                    totalValue = totalValue
                ))
            }
    }

    override fun getCreatorRewardStatistics(creatorId: String): Mono<CreatorRewardStatisticsResponse> {
        return creatorRewardPersistencePort.findByCreatorId(creatorId)
            .collectList()
            .map { creatorRewards ->
                val totalRewards = creatorRewards.size.toLong()
                val totalValue = creatorRewards.sumOf { it.rewardValue }.toLong()
                val eligibleRewards = creatorRewards.count { it.isEligible() }.toLong()
                val claimedRewards = creatorRewards.count { it.isClaimed }.toLong()
                val averageParticipationRate = if (creatorRewards.isNotEmpty()) {
                    creatorRewards.map { it.participationRate }.average()
                } else {
                    0.0
                }
                
                val rewardsByType = creatorRewards.groupBy { it.rewardType }
                    .mapValues { it.value.size.toLong() }
                
                CreatorRewardStatisticsResponse(
                    totalRewards = totalRewards,
                    totalValue = totalValue,
                    eligibleRewards = eligibleRewards,
                    claimedRewards = claimedRewards,
                    averageParticipationRate = averageParticipationRate,
                    rewardsByType = rewardsByType,
                    monthlyEarnings = emptyList() // TODO: 구현 필요
                )
            }
    }
}

private fun CreatorReward.toResponse(): CreatorRewardResponse {
    return CreatorRewardResponse(
        id = id.value,
        surveyId = surveyId,
        creatorId = creatorId,
        rewardType = rewardType,
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
