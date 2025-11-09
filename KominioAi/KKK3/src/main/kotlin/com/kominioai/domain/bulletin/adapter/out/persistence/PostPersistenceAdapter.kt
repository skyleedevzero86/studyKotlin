package com.kominioai.domain.bulletin.adapter.out.persistence

import com.kominioai.domain.bulletin.application.port.out.PostPersistencePort
import com.kominioai.domain.bulletin.domain.model.Post
import com.kominioai.domain.bulletin.domain.model.PostId
import com.kominioai.domain.bulletin.domain.model.PostCategory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class PostPersistenceAdapter(
    private val postRepository: PostRepository
) : PostPersistencePort {

    override fun save(post: Post): Mono<Post> {
        return postRepository.save(PostEntity.fromDomain(post))
            .map { it.toDomain() }
    }

    override fun findById(id: PostId): Mono<Post?> {
        return postRepository.findById(id.value)
            .map { it.toDomain() }
            .switchIfEmpty(Mono.empty())
    }

    override fun findByCategory(category: PostCategory): Flux<Post> {
        return postRepository.findByCategory(category.name)
            .map { it.toDomain() }
    }

    override fun findByAuthorId(authorId: String): Flux<Post> {
        return postRepository.findByAuthorId(authorId)
            .map { it.toDomain() }
    }

    override fun findPinnedPosts(): Flux<Post> {
        return postRepository.findByPinnedTrue()
            .map { it.toDomain() }
    }

    override fun findByTitleContaining(title: String): Flux<Post> {
        return postRepository.findByTitleContainingIgnoreCase(title)
            .map { it.toDomain() }
    }

    override fun findByContentContaining(content: String): Flux<Post> {
        return postRepository.findByContentContainingIgnoreCase(content)
            .map { it.toDomain() }
    }

    override fun findAll(): Flux<Post> {
        return postRepository.findAll()
            .map { it.toDomain() }
    }

    override fun deleteById(id: PostId): Mono<Boolean> {
        return postRepository.deleteById(id.value)
            .then(Mono.just(true))
    }

    override fun count(): Mono<Long> {
        return postRepository.count()
    }

    override fun countByCategory(category: PostCategory): Mono<Long> {
        return postRepository.countByCategory(category.name)
    }

    override fun countByAuthorId(authorId: String): Mono<Long> {
        return postRepository.countByAuthorId(authorId)
    }
}
