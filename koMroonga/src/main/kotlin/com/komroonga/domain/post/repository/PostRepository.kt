package com.komroonga.domain.post.repository

import NoticeType
import Post
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

    // 네이티브 SQL 벌크 삽입 메서드 추가
    @Query(
        value = "INSERT INTO post (title, content, author_id, is_private, notice_type, created_at, updated_at) " +
                "VALUES (:title, :content, :authorId, :isPrivate, :noticeType, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
        nativeQuery = true
    )
    fun bulkInsert(
        @Param("title") title: String,
        @Param("content") content: String,
        @Param("authorId") authorId: Long,
        @Param("isPrivate") isPrivate: Boolean,
        @Param("noticeType") noticeType: String
    )
}