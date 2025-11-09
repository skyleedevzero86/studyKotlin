package com.kominioai.domain.reward.adapter.out.persistence

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface CreatorRewardRepository : ReactiveCrudRepository<CreatorRewardEntity, String> {
    fun findByCreatorId(creatorId: String): Flux<CreatorRewardEntity>
    fun findBySurveyId(surveyId: String): Mono<CreatorRewardEntity>
    fun findEligibleRewards(creatorId: String): Flux<CreatorRewardEntity>
    fun countByCreatorId(creatorId: String): Mono<Long>
}
