package com.komroonga.member.service

import com.komroonga.domain.member.dto.MemberRequest
import com.komroonga.domain.member.dto.MemberResponse
import kotlinx.coroutines.flow.Flow

typealias MemberResult<T> = Result<T>

interface MemberService {
    suspend fun register(request: MemberRequest): MemberResult<MemberResponse>
    suspend fun findByUsername(username: String): MemberResult<MemberResponse>
    suspend fun findAll(): Flow<MemberResponse>
    suspend fun count(): MemberResult<Long>
}