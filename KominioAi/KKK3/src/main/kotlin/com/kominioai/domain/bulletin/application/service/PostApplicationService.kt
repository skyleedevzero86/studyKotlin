package com.kominioai.domain.bulletin.application.service

import com.kominioai.domain.bulletin.application.dto.*
import com.kominioai.domain.bulletin.application.port.`in`.PostUseCase
import com.kominioai.domain.bulletin.application.port.out.PostPersistencePort
import com.kominioai.domain.bulletin.application.port.out.EventPublisherPort
import com.kominioai.domain.bulletin.domain.model.*
import com.kominioai.domain.bulletin.domain.service.PostService
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class PostApplicationService(
    private val postPersistencePort: PostPersistencePort,
    private val eventPublisherPort: EventPublisherPort,
    private val postService: PostService
) : PostUseCase {

    override fun createPost(request: CreatePostRequest, authorId: String, authorName: String): Mono<PostResponse> {
        if (!postService.validatePostCreation(request.title, request.content, request.category, authorId)) {
            return Mono.error(IllegalArgumentException("게시글 생성 조건을 만족하지 않습니다"))
        }
        
        val post = Post.create(
            title = request.title,
            content = request.content,
            category = request.category,
            authorId = authorId,
            authorName = authorName,
            pinned = postService.shouldPinPost(request.category, request.pinned)
        )
        
        return postPersistencePort.save(post)
            .flatMap { savedPost ->
                val event = PostCreatedEvent(
                    postId = savedPost.id,
                    title = savedPost.title,
                    category = savedPost.category,
                    authorId = savedPost.authorId,
                    authorName = savedPost.authorName,
                    pinned = savedPost.pinned,
                    createdAt = savedPost.createdAt
                )
                
                eventPublisherPort.publishPostCreatedEvent(event)
                    .then(Mono.just(savedPost.toResponse()))
            }
    }

    override fun updatePost(postId: String, request: UpdatePostRequest, userId: String): Mono<PostResponse> {
        return postPersistencePort.findById(PostId(postId))
            .switchIfEmpty(Mono.error(IllegalArgumentException("게시글을 찾을 수 없습니다")))
            .flatMap { post ->
                if (!postService.canUserEditPost(post, userId)) {
                    return@flatMap Mono.error(IllegalArgumentException("게시글 수정 권한이 없습니다"))
                }
                
                if (!postService.validatePostUpdate(request.title, request.content)) {
                    return@flatMap Mono.error(IllegalArgumentException("게시글 수정 조건을 만족하지 않습니다"))
                }
                
                val updatedPost = post.update(
                    title = request.title,
                    content = request.content,
                    category = request.category,
                    pinned = request.pinned
                )
                
                postPersistencePort.save(updatedPost)
                    .flatMap { savedPost ->
                        val event = PostUpdatedEvent(
                            postId = savedPost.id,
                            title = savedPost.title,
                            authorId = savedPost.authorId,
                            updatedAt = savedPost.updatedAt
                        )
                        
                        eventPublisherPort.publishPostUpdatedEvent(event)
                            .then(Mono.just(savedPost.toResponse()))
                    }
            }
    }

    override fun deletePost(postId: String, userId: String, userRole: String): Mono<Void> {
        return postPersistencePort.findById(PostId(postId))
            .switchIfEmpty(Mono.error(IllegalArgumentException("게시글을 찾을 수 없습니다")))
            .flatMap { post ->
                if (!postService.canUserDeletePost(post, userId, userRole)) {
                    return@flatMap Mono.error(IllegalArgumentException("게시글 삭제 권한이 없습니다"))
                }
                
                val event = PostDeletedEvent(
                    postId = post.id,
                    title = post.title,
                    authorId = post.authorId,
                    deletedAt = java.time.LocalDateTime.now()
                )
                
                postPersistencePort.deleteById(post.id)
                    .flatMap { success ->
                        if (success) {
                            eventPublisherPort.publishPostDeletedEvent(event)
                        } else {
                            Mono.error(RuntimeException("게시글 삭제에 실패했습니다"))
                        }
                    }
            }
    }

    override fun getPost(postId: String): Mono<PostResponse> {
        return postPersistencePort.findById(PostId(postId))
            .switchIfEmpty(Mono.error(IllegalArgumentException("게시글을 찾을 수 없습니다")))
            .map { it.toResponse() }
    }

    override fun getPosts(request: GetPostsRequest): Mono<PostListResponse> {
        return postPersistencePort.findAll()
            .filter { post ->
                // 사용자 권한에 따른 필터링
                val hasPermission = when (request.userRole) {
                    "ADMIN" -> true // 관리자는 모든 게시글 볼 수 있음
                    "USER" -> post.category != com.kominioai.domain.bulletin.domain.model.PostCategory.ANNOUNCEMENT // 일반 사용자는 공지사항 제외
                    else -> post.category != com.kominioai.domain.bulletin.domain.model.PostCategory.ANNOUNCEMENT // 기본적으로 공지사항 제외
                }
                
                hasPermission &&
                request.category?.let { post.category == it } ?: true &&
                request.authorId?.let { post.authorId == it } ?: true &&
                request.pinned?.let { post.pinned == it } ?: true &&
                request.searchTerm?.let { 
                    post.title.contains(it, ignoreCase = true) || 
                    post.content.contains(it, ignoreCase = true) 
                } ?: true
            }
            .collectList()
            .flatMap { posts ->
                val totalCount = posts.size.toLong()
                val startIndex = request.page * request.size
                val endIndex = minOf(startIndex + request.size, posts.size)
                val pagedPosts = posts.subList(startIndex, endIndex)
                
                Mono.just(PostListResponse(
                    posts = pagedPosts.map { it.toResponse() },
                    totalCount = totalCount,
                    page = request.page,
                    size = request.size,
                    totalPages = ((totalCount + request.size - 1) / request.size).toInt()
                ))
            }
    }

    override fun incrementViewCount(postId: String): Mono<Void> {
        return postPersistencePort.findById(PostId(postId))
            .switchIfEmpty(Mono.error(IllegalArgumentException("게시글을 찾을 수 없습니다")))
            .flatMap { post ->
                val updatedPost = post.incrementViewCount()
                postPersistencePort.save(updatedPost)
                    .then()
            }
    }

    override fun likePost(postId: String, userId: String): Mono<PostResponse> {
        return postPersistencePort.findById(PostId(postId))
            .switchIfEmpty(Mono.error(IllegalArgumentException("게시글을 찾을 수 없습니다")))
            .flatMap { post ->
                val updatedPost = post.incrementLikeCount()
                postPersistencePort.save(updatedPost)
                    .map { it.toResponse() }
            }
    }

    override fun getPinnedPosts(): Mono<List<PostResponse>> {
        return postPersistencePort.findPinnedPosts()
            .collectList()
            .map { posts -> posts.map { it.toResponse() } }
    }
}

// 확장 함수들
private fun Post.toResponse(): PostResponse {
    return PostResponse(
        id = this.id.value,
        title = this.title,
        content = this.content,
        category = this.category,
        authorId = this.authorId,
        authorName = this.authorName,
        pinned = this.pinned,
        viewCount = this.viewCount,
        likeCount = this.likeCount,
        commentCount = this.commentCount,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
