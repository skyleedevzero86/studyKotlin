package com.kominioai.domain.reward.application.port.`in`

import com.kominioai.domain.reward.application.dto.*
import reactor.core.publisher.Mono

interface CreatorRewardUseCase {
    fun calculateCreatorReward(request: CalculateCreatorRewardRequest): Mono<CreatorRewardResponse>
    fun claimCreatorReward(request: ClaimCreatorRewardRequest): Mono<ClaimCreatorRewardResponse>
    fun getCreatorRewards(creatorId: String): Mono<CreatorRewardListResponse>
    fun getCreatorRewardStatistics(creatorId: String): Mono<CreatorRewardStatisticsResponse>
}
