package com.kominioai.domain.bulletin.application.port.`in`

import com.kominioai.domain.bulletin.application.dto.*
import reactor.core.publisher.Mono

interface PostUseCase {
    fun createPost(request: CreatePostRequest, authorId: String, authorName: String): Mono<PostResponse>
    fun updatePost(postId: String, request: UpdatePostRequest, userId: String): Mono<PostResponse>
    fun deletePost(postId: String, userId: String, userRole: String): Mono<Void>
    fun getPost(postId: String): Mono<PostResponse>
    fun getPosts(request: GetPostsRequest): Mono<PostListResponse>
    fun incrementViewCount(postId: String): Mono<Void>
    fun likePost(postId: String, userId: String): Mono<PostResponse>
    fun getPinnedPosts(): Mono<List<PostResponse>>
}
