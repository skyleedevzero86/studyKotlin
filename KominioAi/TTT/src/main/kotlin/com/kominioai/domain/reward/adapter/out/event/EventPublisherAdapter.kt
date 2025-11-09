package com.kominioai.domain.reward.adapter.out.event

import com.kominioai.domain.reward.application.port.out.EventPublisherPort
import com.kominioai.domain.reward.domain.event.RewardWonEvent
import com.kominioai.domain.reward.domain.event.RewardClaimedEvent
import com.kominioai.domain.reward.domain.event.CreatorRewardEarnedEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class EventPublisherAdapter(
    private val eventPublisher: ApplicationEventPublisher
) : EventPublisherPort {

    override fun publishRewardWonEvent(event: RewardWonEvent): Mono<Void> {
        return Mono.fromRunnable {
            eventPublisher.publishEvent(event)
        }
    }

    override fun publishRewardClaimedEvent(event: RewardClaimedEvent): Mono<Void> {
        return Mono.fromRunnable {
            eventPublisher.publishEvent(event)
        }
    }

    override fun publishCreatorRewardEarnedEvent(event: CreatorRewardEarnedEvent): Mono<Void> {
        return Mono.fromRunnable {
            eventPublisher.publishEvent(event)
        }
    }
}
