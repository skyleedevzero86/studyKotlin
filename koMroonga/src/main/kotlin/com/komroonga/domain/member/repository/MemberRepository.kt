package com.komroonga.member.repository

import com.komroonga.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository : JpaRepository<Member, Long> {
    fun findByUsername(username: String): Member?
}