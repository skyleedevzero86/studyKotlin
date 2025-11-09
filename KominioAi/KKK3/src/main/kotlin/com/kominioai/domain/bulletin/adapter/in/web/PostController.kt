package com.kominioai.domain.bulletin.adapter.in.web

import com.kominioai.domain.bulletin.application.dto.*
import com.kominioai.domain.bulletin.application.port.`in`.PostUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/posts")
class PostController(
    private val postUseCase: PostUseCase
) {

    @PostMapping
    fun createPost(
        @RequestBody request: CreatePostRequest,
        @RequestHeader("X-User-Id") userId: String,
        @RequestHeader("X-User-Name") userName: String
    ): Mono<ResponseEntity<PostResponse>> {
        return postUseCase.createPost(request, userId, userName)
            .map { ResponseEntity.status(HttpStatus.CREATED).body(it) }
    }

    @PutMapping("/{id}")
    fun updatePost(
        @PathVariable id: String,
        @RequestBody request: UpdatePostRequest,
        @RequestHeader("X-User-Id") userId: String
    ): Mono<ResponseEntity<PostResponse>> {
        return postUseCase.updatePost(id, request, userId)
            .map { ResponseEntity.ok(it) }
    }

    @DeleteMapping("/{id}")
    fun deletePost(
        @PathVariable id: String,
        @RequestHeader("X-User-Id") userId: String,
        @RequestHeader("X-User-Role") userRole: String
    ): Mono<ResponseEntity<Void>> {
        return postUseCase.deletePost(id, userId, userRole)
            .map { ResponseEntity.noContent().build<Void>() }
    }

    @GetMapping("/{id}")
    fun getPost(@PathVariable id: String): Mono<ResponseEntity<PostResponse>> {
        return postUseCase.getPost(id)
            .map { ResponseEntity.ok(it) }
    }

    @GetMapping
    fun getPosts(
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) searchTerm: String?,
        @RequestParam(required = false) authorId: String?,
        @RequestParam(required = false) pinned: Boolean?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "DESC") sortDirection: String,
        @RequestHeader("X-User-Role") userRole: String
    ): Mono<ResponseEntity<PostListResponse>> {
        val request = GetPostsRequest(
            category = category?.let { com.kominioai.domain.bulletin.domain.model.PostCategory.valueOf(it) },
            searchTerm = searchTerm,
            authorId = authorId,
            pinned = pinned,
            page = page,
            size = size,
            sortBy = sortBy,
            sortDirection = sortDirection,
            userRole = userRole
        )
        
        return postUseCase.getPosts(request)
            .map { ResponseEntity.ok(it) }
    }

    @PostMapping("/{id}/view")
    fun incrementViewCount(@PathVariable id: String): Mono<ResponseEntity<Void>> {
        return postUseCase.incrementViewCount(id)
            .map { ResponseEntity.ok().build<Void>() }
    }

    @PostMapping("/{id}/like")
    fun likePost(
        @PathVariable id: String,
        @RequestHeader("X-User-Id") userId: String
    ): Mono<ResponseEntity<PostResponse>> {
        return postUseCase.likePost(id, userId)
            .map { ResponseEntity.ok(it) }
    }

    @GetMapping("/pinned")
    fun getPinnedPosts(): Mono<ResponseEntity<List<PostResponse>>> {
        return postUseCase.getPinnedPosts()
            .map { ResponseEntity.ok(it) }
    }
}
