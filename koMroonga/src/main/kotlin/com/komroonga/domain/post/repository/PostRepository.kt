package com.komroonga.domain.post.repository

import com.komroonga.domain.post.entity.NoticeType
import com.komroonga.domain.post.entity.Post
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface PostRepository : JpaRepository<Post, Long>, PostRepositoryCustom {
    override fun findById(id: Long): Optional<Post>
    override fun findAll(): List<Post>

    fun findByNoticeType(noticeType: NoticeType): List<Post>
    fun findByIsPrivate(isPrivate: Boolean): List<Post>

    @Query("SELECT p FROM Post p WHERE p.noticeType = :noticeType AND (p.isPrivate = false OR p.author.id = :userId)")
    fun findVisiblePostsByNoticeTypeAndUserId(
        @Param("noticeType") noticeType: NoticeType,
        @Param("userId") userId: Long
    ): List<Post>

    @Query("SELECT p FROM Post p WHERE p.noticeType = :noticeType AND p.isPrivate = false")
    fun findPublicPostsByNoticeType(@Param("noticeType") noticeType: NoticeType): List<Post>
}
