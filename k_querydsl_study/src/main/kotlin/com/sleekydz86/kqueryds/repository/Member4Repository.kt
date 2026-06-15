package com.sleekydz86.kqueryds.repository

import com.sleekydz86.kqueryds.entity.Member

class Member4Repository {
    JpaRepository<Member, Long>,
    MemberRepositoryCustom,
    QuerydslPredicateExecutor<Member> {

        fun findByUsername(username: String): List<Member>
}