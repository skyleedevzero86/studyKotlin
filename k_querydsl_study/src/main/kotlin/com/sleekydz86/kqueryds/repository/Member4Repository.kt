package com.sleekydz86.kqueryds.repository

import com.sleekydz86.kqueryds.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor

interface Member4Repository :
    JpaRepository<Member, Long>,
    MemberRepositoryCustom,
    QuerydslPredicateExecutor<Member> {

    fun findByUsername(username: String): List<Member>
}