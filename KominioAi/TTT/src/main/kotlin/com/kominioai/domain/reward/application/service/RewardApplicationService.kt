package com.kominioai.domain.reward.application.service

import com.kominioai.domain.reward.application.dto.*
import com.kominioai.domain.reward.application.port.`in`.RewardUseCase
import com.kominioai.domain.reward.application.port.out.RewardPersistencePort
import com.kominioai.domain.reward.application.port.out.UserRewardPersistencePort
import com.kominioai.domain.reward.application.port.out.EventPublisherPort
import com.kominioai.domain.reward.domain.model.*
import com.kominioai.domain.reward.domain.service.RewardDrawService
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class RewardApplicationService(
    private val rewardPersistencePort: RewardPersistencePort,
    private val userRewardPersistencePort: UserRewardPersistencePort,
    private val eventPublisherPort: EventPublisherPort,
    private val rewardDrawService: RewardDrawService
) : RewardUseCase {

    override fun createReward(request: CreateRewardRequest): Mono<RewardResponse> {
        val reward = Reward.create(
            type = request.type,
            value = request.value,
            description = request.description,
            imageUrl = request.imageUrl,
            probability = request.probability
        )
        
        return rewardPersistencePort.save(reward)
            .map { it.toResponse() }
    }

    override fun updateReward(id: String, request: UpdateRewardRequest): Mono<RewardResponse> {
        return rewardPersistencePort.findById(RewardId(id))
            .switchIfEmpty(Mono.error(IllegalArgumentException("리워드를 찾을 수 없습니다")))
            .flatMap { reward ->
                val updatedReward = reward.update(
                    type = request.type,
                    value = request.value,
                    description = request.description,
                    imageUrl = request.imageUrl,
                    probability = request.probability,
                    isActive = request.isActive
                )
                rewardPersistencePort.save(updatedReward)
            }
            .map { it.toResponse() }
    }

    override fun deleteReward(id: String): Mono<Void> {
        return rewardPersistencePort.deleteById(RewardId(id))
            .then()
    }

    override fun getReward(id: String): Mono<RewardResponse> {
        return rewardPersistencePort.findById(RewardId(id))
            .switchIfEmpty(Mono.error(IllegalArgumentException("리워드를 찾을 수 없습니다")))
            .map { it.toResponse() }
    }

    override fun getRewards(request: GetRewardsRequest): Mono<RewardListResponse> {
        return rewardPersistencePort.findActiveRewards()
            .filter { reward ->
                request.type?.let { reward.type == it } ?: true &&
                request.isActive?.let { reward.isActive == it } ?: true
            }
            .collectList()
            .flatMap { rewards ->
                val totalCount = rewards.size.toLong()
                val startIndex = request.page * request.size
                val endIndex = minOf(startIndex + request.size, rewards.size)
                val pagedRewards = rewards.subList(startIndex, endIndex)
                
                Mono.just(RewardListResponse(
                    rewards = pagedRewards.map { it.toResponse() },
                    totalCount = totalCount,
                    page = request.page,
                    size = request.size,
                    totalPages = ((totalCount + request.size - 1) / request.size).toInt()
                ))
            }
    }

    override fun processParticipantReward(request: ProcessParticipantRewardRequest): Mono<ParticipantRewardResponse> {
        val participantReward = ParticipantReward.create(
            type = request.type,
            value = request.value,
            description = request.description,
            probability = request.probability,
            imageUrl = request.imageUrl
        )
        
        return if (rewardDrawService.shouldWinReward(participantReward)) {
            val reward = Reward.create(
                type = request.type,
                value = request.value,
                description = request.description,
                imageUrl = request.imageUrl,
                probability = request.probability
            )
            
            val userReward = UserReward.create(request.userId, reward)
            
            userRewardPersistencePort.save(userReward)
                .flatMap { savedUserReward ->
                    val event = RewardWonEvent(
                        userId = request.userId,
                        rewardId = reward.id,
                        rewardType = reward.type,
                        rewardValue = reward.value,
                        claimCode = savedUserReward.claimCode,
                        wonAt = savedUserReward.wonAt
                    )
                    
                    eventPublisherPort.publishRewardWonEvent(event)
                        .then(Mono.just(ParticipantRewardResponse(
                            won = true,
                            userRewardId = savedUserReward.id.value,
                            rewardType = savedUserReward.reward.type,
                            rewardValue = savedUserReward.reward.value,
                            description = savedUserReward.reward.description,
                            claimCode = savedUserReward.claimCode.value,
                            status = savedUserReward.status,
                            wonAt = savedUserReward.wonAt,
                            expiredAt = savedUserReward.expiredAt
                        )))
                }
        } else {
            Mono.just(ParticipantRewardResponse(
                won = false,
                userRewardId = null,
                rewardType = null,
                rewardValue = null,
                description = null,
                claimCode = null,
                status = null,
                wonAt = null,
                expiredAt = null
            ))
        }
    }

    override fun claimReward(request: ClaimRewardRequest): Mono<ClaimRewardResponse> {
        return userRewardPersistencePort.findByClaimCode(request.claimCode)
            .switchIfEmpty(Mono.error(IllegalArgumentException("유효하지 않은 클레임 코드입니다")))
            .flatMap { userReward ->
                if (userReward.status != RewardStatus.PENDING) {
                    return@flatMap Mono.just(ClaimRewardResponse(
                        success = false,
                        message = "이미 처리된 리워드입니다",
                        userRewardId = null,
                        rewardType = null,
                        rewardValue = null,
                        status = null,
                        claimedAt = null
                    ))
                }
                
                if (userReward.isExpired()) {
                    val expiredReward = userReward.expire()
                    return@flatMap userRewardPersistencePort.save(expiredReward)
                        .map { ClaimRewardResponse(
                            success = false,
                            message = "만료된 리워드입니다",
                            userRewardId = null,
                            rewardType = null,
                            rewardValue = null,
                            status = null,
                            claimedAt = null
                        ) }
                }
                
                val claimedReward = userReward.claim()
                userRewardPersistencePort.save(claimedReward)
                    .flatMap { savedReward ->
                        val event = RewardClaimedEvent(
                            userId = savedReward.userId,
                            rewardId = savedReward.rewardId,
                            rewardType = savedReward.reward.type,
                            rewardValue = savedReward.reward.value,
                            claimedAt = savedReward.claimedAt!!
                        )
                        
                        eventPublisherPort.publishRewardClaimedEvent(event)
                            .then(Mono.just(ClaimRewardResponse(
                                success = true,
                                message = "리워드가 성공적으로 수령되었습니다",
                                userRewardId = savedReward.id.value,
                                rewardType = savedReward.reward.type,
                                rewardValue = savedReward.reward.value,
                                status = savedReward.status,
                                claimedAt = savedReward.claimedAt
                            )))
                    }
            }
    }

    override fun getUserRewards(userId: String): Mono<UserRewardListResponse> {
        return userRewardPersistencePort.findByUserId(userId)
            .collectList()
            .flatMap { userRewards ->
                val totalCount = userRewards.size.toLong()
                val pendingCount = userRewards.count { it.status == RewardStatus.PENDING }.toLong()
                val claimedCount = userRewards.count { it.status == RewardStatus.CLAIMED }.toLong()
                val expiredCount = userRewards.count { it.status == RewardStatus.EXPIRED }.toLong()
                
                Mono.just(UserRewardListResponse(
                    rewards = userRewards.map { it.toResponse() },
                    totalCount = totalCount,
                    pendingCount = pendingCount,
                    claimedCount = claimedCount,
                    expiredCount = expiredCount
                ))
            }
    }

    override fun getRewardStatistics(): Mono<RewardStatisticsResponse> {
        return Mono.zip(
            rewardPersistencePort.count(),
            userRewardPersistencePort.countByStatus(RewardStatus.PENDING),
            userRewardPersistencePort.countByStatus(RewardStatus.CLAIMED),
            userRewardPersistencePort.countByStatus(RewardStatus.EXPIRED)
        ).map { (totalRewards, pendingCount, claimedCount, expiredCount) ->
            RewardStatisticsResponse(
                totalRewards = totalRewards,
                totalUserRewards = pendingCount + claimedCount + expiredCount,
                totalClaimedRewards = claimedCount,
                totalPendingRewards = pendingCount,
                totalExpiredRewards = expiredCount,
                rewardsByType = emptyMap(), // TODO: 구현 필요
                recentWinners = emptyList(), // TODO: 구현 필요
                monthlyStats = emptyList() // TODO: 구현 필요
            )
        }
    }
}

private fun Reward.toResponse(): RewardResponse {
    return RewardResponse(
        id = id.value,
        type = type,
        value = value,
        description = description,
        imageUrl = imageUrl,
        probability = probability,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

private fun UserReward.toResponse(): UserRewardResponse {
    return UserRewardResponse(
        id = id.value,
        rewardType = reward.type,
        rewardValue = reward.value,
        description = reward.description,
        claimCode = claimCode.value,
        status = status,
        wonAt = wonAt,
        claimedAt = claimedAt,
        expiredAt = expiredAt
    )
}
