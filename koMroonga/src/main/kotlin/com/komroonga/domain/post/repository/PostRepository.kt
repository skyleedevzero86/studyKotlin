package com.komroonga.domain.post.repository

import com.komroonga.domain.post.entity.Post
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface PostRepository : JpaRepository<Post, Long> {
    override fun findById(id: Long): Optional<Post>
    override fun findAll(): List<Post>
}
