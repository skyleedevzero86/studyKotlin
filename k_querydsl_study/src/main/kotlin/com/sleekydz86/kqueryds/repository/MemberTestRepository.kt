package com.sleekydz86.kqueryds.repository

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQuery
import com.sleekydz86.kqueryds.dto.MemberSearch4Condition
import com.sleekydz86.kqueryds.entity.Member
import com.sleekydz86.kqueryds.entity.QMember.member
import com.sleekydz86.kqueryds.entity.QTeam.team
import com.sleekydz86.kqueryds.repository.support.Querydsl4RepositorySupport
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository

@Repository
class MemberTestRepository : Querydsl4RepositorySupport(Member::class.java) {

    fun basicSelect(): List<Member> {
        return select(member)
            .from(member)
            .fetch()
    }

    fun basicSelectFrom(): List<Member> {
        return selectFrom(member)
            .fetch()
    }

    @Suppress("DEPRECATION")
    fun searchPageByApplyPage(
        condition: MemberSearch4Condition,
        pageable: Pageable
    ): Page<Member> {
        val query: JPAQuery<Member> = selectFrom(member)
            .leftJoin(member.team, team)
            .where(
                usernameEq(condition.username),
                teamNameEq(condition.teamName),
                ageGoe(condition.ageGoe),
                ageLoe(condition.ageLoe)
            )

        val content = getQuerydsl()
            .applyPagination(pageable, query)
            .fetch()

        return PageableExecutionUtils.getPage(content, pageable) {
            query.fetchCount()
        }
    }

    fun applyPagination(
        condition: MemberSearch4Condition,
        pageable: Pageable
    ): Page<Member> {
        return applyPagination(pageable) { queryFactory ->
            queryFactory
                .selectFrom(member)
                .leftJoin(member.team, team)
                .where(
                    usernameEq(condition.username),
                    teamNameEq(condition.teamName),
                    ageGoe(condition.ageGoe),
                    ageLoe(condition.ageLoe)
                )
        }
    }

    fun applyPagination2(
        condition: MemberSearch4Condition,
        pageable: Pageable
    ): Page<Member> {
        return applyPagination(
            pageable,
            { queryFactory ->
                queryFactory
                    .selectFrom(member)
                    .leftJoin(member.team, team)
                    .where(
                        usernameEq(condition.username),
                        teamNameEq(condition.teamName),
                        ageGoe(condition.ageGoe),
                        ageLoe(condition.ageLoe)
                    )
            },
            { queryFactory ->
                queryFactory
                    .selectFrom(member)
                    .leftJoin(member.team, team)
                    .where(
                        usernameEq(condition.username),
                        teamNameEq(condition.teamName),
                        ageGoe(condition.ageGoe),
                        ageLoe(condition.ageLoe)
                    )
            }
        )
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