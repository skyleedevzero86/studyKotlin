package com.kominioai.domain.bulletin.adapter.in.web

import com.kominioai.domain.bulletin.application.dto.*
import com.kominioai.domain.bulletin.application.port.`in`.CommentUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/comments")
class CommentController(
    private val commentUseCase: CommentUseCase
) {

    @PostMapping("/post/{postId}")
    fun createComment(
        @PathVariable postId: String,
        @RequestBody request: CreateCommentRequest,
        @RequestHeader("X-User-Id") userId: String,
        @RequestHeader("X-User-Name") userName: String
    ): Mono<ResponseEntity<CommentResponse>> {
        return commentUseCase.createComment(postId, request, userId, userName)
            .map { ResponseEntity.status(HttpStatus.CREATED).body(it) }
    }

    @PutMapping("/{id}")
    fun updateComment(
        @PathVariable id: String,
        @RequestBody request: UpdateCommentRequest,
        @RequestHeader("X-User-Id") userId: String
    ): Mono<ResponseEntity<CommentResponse>> {
        return commentUseCase.updateComment(id, request, userId)
            .map { ResponseEntity.ok(it) }
    }

    @DeleteMapping("/{id}")
    fun deleteComment(
        @PathVariable id: String,
        @RequestHeader("X-User-Id") userId: String,
        @RequestHeader("X-User-Role") userRole: String
    ): Mono<ResponseEntity<Void>> {
        return commentUseCase.deleteComment(id, userId, userRole)
            .map { ResponseEntity.noContent().build<Void>() }
    }

    @GetMapping("/{id}")
    fun getComment(@PathVariable id: String): Mono<ResponseEntity<CommentResponse>> {
        return commentUseCase.getComment(id)
            .map { ResponseEntity.ok(it) }
    }

    @GetMapping("/post/{postId}")
    fun getCommentsByPostId(@PathVariable postId: String): Mono<ResponseEntity<CommentListResponse>> {
        return commentUseCase.getCommentsByPostId(postId)
            .map { ResponseEntity.ok(it) }
    }

    @GetMapping("/post/{postId}/tree")
    fun getCommentTree(@PathVariable postId: String): Mono<ResponseEntity<CommentListResponse>> {
        return commentUseCase.getCommentTree(postId)
            .map { ResponseEntity.ok(it) }
    }

    @PostMapping("/{id}/like")
    fun likeComment(
        @PathVariable id: String,
        @RequestHeader("X-User-Id") userId: String
    ): Mono<ResponseEntity<CommentResponse>> {
        return commentUseCase.likeComment(id, userId)
            .map { ResponseEntity.ok(it) }
    }
}
