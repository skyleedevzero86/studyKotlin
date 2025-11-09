package com.kominioai.domain.bulletin.application.service

import com.kominioai.domain.bulletin.application.dto.*
import com.kominioai.domain.bulletin.application.port.`in`.CommentUseCase
import com.kominioai.domain.bulletin.application.port.out.CommentPersistencePort
import com.kominioai.domain.bulletin.application.port.out.EventPublisherPort
import com.kominioai.domain.bulletin.domain.model.*
import com.kominioai.domain.bulletin.domain.service.CommentService
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class CommentApplicationService(
    private val commentPersistencePort: CommentPersistencePort,
    private val eventPublisherPort: EventPublisherPort,
    private val commentService: CommentService
) : CommentUseCase {

    override fun createComment(postId: String, request: CreateCommentRequest, authorId: String, authorName: String): Mono<CommentResponse> {
        if (!commentService.validateCommentCreation(request.content, authorId)) {
            return Mono.error(IllegalArgumentException("댓글 생성 조건을 만족하지 않습니다"))
        }
        
        val parentId = request.parentId?.let { CommentId(it) }
        val comment = Comment.create(
            postId = PostId(postId),
            parentId = parentId,
            content = request.content,
            authorId = authorId,
            authorName = authorName
        )
        
        return commentPersistencePort.save(comment)
            .flatMap { savedComment ->
                val event = CommentCreatedEvent(
                    commentId = savedComment.id,
                    postId = savedComment.postId,
                    parentId = savedComment.parentId,
                    content = savedComment.content,
                    authorId = savedComment.authorId,
                    authorName = savedComment.authorName,
                    createdAt = savedComment.createdAt
                )
                
                eventPublisherPort.publishCommentCreatedEvent(event)
                    .then(Mono.just(savedComment.toResponse()))
            }
    }

    override fun updateComment(commentId: String, request: UpdateCommentRequest, userId: String): Mono<CommentResponse> {
        return commentPersistencePort.findById(CommentId(commentId))
            .switchIfEmpty(Mono.error(IllegalArgumentException("댓글을 찾을 수 없습니다")))
            .flatMap { comment ->
                if (!commentService.canUserEditComment(comment, userId)) {
                    return@flatMap Mono.error(IllegalArgumentException("댓글 수정 권한이 없습니다"))
                }
                
                if (!commentService.validateCommentUpdate(request.content)) {
                    return@flatMap Mono.error(IllegalArgumentException("댓글 수정 조건을 만족하지 않습니다"))
                }
                
                val updatedComment = comment.update(request.content)
                
                commentPersistencePort.save(updatedComment)
                    .flatMap { savedComment ->
                        val event = CommentUpdatedEvent(
                            commentId = savedComment.id,
                            content = savedComment.content,
                            authorId = savedComment.authorId,
                            updatedAt = savedComment.updatedAt
                        )
                        
                        eventPublisherPort.publishCommentUpdatedEvent(event)
                            .then(Mono.just(savedComment.toResponse()))
                    }
            }
    }

    override fun deleteComment(commentId: String, userId: String, userRole: String): Mono<Void> {
        return commentPersistencePort.findById(CommentId(commentId))
            .switchIfEmpty(Mono.error(IllegalArgumentException("댓글을 찾을 수 없습니다")))
            .flatMap { comment ->
                if (!commentService.canUserDeleteComment(comment, userId, userRole)) {
                    return@flatMap Mono.error(IllegalArgumentException("댓글 삭제 권한이 없습니다"))
                }
                
                val event = CommentDeletedEvent(
                    commentId = comment.id,
                    postId = comment.postId.value,
                    authorId = comment.authorId,
                    deletedAt = java.time.LocalDateTime.now()
                )
                
                commentPersistencePort.deleteById(comment.id)
                    .flatMap { success ->
                        if (success) {
                            eventPublisherPort.publishCommentDeletedEvent(event)
                        } else {
                            Mono.error(RuntimeException("댓글 삭제에 실패했습니다"))
                        }
                    }
            }
    }

    override fun getComment(commentId: String): Mono<CommentResponse> {
        return commentPersistencePort.findById(CommentId(commentId))
            .switchIfEmpty(Mono.error(IllegalArgumentException("댓글을 찾을 수 없습니다")))
            .map { it.toResponse() }
    }

    override fun getCommentsByPostId(postId: String): Mono<CommentListResponse> {
        return commentPersistencePort.findByPostId(PostId(postId))
            .collectList()
            .map { comments ->
                CommentListResponse(
                    comments = comments.map { it.toResponse() },
                    totalCount = comments.size.toLong()
                )
            }
    }

    override fun likeComment(commentId: String, userId: String): Mono<CommentResponse> {
        return commentPersistencePort.findById(CommentId(commentId))
            .switchIfEmpty(Mono.error(IllegalArgumentException("댓글을 찾을 수 없습니다")))
            .flatMap { comment ->
                val updatedComment = comment.incrementLikeCount()
                commentPersistencePort.save(updatedComment)
                    .map { it.toResponse() }
            }
    }

    override fun getCommentTree(postId: String): Mono<CommentListResponse> {
        return commentPersistencePort.findByPostId(PostId(postId))
            .collectList()
            .map { comments ->
                val commentTree = commentService.buildCommentTree(comments)
                CommentListResponse(
                    comments = commentTree.map { it.toResponse() },
                    totalCount = comments.size.toLong()
                )
            }
    }
}

// 확장 함수들
private fun Comment.toResponse(): CommentResponse {
    return CommentResponse(
        id = this.id.value,
        postId = this.postId.value,
        parentId = this.parentId?.value,
        content = this.content,
        authorId = this.authorId,
        authorName = this.authorName,
        likeCount = this.likeCount,
        replyCount = this.replyCount,
        depth = this.getDepth(),
        isReply = this.isReply(),
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
