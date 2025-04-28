package com.komroonga.domain.post.service

import com.komroonga.domain.post.dto.PostRequest
import com.komroonga.domain.post.dto.PostResponse
import kotlinx.coroutines.flow.Flow

typealias PostResult<T> = Result<T>

fun interface PostService {
    suspend fun create(request: PostRequest): PostResult<PostResponse>
    suspend fun findById(id: Long): PostResult<PostResponse>
    suspend fun findAll(): Flow<PostResponse>
}