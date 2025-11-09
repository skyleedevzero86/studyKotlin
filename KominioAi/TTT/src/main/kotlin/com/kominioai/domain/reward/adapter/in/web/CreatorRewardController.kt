package com.kominioai.domain.reward.adapter.in.web

import com.kominioai.domain.reward.application.dto.*
import com.kominioai.domain.reward.application.port.`in`.CreatorRewardUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/creator-rewards")
class CreatorRewardController(
    private val creatorRewardUseCase: CreatorRewardUseCase
) {

    @PostMapping("/calculate")
    fun calculateCreatorReward(@RequestBody request: CalculateCreatorRewardRequest): Mono<ResponseEntity<CreatorRewardResponse>> {
        return creatorRewardUseCase.calculateCreatorReward(request)
            .map { ResponseEntity.status(HttpStatus.CREATED).body(it) }
    }

    @PostMapping("/claim")
    fun claimCreatorReward(@RequestBody request: ClaimCreatorRewardRequest): Mono<ResponseEntity<ClaimCreatorRewardResponse>> {
        return creatorRewardUseCase.claimCreatorReward(request)
            .map { ResponseEntity.ok(it) }
    }

    @GetMapping("/creator/{creatorId}")
    fun getCreatorRewards(@PathVariable creatorId: String): Mono<ResponseEntity<CreatorRewardListResponse>> {
        return creatorRewardUseCase.getCreatorRewards(creatorId)
            .map { ResponseEntity.ok(it) }
    }

    @GetMapping("/creator/{creatorId}/statistics")
    fun getCreatorRewardStatistics(@PathVariable creatorId: String): Mono<ResponseEntity<CreatorRewardStatisticsResponse>> {
        return creatorRewardUseCase.getCreatorRewardStatistics(creatorId)
            .map { ResponseEntity.ok(it) }
    }
}
