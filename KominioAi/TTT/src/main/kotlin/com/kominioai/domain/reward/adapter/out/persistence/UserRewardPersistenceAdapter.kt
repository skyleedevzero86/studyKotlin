package com.kominioai.domain.reward.adapter.out.persistence

import com.kominioai.domain.reward.application.port.out.UserRewardPersistencePort
import com.kominioai.domain.reward.domain.model.UserReward
import com.kominioai.domain.reward.domain.model.UserRewardId
import com.kominioai.domain.reward.domain.model.RewardStatus
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class UserRewardPersistenceAdapter(
    private val userRewardRepository: UserRewardRepository
) : UserRewardPersistencePort {

    override fun save(userReward: UserReward): Mono<UserReward> {
        return userRewardRepository.save(userReward.toEntity())
            .map { it.toDomain() }
    }

    override fun findById(id: UserRewardId): Mono<UserReward?> {
        return userRewardRepository.findById(id.value)
            .map { it.toDomain() }
            .switchIfEmpty(Mono.empty())
    }

    override fun findByUserId(userId: String): Flux<UserReward> {
        return userRewardRepository.findByUserId(userId)
            .map { it.toDomain() }
    }

    override fun findByUserIdAndStatus(userId: String, status: RewardStatus): Flux<UserReward> {
        return userRewardRepository.findByUserIdAndStatus(userId, status.name)
            .map { it.toDomain() }
    }

    override fun findByClaimCode(claimCode: String): Mono<UserReward?> {
        return userRewardRepository.findByClaimCode(claimCode)
            .map { it.toDomain() }
            .switchIfEmpty(Mono.empty())
    }

    override fun findExpiredRewards(): Flux<UserReward> {
        return userRewardRepository.findExpiredRewards()
            .map { it.toDomain() }
    }

    override fun countByUserId(userId: String): Mono<Long> {
        return userRewardRepository.countByUserId(userId)
    }

    override fun countByStatus(status: RewardStatus): Mono<Long> {
        return userRewardRepository.countByStatus(status.name)
    }
}
