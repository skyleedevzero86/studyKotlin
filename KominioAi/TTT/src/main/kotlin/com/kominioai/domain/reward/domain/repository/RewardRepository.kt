package com.kominioai.domain.reward.domain.repository

import com.kominioai.domain.reward.domain.model.Reward
import com.kominioai.domain.reward.domain.model.RewardId
import com.kominioai.domain.reward.domain.model.RewardType
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface RewardRepository {
    fun save(reward: Reward): Mono<Reward>
    fun findById(id: RewardId): Mono<Reward?>
    fun findByType(type: RewardType): Flux<Reward>
    fun findActiveRewards(): Flux<Reward>
    fun deleteById(id: RewardId): Mono<Boolean>
    fun count(): Mono<Long>
}
