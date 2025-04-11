package com.kposti.domain.post.service

import com.kposti.domain.member.entity.Member
import com.kposti.domain.post.entity.Post
import com.kposti.domain.post.repository.PostRepository
import com.kposti.global.https.RespData
import com.kposti.standard.search.PostSearchKeywordTypeV1
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort

@Service
class PostService(private val postRepository: PostRepository) {

    fun count(): Long = postRepository.count()

    fun countByPublished(published: Boolean): Long = postRepository.countByPublished(published)

    fun countByListed(listed: Boolean): Long = postRepository.countByListed(listed)

    fun write(author: Member, title: String, content: String, published: Boolean, listed: Boolean): Post =
        Post().apply {
            this.author = author
            this.title = title
            this.content = content
            this.published = published
            this.listed = listed
        }.let(postRepository::save)

    fun findAllByOrderByIdDesc(): List<Post> = postRepository.findAllByOrderByIdDesc()

    fun findById(id: Long): Optional<Post> = postRepository.findById(id)

    fun delete(post: Post) = postRepository.delete(post)

    fun modify(post: Post, title: String, content: String, published: Boolean, listed: Boolean) {
        val wasTemp = post.isTemp()

        post.title = title
        post.content = content
        post.published = published
        post.listed = listed

        if (wasTemp && !post.isTemp()) post.setCreateDateNow()
    }

    fun flush() = postRepository.flush()

    fun findLatest(): Optional<Post> = postRepository.findFirstByOrderByIdDesc()

    fun findByListedPaged(listed: Boolean, page: Int, pageSize: Int): Page<Post> =
        findByListedPaged(listed, null, null, page, pageSize)

    fun findByListedPaged(
        listed: Boolean,
        searchKeywordType: PostSearchKeywordTypeV1?,
        searchKeyword: String?,
        page: Int,
        pageSize: Int
    ): Page<Post> =
        postRepository.findByKw(searchKeywordType, searchKeyword, null, null, listed,
            PageRequest.of(page - 1, pageSize, Sort.by(Sort.Order.desc("id"))))

    fun findByAuthorPaged(author: Member, page: Int, pageSize: Int): Page<Post> =
        findByAuthorPaged(author, null, null, page, pageSize)

    fun findByAuthorPaged(
        author: Member,
        searchKeywordType: PostSearchKeywordTypeV1?,
        searchKeyword: String?,
        page: Int,
        pageSize: Int
    ): Page<Post> =
        postRepository.findByKw(searchKeywordType, searchKeyword, author, null, null,
            PageRequest.of(page - 1, pageSize, Sort.by(Sort.Order.desc("id"))))

    fun findTempOrMake(author: Member): RespData<Post> {
        var isNew = false

        val post = postRepository.findTop1ByAuthorAndPublishedAndTitleOrderByIdDesc(
            author, false, "임시글"
        ).orElseGet {
            isNew = true
            write(author, "임시글", "", false, false)
        }

        return if (isNew) {
            RespData("201-1", "${post.id}번 임시글이 생성되었습니다.", post)
        } else {
            RespData("200-1", "${post.id}번 임시글을 불러옵니다.", post)
        }
    }
}