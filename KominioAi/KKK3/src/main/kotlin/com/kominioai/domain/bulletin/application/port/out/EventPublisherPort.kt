package com.kominioai.domain.bulletin.application.port.out

import com.kominioai.domain.bulletin.domain.event.*
import reactor.core.publisher.Mono

interface EventPublisherPort {
    fun publishPostCreatedEvent(event: PostCreatedEvent): Mono<Void>
    fun publishPostUpdatedEvent(event: PostUpdatedEvent): Mono<Void>
    fun publishPostDeletedEvent(event: PostDeletedEvent): Mono<Void>
    fun publishCommentCreatedEvent(event: CommentCreatedEvent): Mono<Void>
    fun publishCommentUpdatedEvent(event: CommentUpdatedEvent): Mono<Void>
    fun publishCommentDeletedEvent(event: CommentDeletedEvent): Mono<Void>
}
