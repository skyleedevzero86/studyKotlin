package com.sleekydz86.kqueryds.repository

import com.querydsl.core.QueryResults
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.PathBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import com.sleekydz86.kqueryds.dto.MemberSearch3Condition
import com.sleekydz86.kqueryds.dto.MemberTeam3Dto
import com.sleekydz86.kqueryds.dto.QMemberTeam3Dto
import com.sleekydz86.kqueryds.entity.Member
import com.sleekydz86.kqueryds.entity.QMember.member
import com.sleekydz86.kqueryds.entity.QTeam.team
import jakarta.persistence.EntityManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils

class MemberRepositoryImpl(
    em: EntityManager
) : MemberRepositoryCustom {

    private val queryFactory = JPAQueryFactory(em)

    //회원명, 팀명, 나이(ageGoe, ageLoe)
    override fun search(condition: MemberSearch3Condition): List<MemberTeam3Dto> {
        return queryFactory
            .select(
                QMemberTeam3Dto(
                    member.id,
                    member.username,
                    member.age,
                    team.id,
                    team.name
                )
            )
            .from(member)
            .leftJoin(member.team, team)
            .where(
                usernameEq(condition.username),
                teamNameEq(condition.teamName),
                ageGoe(condition.ageGoe),
                ageLoe(condition.ageLoe)
            )
            .fetch()
    }

    //전체 카운트를 한번에 조회하는 단순한 방법
    //searchPageSimple(), fetchResults() 사용
    @Suppress("DEPRECATION")
    override fun searchPageSimple(
        condition: MemberSearch3Condition,
        pageable: Pageable
    ): Page<MemberTeam3Dto> {
        val results: QueryResults<MemberTeam3Dto> = queryFactory
            .select(
                QMemberTeam3Dto(
                    member.id,
                    member.username,
                    member.age,
                    team.id,
                    team.name
                )
            )
            .from(member)
            .leftJoin(member.team, team)
            .where(
                usernameEq(condition.username),
                teamNameEq(condition.teamName),
                ageGoe(condition.ageGoe),
                ageLoe(condition.ageLoe)
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetchResults()

        val content = results.results
        val total = results.total

        return PageImpl(content, pageable, total)
    }

    /**
     * 복잡한 페이징
     * 데이터 조회 쿼리와, 전체 카운트 쿼리를 분리
     */
    override fun searchPageComplex(
        condition: MemberSearch3Condition,
        pageable: Pageable
    ): Page<MemberTeam3Dto> {
        val content = queryFactory
            .select(
                QMemberTeam3Dto(
                    member.id,
                    member.username,
                    member.age,
                    team.id,
                    team.name
                )
            )
            .from(member)
            .leftJoin(member.team, team)
            .where(
                usernameEq(condition.username),
                teamNameEq(condition.teamName),
                ageGoe(condition.ageGoe),
                ageLoe(condition.ageLoe)
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val countQuery = queryFactory
            .select(member.count())
            .from(member)
            .leftJoin(member.team, team)
            .where(
                usernameEq(condition.username),
                teamNameEq(condition.teamName),
                ageGoe(condition.ageGoe),
                ageLoe(condition.ageLoe)
            )

        return PageableExecutionUtils.getPage(content, pageable) {
            countQuery.fetchOne() ?: 0L
        }
    }

    private fun usernameEq(username: String?): BooleanExpression? {
        return username
            ?.takeIf { it.isNotBlank() }
            ?.let { member.username.eq(it) }
    }

    private fun teamNameEq(teamName: String?): BooleanExpression? {
        return teamName
            ?.takeIf { it.isNotBlank() }
            ?.let { team.name.eq(it) }
    }

    private fun ageGoe(ageGoe: Int?): BooleanExpression? {
        return ageGoe?.let {
            member.age.goe(it)
        }
    }

    private fun ageLoe(ageLoe: Int?): BooleanExpression? {
        return ageLoe?.let {
            member.age.loe(it)
        }
    }

    /**
     * 스프링 데이터 Sort를 Querydsl의 OrderSpecifier로 변환하는 예제
     */
    private fun applySort(pageable: Pageable): List<OrderSpecifier<*>> {
        return pageable.sort.map { order ->
            val direction = if (order.isAscending) Order.ASC else Order.DESC

            when (order.property) {
                "id" -> OrderSpecifier(direction, member.id)
                "username" -> OrderSpecifier(direction, member.username)
                "age" -> OrderSpecifier(direction, member.age)
                else -> throw IllegalArgumentException("지원하지 않는 정렬 필드입니다: ${order.property}")
            }
        }.toList()
    }

    fun findAllWithSort(pageable: Pageable): List<Member> {
        val query = queryFactory
            .selectFrom(member)

        val orderSpecifiers = applySort(pageable)

        if (orderSpecifiers.isNotEmpty()) {
            query.orderBy(*orderSpecifiers.toTypedArray())
        }

        return query.fetch()
    }
}