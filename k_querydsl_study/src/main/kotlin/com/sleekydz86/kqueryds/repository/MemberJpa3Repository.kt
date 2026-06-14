package com.sleekydz86.kqueryds.repository

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import com.sleekydz86.kqueryds.dto.MemberSearch3Condition
import com.sleekydz86.kqueryds.dto.MemberTeam3Dto
import com.sleekydz86.kqueryds.dto.QMemberTeam3Dto
import com.sleekydz86.kqueryds.entity.Member
import com.sleekydz86.kqueryds.entity.QMember.member
import com.sleekydz86.kqueryds.entity.QTeam.team
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
class MemberJpa3Repository(
    private val em: EntityManager,
    private val queryFactory: JPAQueryFactory
) {

    fun save(member: Member) {
        em.persist(member)
    }

    fun findById(id: Long): Optional<Member> {
        val findMember = em.find(Member::class.java, id)
        return Optional.ofNullable(findMember)
    }

    fun findAll(): List<Member> {
        return em.createQuery(
            "select m from Member m",
            Member::class.java
        ).resultList
    }

    fun findByUsername(username: String): List<Member> {
        return em.createQuery(
            "select m from Member m where m.username = :username",
            Member::class.java
        )
            .setParameter("username", username)
            .resultList
    }

    fun findAllQuerydsl(): List<Member> {
        return queryFactory
            .selectFrom(member)
            .fetch()
    }

    fun findByUsernameQuerydsl(username: String): List<Member> {
        return queryFactory
            .selectFrom(member)
            .where(member.username.eq(username))
            .fetch()
    }

    //Builder 사용
    //회원명, 팀명, 나이(ageGoe, ageLoe)
    fun searchByBuilder(condition: MemberSearch3Condition): List<MemberTeam3Dto> {
        val builder = BooleanBuilder()

        if (!condition.username.isNullOrBlank()) {
            builder.and(member.username.eq(condition.username))
        }

        if (!condition.teamName.isNullOrBlank()) {
            builder.and(team.name.eq(condition.teamName))
        }

        condition.ageGoe?.let {
            builder.and(member.age.goe(it))
        }

        condition.ageLoe?.let {
            builder.and(member.age.loe(it))
        }

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
            .where(builder)
            .fetch()
    }

    //where절에 파라미터를 사용한 예제
    //회원명, 팀명, 나이(ageGoe, ageLoe)
    fun search(condition: MemberSearch3Condition): List<MemberTeam3Dto> {
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

    //where 절에 파라미터 방식을 사용하면 조건 재사용 가능
    //where 파라미터 방식은 이런식으로 재사용이 가능하다.
    fun findMember(condition: MemberSearch3Condition): List<Member> {
        return queryFactory
            .selectFrom(member)
            .leftJoin(member.team, team)
            .where(
                usernameEq(condition.username),
                teamNameEq(condition.teamName),
                ageGoe(condition.ageGoe),
                ageLoe(condition.ageLoe)
            )
            .fetch()
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