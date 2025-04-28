package com.komroonga.domain.post.repository

import com.komroonga.domain.post.entity.Post
import org.springframework.data.jpa.repository.JpaRepository

interface PostRepository : JpaRepository<Post, Long> {
    suspend fun findById(id: Long): Post?
    suspend fun findAll(): kotlinx.coroutines.flow.Flow<Post>
}