package com.kominioai.domain.reward.domain.repository

import com.kominioai.domain.reward.domain.model.UserReward
import com.kominioai.domain.reward.domain.model.UserRewardId
import com.kominioai.domain.reward.domain.model.RewardStatus
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface UserRewardRepository {
    fun save(userReward: UserReward): Mono<UserReward>
    fun findById(id: UserRewardId): Mono<UserReward?>
    fun findByUserId(userId: String): Flux<UserReward>
    fun findByUserIdAndStatus(userId: String, status: RewardStatus): Flux<UserReward>
    fun findByClaimCode(claimCode: String): Mono<UserReward?>
    fun findExpiredRewards(): Flux<UserReward>
    fun countByUserId(userId: String): Mono<Long>
    fun countByStatus(status: RewardStatus): Mono<Long>
}
