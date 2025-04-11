package com.kposti.domain.member.repository

import com.kposti.domain.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface MemberRepository : JpaRepository<Member, Long>, MemberRepositoryCustom {
    fun findByUsername(username: String): Optional<Member>

    fun findByApiKey(apiKey: String): Optional<Member>
}
