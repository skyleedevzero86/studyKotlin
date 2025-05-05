package com.komroonga.member.service

import com.komroonga.domain.member.dto.MemberRequest
import com.komroonga.domain.member.dto.MemberResponse
import com.komroonga.member.entity.Member
import kotlinx.coroutines.flow.Flow

/**
 * 회원 서비스 인터페이스
 * 회원 관련 비즈니스 로직 정의
 */
typealias MemberResult<T> = Result<T>

interface MemberService {
    suspend fun register(request: MemberRequest): MemberResult<MemberResponse>
    suspend fun findByUsername(username: String): MemberResult<MemberResponse>
    suspend fun findAll(): Flow<MemberResponse>
    suspend fun count(): MemberResult<Long>
    suspend fun findMemberEntityByUsername(username: String): Result<Member>
    suspend fun findMemberEntityById(id: Long): Result<Member>
    suspend fun searchByKeyword(keyword: String): MemberResult<List<MemberResponse>>
    suspend fun registerBatch(requests: List<MemberRequest>): List<MemberResponse>
    // 단일 쿼리로 여러 ID 조회 추가
    suspend fun findAllByIds(ids: List<Long>): List<Member>
}