package com.komroonga.domain.post.service

import com.komroonga.domain.member.dto.MemberResponse
import com.komroonga.domain.post.dto.PostRequest
import com.komroonga.domain.post.dto.PostResponse
import kotlinx.coroutines.flow.Flow

interface PostService {
    suspend fun count(): Long
    suspend fun create(request: PostRequest): Result<PostResponse>
    suspend fun findById(id: Long): Result<PostResponse>
    suspend fun findAll(): Flow<PostResponse>
    suspend fun edit(postId: Long, memberResponse: MemberResponse, title: String, content: String): Result<PostResponse>
}