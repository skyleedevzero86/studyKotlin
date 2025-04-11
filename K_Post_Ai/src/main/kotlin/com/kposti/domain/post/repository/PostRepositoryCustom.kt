package com.kposti.domain.post.repository

import com.kposti.domain.member.entity.Member
import com.kposti.domain.post.entity.Post
import com.kposti.standard.search.PostSearchKeywordTypeV1
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface PostRepositoryCustom {
    fun findByKw(
        kwType: PostSearchKeywordTypeV1?,
        kw: String?,
        author: Member?,
        published: Boolean?,
        listed: Boolean?,
        pageable: Pageable
    ): Page<Post>
}