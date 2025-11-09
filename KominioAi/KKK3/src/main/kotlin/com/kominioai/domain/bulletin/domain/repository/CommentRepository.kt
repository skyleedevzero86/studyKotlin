package com.kominioai.domain.bulletin.domain.repository

import com.kominioai.domain.bulletin.domain.model.Comment
import com.kominioai.domain.bulletin.domain.model.CommentId
import com.kominioai.domain.bulletin.domain.model.PostId
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface CommentRepository {
    fun save(comment: Comment): Mono<Comment>
    fun findById(id: CommentId): Mono<Comment?>
    fun findByPostId(postId: PostId): Flux<Comment>
    fun findByParentId(parentId: CommentId): Flux<Comment>
    fun findByAuthorId(authorId: String): Flux<Comment>
    fun findRootCommentsByPostId(postId: PostId): Flux<Comment>
    fun findAll(): Flux<Comment>
    fun deleteById(id: CommentId): Mono<Boolean>
    fun count(): Mono<Long>
    fun countByPostId(postId: PostId): Mono<Long>
    fun countByAuthorId(authorId: String): Mono<Long>
}
