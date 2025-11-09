package com.kominioai.domain.bulletin.adapter.out.persistence

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface CommentRepository : ReactiveCrudRepository<CommentEntity, String> {
    fun findByPostId(postId: String): Flux<CommentEntity>
    fun findByParentId(parentId: String): Flux<CommentEntity>
    fun findByAuthorId(authorId: String): Flux<CommentEntity>
    fun findByPostIdAndParentIdIsNull(postId: String): Flux<CommentEntity>
    fun countByPostId(postId: String): Mono<Long>
    fun countByAuthorId(authorId: String): Mono<Long>
}
