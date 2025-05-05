package com.komroonga.domain.post.repository

import com.komroonga.domain.post.entity.NoticeType
import com.komroonga.domain.post.entity.Post
import com.komroonga.member.entity.Role
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Repository

@Repository
class PostRepositoryImpl : PostRepositoryCustom {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun search(searchType: String, keyword: String): List<Post> {
        require(searchType.isNotBlank()) { "searchType은 비어 있을 수 없습니다" }
        require(keyword.isNotBlank()) { "keyword는 비어 있을 수 없습니다" }

        val jpql = StringBuilder("SELECT p FROM Post p JOIN FETCH p.author WHERE ")

        when (searchType.toLowerCase()) {
            "title" -> jpql.append("LOWER(p.title) LIKE LOWER(:keyword)")
            "content" -> jpql.append("LOWER(p.content) LIKE LOWER(:keyword)")
            "author" -> jpql.append("LOWER(p.author.username) LIKE LOWER(:keyword)")
            else -> jpql.append("(LOWER(p.title) LIKE LOWER(:keyword) OR LOWER(p.content) LIKE LOWER(:keyword) OR LOWER(p.author.username) LIKE LOWER(:keyword))")
        }

        return entityManager.createQuery(jpql.toString(), Post::class.java)
            .setParameter("keyword", "%$keyword%")
            .resultList
    }

    override fun findVisiblePosts(userId: Long?): List<Post> {
        val jpql = StringBuilder("SELECT p FROM Post p JOIN FETCH p.author WHERE ")

        if (userId == null) {
            // 비로그인 사용자는 공개 게시글과 전체 공지만 볼 수 있음
            jpql.append("(p.isPrivate = false AND (p.noticeType = :noneType OR p.noticeType = :allType))")

            return entityManager.createQuery(jpql.toString(), Post::class.java)
                .setParameter("noneType", NoticeType.NONE)
                .setParameter("allType", NoticeType.ALL)
                .resultList
        } else {
            // 로그인 사용자는 자신의 비공개 게시글, 공개 게시글, 모든 공지를 볼 수 있음
            jpql.append("(p.isPrivate = false OR p.author.id = :userId OR p.author.role = :adminRole)")

            return entityManager.createQuery(jpql.toString(), Post::class.java)
                .setParameter("userId", userId)
                .setParameter("adminRole", Role.ROLE_ADMIN)
                .resultList
        }
    }

    override fun findVisiblePostsByNoticeType(noticeType: NoticeType, userId: Long?): List<Post> {
        // 회원 공지는 로그인 사용자만 볼 수 있음
        if (userId == null && noticeType == NoticeType.MEMBER) {
            return emptyList()
        }

        val jpql = StringBuilder("SELECT p FROM Post p JOIN FETCH p.author WHERE p.noticeType = :noticeType AND ")

        if (userId == null) {
            // 비로그인 사용자는 공개 게시글만 볼 수 있음
            jpql.append("p.isPrivate = false")

            return entityManager.createQuery(jpql.toString(), Post::class.java)
                .setParameter("noticeType", noticeType)
                .resultList
        } else {
            // 로그인 사용자는 자신의 비공개 게시글, 공개 게시글, 관리자 게시글을 볼 수 있음
            jpql.append("(p.isPrivate = false OR p.author.id = :userId OR p.author.role = :adminRole)")

            return entityManager.createQuery(jpql.toString(), Post::class.java)
                .setParameter("noticeType", noticeType)
                .setParameter("userId", userId)
                .setParameter("adminRole", Role.ROLE_ADMIN)
                .resultList
        }
    }
}