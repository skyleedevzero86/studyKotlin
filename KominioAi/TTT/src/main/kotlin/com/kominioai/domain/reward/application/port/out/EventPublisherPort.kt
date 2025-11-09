package com.kominioai.domain.reward.application.port.out

import com.kominioai.domain.reward.domain.event.RewardWonEvent
import com.kominioai.domain.reward.domain.event.RewardClaimedEvent
import com.kominioai.domain.reward.domain.event.CreatorRewardEarnedEvent
import reactor.core.publisher.Mono

interface EventPublisherPort {
    fun publishRewardWonEvent(event: RewardWonEvent): Mono<Void>
    fun publishRewardClaimedEvent(event: RewardClaimedEvent): Mono<Void>
    fun publishCreatorRewardEarnedEvent(event: CreatorRewardEarnedEvent): Mono<Void>
}
