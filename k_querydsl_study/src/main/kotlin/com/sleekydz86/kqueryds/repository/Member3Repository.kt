package com.sleekydz86.kqueryds.repository

import com.sleekydz86.kqueryds.entity.Member
import org.springframework.data.jpa.repository.JpaRepository

interface Member3Repository : JpaRepository<Member, Long>, MemberRepositoryCustom {

    fun findByUsername(username: String): List<Member>
}