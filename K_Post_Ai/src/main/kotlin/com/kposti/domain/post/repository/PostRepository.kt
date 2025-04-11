package com.kposti.domain.post.repository

import com.kposti.domain.member.entity.Member
import com.kposti.domain.post.entity.Post
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface PostRepository : JpaRepository<Post, Long>, PostRepositoryCustom {
    fun findAllByOrderByIdDesc(): List<Post>
    fun findFirstByOrderByIdDesc(): Optional<Post>
    fun findTop1ByAuthorAndPublishedAndTitleOrderByIdDesc(author: Member, published: Boolean, title: String): Optional<Post>
    fun countByPublished(published: Boolean): Long
    fun countByListed(listed: Boolean): Long
}
