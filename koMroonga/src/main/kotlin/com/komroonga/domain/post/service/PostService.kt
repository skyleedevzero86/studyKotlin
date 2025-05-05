package com.komroonga.domain.post.service

import NoticeType
import com.komroonga.domain.member.dto.MemberResponse
import com.komroonga.domain.post.dto.PostRequest
import com.komroonga.domain.post.dto.PostResponse
import kotlinx.coroutines.flow.Flow

interface PostService {
    suspend fun count(): Long
    suspend fun create(request: PostRequest): Result<PostResponse>
    suspend fun findById(id: Long, currentUser: MemberResponse?): Result<PostResponse>
    suspend fun findAll(currentUser: MemberResponse?): Flow<PostResponse>
    suspend fun edit(
        postId: Long, 
        memberResponse: MemberResponse, 
        title: String, 
        content: String, 
        isPrivate: Boolean,
        noticeType: NoticeType
    ): Result<PostResponse>

    /**
     * 검색 조건에 따라 게시글을 검색합니다.
     * @param searchType 검색 유형 (title, content, author, all)
     * @param keyword 검색어
     * @param currentUser 현재 사용자 (null인 경우 비로그인 사용자)
     * @return 검색 결과 게시글 목록
     */
    suspend fun search(
        searchType: String, 
        keyword: String, 
        currentUser: MemberResponse?
    ): Flow<PostResponse>

    /**
     * 공지 유형에 따라 게시글을 필터링합니다.
     * @param noticeType 공지 유형
     * @param currentUser 현재 사용자 (null인 경우 비로그인 사용자)
     * @return 필터링된 게시글 목록
     */
    suspend fun findByNoticeType(
        noticeType: NoticeType, 
        currentUser: MemberResponse?
    ): Flow<PostResponse>
}
