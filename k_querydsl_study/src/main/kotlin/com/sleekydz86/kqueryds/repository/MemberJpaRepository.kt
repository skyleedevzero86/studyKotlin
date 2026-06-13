package com.sleekydz86.kqueryds.repository

import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import com.sleekydz86.kqueryds.dto.MemberSearchCondition
import com.sleekydz86.kqueryds.dto.MemberTeamDto
import com.sleekydz86.kqueryds.dto.QMemberTeamDto
import com.sleekydz86.kqueryds.entity.Member
import com.sleekydz86.kqueryds.entity.QMember.member
import com.sleekydz86.kqueryds.entity.QTeam.team
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
class MemberJpaRepository(
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

    //순수 JPA 리포지토리 - Querydsl 추가
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
    fun searchByBuilder(condition: MemberSearchCondition): List<MemberTeamDto> {
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
                QMemberTeamDto(
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
}