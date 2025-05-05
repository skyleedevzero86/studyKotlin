package com.komroonga.domain.post.repository

import com.komroonga.domain.post.entity.NoticeType
import com.komroonga.domain.post.entity.Post

interface PostRepositoryCustom {
    fun search(searchType: String, keyword: String): List<Post>
    fun findVisiblePosts(userId: Long?): List<Post>
    fun findVisiblePostsByNoticeType(noticeType: NoticeType, userId: Long?): List<Post>
}