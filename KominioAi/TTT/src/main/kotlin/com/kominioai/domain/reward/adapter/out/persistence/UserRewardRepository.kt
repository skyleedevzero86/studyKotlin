package com.kominioai.domain.reward.adapter.out.persistence

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

interface UserRewardRepository : ReactiveCrudRepository<UserRewardEntity, String> {
    fun findByUserId(userId: String): Flux<UserRewardEntity>
    fun findByUserIdAndStatus(userId: String, status: String): Flux<UserRewardEntity>
    fun findByClaimCode(claimCode: String): Mono<UserRewardEntity>
    fun findExpiredRewards(): Flux<UserRewardEntity>
    fun countByUserId(userId: String): Mono<Long>
    fun countByStatus(status: String): Mono<Long>
}
