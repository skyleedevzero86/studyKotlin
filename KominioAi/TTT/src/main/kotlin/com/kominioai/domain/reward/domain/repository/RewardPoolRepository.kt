package com.kominioai.domain.reward.domain.repository

import com.kominioai.domain.reward.domain.model.RewardPool
import com.kominioai.domain.reward.domain.model.RewardPoolId
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface RewardPoolRepository {
    fun save(rewardPool: RewardPool): Mono<RewardPool>
    fun findById(id: RewardPoolId): Mono<RewardPool?>
    fun findActivePools(): Flux<RewardPool>
    fun findAll(): Flux<RewardPool>
    fun deleteById(id: RewardPoolId): Mono<Boolean>
    fun count(): Mono<Long>
}
