package com.kominioai.domain.reward.adapter.in.web

import com.kominioai.domain.reward.application.dto.*
import com.kominioai.domain.reward.application.port.`in`.RewardUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/rewards")
class RewardController(
    private val rewardUseCase: RewardUseCase
) {

    @PostMapping
    fun createReward(@RequestBody request: CreateRewardRequest): Mono<ResponseEntity<RewardResponse>> {
        return rewardUseCase.createReward(request)
            .map { ResponseEntity.status(HttpStatus.CREATED).body(it) }
    }

    @PutMapping("/{id}")
    fun updateReward(
        @PathVariable id: String,
        @RequestBody request: UpdateRewardRequest
    ): Mono<ResponseEntity<RewardResponse>> {
        return rewardUseCase.updateReward(id, request)
            .map { ResponseEntity.ok(it) }
    }

    @DeleteMapping("/{id}")
    fun deleteReward(@PathVariable id: String): Mono<ResponseEntity<Void>> {
        return rewardUseCase.deleteReward(id)
            .map { ResponseEntity.noContent().build<Void>() }
    }

    @GetMapping("/{id}")
    fun getReward(@PathVariable id: String): Mono<ResponseEntity<RewardResponse>> {
        return rewardUseCase.getReward(id)
            .map { ResponseEntity.ok(it) }
    }

    @GetMapping
    fun getRewards(@RequestParam(required = false) type: String?,
                   @RequestParam(required = false) isActive: Boolean?,
                   @RequestParam(defaultValue = "0") page: Int,
                   @RequestParam(defaultValue = "20") size: Int): Mono<ResponseEntity<RewardListResponse>> {
        val request = GetRewardsRequest(
            type = type?.let { com.kominioai.domain.reward.domain.model.RewardType.valueOf(it) },
            isActive = isActive,
            page = page,
            size = size
        )
        
        return rewardUseCase.getRewards(request)
            .map { ResponseEntity.ok(it) }
    }

    @PostMapping("/participant")
    fun processParticipantReward(@RequestBody request: ProcessParticipantRewardRequest): Mono<ResponseEntity<ParticipantRewardResponse>> {
        return rewardUseCase.processParticipantReward(request)
            .map { ResponseEntity.ok(it) }
    }

    @PostMapping("/claim")
    fun claimReward(@RequestBody request: ClaimRewardRequest): Mono<ResponseEntity<ClaimRewardResponse>> {
        return rewardUseCase.claimReward(request)
            .map { ResponseEntity.ok(it) }
    }

    @GetMapping("/user/{userId}")
    fun getUserRewards(@PathVariable userId: String): Mono<ResponseEntity<UserRewardListResponse>> {
        return rewardUseCase.getUserRewards(userId)
            .map { ResponseEntity.ok(it) }
    }

    @GetMapping("/statistics")
    fun getRewardStatistics(): Mono<ResponseEntity<RewardStatisticsResponse>> {
        return rewardUseCase.getRewardStatistics()
            .map { ResponseEntity.ok(it) }
    }
}
