package com.kominioai.domain.reward.application.port.out

import com.kominioai.domain.reward.domain.model.CreatorReward
import com.kominioai.domain.reward.domain.model.CreatorRewardId
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface CreatorRewardPersistencePort {
    fun save(creatorReward: CreatorReward): Mono<CreatorReward>
    fun findById(id: CreatorRewardId): Mono<CreatorReward?>
    fun findByCreatorId(creatorId: String): Flux<CreatorReward>
    fun findBySurveyId(surveyId: String): Mono<CreatorReward?>
    fun findEligibleRewards(creatorId: String): Flux<CreatorReward>
    fun countByCreatorId(creatorId: String): Mono<Long>
}
