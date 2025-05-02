package com.komroonga.member.repository

import com.komroonga.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface MemberRepository : JpaRepository<Member, Long> {
    fun findByUsername(username: String): Member?

    @Query("SELECT m FROM Member m WHERE m.username LIKE %:keyword% OR m.email LIKE %:keyword% OR m.name LIKE %:keyword%")
    fun searchByKeyword(keyword: String): List<Member>
}
