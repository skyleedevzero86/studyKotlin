package com.kominioai.domain.reward.application.port.`in`

import com.kominioai.domain.reward.application.dto.*
import reactor.core.publisher.Mono

interface RewardUseCase {
    fun createReward(request: CreateRewardRequest): Mono<RewardResponse>
    fun updateReward(id: String, request: UpdateRewardRequest): Mono<RewardResponse>
    fun deleteReward(id: String): Mono<Void>
    fun getReward(id: String): Mono<RewardResponse>
    fun getRewards(request: GetRewardsRequest): Mono<RewardListResponse>
    fun processParticipantReward(request: ProcessParticipantRewardRequest): Mono<ParticipantRewardResponse>
    fun claimReward(request: ClaimRewardRequest): Mono<ClaimRewardResponse>
    fun getUserRewards(userId: String): Mono<UserRewardListResponse>
    fun getRewardStatistics(): Mono<RewardStatisticsResponse>
}
