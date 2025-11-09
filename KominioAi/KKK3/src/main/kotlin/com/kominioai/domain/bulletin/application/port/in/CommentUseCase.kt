package com.kominioai.domain.bulletin.application.port.`in`

import com.kominioai.domain.bulletin.application.dto.*
import reactor.core.publisher.Mono

interface CommentUseCase {
    fun createComment(postId: String, request: CreateCommentRequest, authorId: String, authorName: String): Mono<CommentResponse>
    fun updateComment(commentId: String, request: UpdateCommentRequest, userId: String): Mono<CommentResponse>
    fun deleteComment(commentId: String, userId: String, userRole: String): Mono<Void>
    fun getComment(commentId: String): Mono<CommentResponse>
    fun getCommentsByPostId(postId: String): Mono<CommentListResponse>
    fun likeComment(commentId: String, userId: String): Mono<CommentResponse>
    fun getCommentTree(postId: String): Mono<CommentListResponse>
}
