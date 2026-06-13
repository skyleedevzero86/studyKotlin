package com.sleekydz86.kqueryds

import com.querydsl.jpa.impl.JPAQueryFactory
import com.sleekydz86.kqueryds.entity.Member
import com.sleekydz86.kqueryds.entity.QMember
import com.sleekydz86.kqueryds.entity.Team
import com.sleekydz86.kqueryds.entity.QTeam
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import com.querydsl.core.Tuple
import com.querydsl.core.types.ExpressionUtils
import com.querydsl.core.types.Projections
import com.querydsl.jpa.JPAExpressions
import com.sleekydz86.kqueryds.dto.MemberDto
import com.sleekydz86.kqueryds.dto.UserDto
import kotlin.test.Test
import com.querydsl.core.BooleanBuilder
import org.assertj.core.api.Assertions.assertThat
import com.querydsl.core.types.dsl.BooleanExpression
import com.sleekydz86.kqueryds.dto.QMemberDto2


@SpringBootTest
@Transactional
class QuerydslBasic5Test {

    @Autowired
    private lateinit var em: EntityManager
    private lateinit var queryFactory: JPAQueryFactory
    private val member = QMember.member
    private val team = QTeam.team

    @BeforeEach
    fun before() {
        queryFactory = JPAQueryFactory(em)

        val teamA = Team("teamA")
        val teamB = Team("teamB")

        em.persist(teamA)
        em.persist(teamB)

        em.persist(Member("member1", 10, teamA))
        em.persist(Member("member2", 20, teamA))
        em.persist(Member("member3", 30, teamB))
        em.persist(Member("member4", 40, teamB))

        em.flush()
        em.clear()
    }



}