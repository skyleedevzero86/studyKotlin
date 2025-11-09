package com.kominioai.domain.reward.adapter.out.persistence

import com.kominioai.domain.reward.application.port.out.CreatorRewardPersistencePort
import com.kominioai.domain.reward.domain.model.CreatorReward
import com.kominioai.domain.reward.domain.model.CreatorRewardId
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class CreatorRewardPersistenceAdapter(
    private val creatorRewardRepository: CreatorRewardRepository
) : CreatorRewardPersistencePort {

    override fun save(creatorReward: CreatorReward): Mono<CreatorReward> {
        return creatorRewardRepository.save(creatorReward.toEntity())
            .map { it.toDomain() }
    }

    override fun findById(id: CreatorRewardId): Mono<CreatorReward?> {
        return creatorRewardRepository.findById(id.value)
            .map { it.toDomain() }
            .switchIfEmpty(Mono.empty())
    }

    override fun findByCreatorId(creatorId: String): Flux<CreatorReward> {
        return creatorRewardRepository.findByCreatorId(creatorId)
            .map { it.toDomain() }
    }

    override fun findBySurveyId(surveyId: String): Mono<CreatorReward?> {
        return creatorRewardRepository.findBySurveyId(surveyId)
            .map { it.toDomain() }
            .switchIfEmpty(Mono.empty())
    }

    override fun findEligibleRewards(creatorId: String): Flux<CreatorReward> {
        return creatorRewardRepository.findEligibleRewards(creatorId)
            .map { it.toDomain() }
    }

    override fun countByCreatorId(creatorId: String): Mono<Long> {
        return creatorRewardRepository.countByCreatorId(creatorId)
    }
}
