package com.kposti.domain.member.repository

import com.kposti.domain.member.entity.Member
import com.kposti.standard.search.MemberSearchKeywordTypeV1
import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.PathBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository
import com.kposti.domain.member.entity.QMember.member

@Repository
class MemberRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : MemberRepositoryCustom {
    override fun findByKw(kwType: MemberSearchKeywordTypeV1?, kw: String?, pageable: Pageable): Page<Member> {
        val builder = BooleanBuilder()

        if (!kw.isNullOrBlank()) {
            when (kwType) {
                MemberSearchKeywordTypeV1.username -> builder.and(member.username.containsIgnoreCase(kw))
                MemberSearchKeywordTypeV1.nickname -> builder.and(member.nickname.containsIgnoreCase(kw))
                else -> builder.and(
                    member.username.containsIgnoreCase(kw).or(member.nickname.containsIgnoreCase(kw))
                )
            }
        }

        val membersQuery = jpaQueryFactory
            .select(member)
            .from(member)
            .where(builder)

        pageable.sort.forEach { order ->
            val pathBuilder = PathBuilder(Member::class.java, "member")
            membersQuery.orderBy(
                OrderSpecifier(
                    if (order.isAscending) Order.ASC else Order.DESC,
                    pathBuilder.getComparable(order.property, Comparable::class.java)
                )
            )
        }

        membersQuery.offset(pageable.offset).limit(pageable.pageSize.toLong())

        val totalQuery = jpaQueryFactory
            .select(member.count())
            .from(member)
            .where(builder)

        return PageableExecutionUtils.getPage(membersQuery.fetch(), pageable) { totalQuery.fetchOne() ?: 0L }
    }
}