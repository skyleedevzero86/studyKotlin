package com.kominioai.domain.bulletin.domain.repository

import com.kominioai.domain.bulletin.domain.model.Post
import com.kominioai.domain.bulletin.domain.model.PostId
import com.kominioai.domain.bulletin.domain.model.PostCategory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface PostRepository {
    fun save(post: Post): Mono<Post>
    fun findById(id: PostId): Mono<Post?>
    fun findByCategory(category: PostCategory): Flux<Post>
    fun findByAuthorId(authorId: String): Flux<Post>
    fun findPinnedPosts(): Flux<Post>
    fun findByTitleContaining(title: String): Flux<Post>
    fun findByContentContaining(content: String): Flux<Post>
    fun findAll(): Flux<Post>
    fun deleteById(id: PostId): Mono<Boolean>
    fun count(): Mono<Long>
    fun countByCategory(category: PostCategory): Mono<Long>
    fun countByAuthorId(authorId: String): Mono<Long>
}
