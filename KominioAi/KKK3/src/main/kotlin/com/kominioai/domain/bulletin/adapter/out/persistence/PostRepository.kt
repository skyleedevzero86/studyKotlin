package com.kominioai.domain.bulletin.adapter.out.persistence

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface PostRepository : ReactiveCrudRepository<PostEntity, String> {
    fun findByCategory(category: String): Flux<PostEntity>
    fun findByAuthorId(authorId: String): Flux<PostEntity>
    fun findByPinnedTrue(): Flux<PostEntity>
    fun findByTitleContainingIgnoreCase(title: String): Flux<PostEntity>
    fun findByContentContainingIgnoreCase(content: String): Flux<PostEntity>
    fun countByCategory(category: String): Mono<Long>
    fun countByAuthorId(authorId: String): Mono<Long>
}
