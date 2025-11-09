package com.kominioai.domain.reward.adapter.out.persistence

import com.kominioai.domain.reward.application.port.out.RewardPersistencePort
import com.kominioai.domain.reward.domain.model.Reward
import com.kominioai.domain.reward.domain.model.RewardId
import com.kominioai.domain.reward.domain.model.RewardType
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class RewardPersistenceAdapter(
    private val rewardRepository: RewardRepository
) : RewardPersistencePort {

    override fun save(reward: Reward): Mono<Reward> {
        return rewardRepository.save(reward.toEntity())
            .map { it.toDomain() }
    }

    override fun findById(id: RewardId): Mono<Reward?> {
        return rewardRepository.findById(id.value)
            .map { it.toDomain() }
            .switchIfEmpty(Mono.empty())
    }

    override fun findByType(type: RewardType): Flux<Reward> {
        return rewardRepository.findByType(type)
            .map { it.toDomain() }
    }

    override fun findActiveRewards(): Flux<Reward> {
        return rewardRepository.findByIsActiveTrue()
            .map { it.toDomain() }
    }

    override fun deleteById(id: RewardId): Mono<Boolean> {
        return rewardRepository.deleteById(id.value)
            .then(Mono.just(true))
    }

    override fun count(): Mono<Long> {
        return rewardRepository.count()
    }
}
