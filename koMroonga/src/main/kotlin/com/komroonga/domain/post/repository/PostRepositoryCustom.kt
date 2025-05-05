package com.komroonga.domain.post.repository

import NoticeType
import Post

interface PostRepositoryCustom {
    fun search(searchType: String, keyword: String): List<Post>
    fun findVisiblePosts(userId: Long?): List<Post>
    fun findVisiblePostsByNoticeType(noticeType: NoticeType, userId: Long?): List<Post>
}