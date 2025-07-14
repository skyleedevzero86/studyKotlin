package com.kominioai.domain.survey.adapter.out.event

import com.kominioai.domain.survey.application.port.out.EventPublisherPort
import com.kominioai.domain.survey.domain.event.DomainEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class SpringEventPublisherAdapter(
    private val eventPublisher: ApplicationEventPublisher
) : EventPublisherPort {

    override fun publish(event: DomainEvent): Mono<Void> {
        return Mono.fromRunnable<Void> {
            eventPublisher.publishEvent(event)
        }.then()
    }
} 