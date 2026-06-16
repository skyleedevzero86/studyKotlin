package com.sleekydz86.kqueryds.repository

import com.querydsl.core.types.dsl.Wildcard
import com.querydsl.jpa.impl.JPAQueryFactory
import com.sleekydz86.kqueryds.dto.MemberSearch3Condition
import com.sleekydz86.kqueryds.dto.MemberTeam3Dto
import com.sleekydz86.kqueryds.dto.QMemberTeam3Dto
import com.sleekydz86.kqueryds.entity.QMember.member
import com.sleekydz86.kqueryds.entity.QTeam.team
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository

@Repository
class MemberPagingExampleRepository(
    private val queryFactory: JPAQueryFactory
) {

    /**
     * count(member.id) 예제
     */
    fun countByMemberId(): Long {
        return queryFactory
            .select(member.count())
            .from(member)
            .fetchOne() ?: 0L
    }

    /**
     * count(*) 예제
     */
    fun countByWildcard(): Long {
        return queryFactory
            .select(Wildcard.count)
            .from(member)
            .fetchOne() ?: 0L
    }

    /**
     * fetchResults(), fetchCount()를 사용하지 않는 최신 방식 페이징 예제
     */
    fun searchPageWithoutFetchCount(
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
}