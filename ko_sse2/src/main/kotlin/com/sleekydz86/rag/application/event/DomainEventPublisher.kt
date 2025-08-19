package com.sleekydz86.rag.application.event

import com.sleekydz86.rag.domain.event.DomainEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class DomainEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    fun publish(event: DomainEvent) {
        applicationEventPublisher.publishEvent(event)
    }
}