package com.kominioai.domain.reward.adapter.out.persistence

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface RewardRepository : ReactiveCrudRepository<RewardEntity, String> {
    fun findByType(type: String): Flux<RewardEntity>
    fun findByIsActiveTrue(): Flux<RewardEntity>
}
