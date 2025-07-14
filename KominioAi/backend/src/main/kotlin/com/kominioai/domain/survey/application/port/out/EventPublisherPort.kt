package com.kominioai.domain.survey.application.port.out

import com.kominioai.domain.survey.domain.event.DomainEvent
import reactor.core.publisher.Mono

interface EventPublisherPort {
    fun publish(event: DomainEvent): Mono<Void>
}