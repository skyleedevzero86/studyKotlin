package com.kominioai.domain.bulletin.adapter.out.persistence

import com.kominioai.domain.bulletin.application.port.out.CommentPersistencePort
import com.kominioai.domain.bulletin.domain.model.Comment
import com.kominioai.domain.bulletin.domain.model.CommentId
import com.kominioai.domain.bulletin.domain.model.PostId
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class CommentPersistenceAdapter(
    private val commentRepository: CommentRepository
) : CommentPersistencePort {

    override fun save(comment: Comment): Mono<Comment> {
        return commentRepository.save(CommentEntity.fromDomain(comment))
            .map { it.toDomain() }
    }

    override fun findById(id: CommentId): Mono<Comment?> {
        return commentRepository.findById(id.value)
            .map { it.toDomain() }
            .switchIfEmpty(Mono.empty())
    }

    override fun findByPostId(postId: PostId): Flux<Comment> {
        return commentRepository.findByPostId(postId.value)
            .map { it.toDomain() }
    }

    override fun findByParentId(parentId: CommentId): Flux<Comment> {
        return commentRepository.findByParentId(parentId.value)
            .map { it.toDomain() }
    }

    override fun findByAuthorId(authorId: String): Flux<Comment> {
        return commentRepository.findByAuthorId(authorId)
            .map { it.toDomain() }
    }

    override fun findRootCommentsByPostId(postId: PostId): Flux<Comment> {
        return commentRepository.findByPostIdAndParentIdIsNull(postId.value)
            .map { it.toDomain() }
    }

    override fun findAll(): Flux<Comment> {
        return commentRepository.findAll()
            .map { it.toDomain() }
    }

    override fun deleteById(id: CommentId): Mono<Boolean> {
        return commentRepository.deleteById(id.value)
            .then(Mono.just(true))
    }

    override fun count(): Mono<Long> {
        return commentRepository.count()
    }

    override fun countByPostId(postId: PostId): Mono<Long> {
        return commentRepository.countByPostId(postId.value)
    }

    override fun countByAuthorId(authorId: String): Mono<Long> {
        return commentRepository.countByAuthorId(authorId)
    }
}
