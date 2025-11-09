package com.kominioai.domain.bulletin.adapter.out.event

import com.kominioai.domain.bulletin.application.port.out.EventPublisherPort
import com.kominioai.domain.bulletin.domain.event.*
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class EventPublisherAdapter(
    private val eventPublisher: ApplicationEventPublisher
) : EventPublisherPort {

    override fun publishPostCreatedEvent(event: PostCreatedEvent): Mono<Void> {
        return Mono.fromRunnable {
            eventPublisher.publishEvent(event)
        }
    }

    override fun publishPostUpdatedEvent(event: PostUpdatedEvent): Mono<Void> {
        return Mono.fromRunnable {
            eventPublisher.publishEvent(event)
        }
    }

    override fun publishPostDeletedEvent(event: PostDeletedEvent): Mono<Void> {
        return Mono.fromRunnable {
            eventPublisher.publishEvent(event)
        }
    }

    override fun publishCommentCreatedEvent(event: CommentCreatedEvent): Mono<Void> {
        return Mono.fromRunnable {
            eventPublisher.publishEvent(event)
        }
    }

    override fun publishCommentUpdatedEvent(event: CommentUpdatedEvent): Mono<Void> {
        return Mono.fromRunnable {
            eventPublisher.publishEvent(event)
        }
    }

    override fun publishCommentDeletedEvent(event: CommentDeletedEvent): Mono<Void> {
        return Mono.fromRunnable {
            eventPublisher.publishEvent(event)
        }
    }
}
