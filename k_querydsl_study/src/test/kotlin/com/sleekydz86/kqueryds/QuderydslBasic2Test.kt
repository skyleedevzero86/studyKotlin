package com.sleekydz86.kqueryds

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import com.sleekydz86.kqueryds.entity.Member
import com.sleekydz86.kqueryds.entity.QMember
import com.sleekydz86.kqueryds.entity.Team
import com.sleekydz86.kqueryds.entity.QTeam
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import com.querydsl.core.Tuple

@SpringBootTest
@Transactional
class QuerydslBasic2Test {

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

    //기본조인
    /**
     * 팀 A에 소속된 모든 회원
     *
     */
    @Test
    fun join() {
        val result = queryFactory
            .selectFrom(member)
            //.join(member.team, team)
            .leftJoin(member.team,team)
            .where(team.name.eq("teamA"))
            //.on(team.name.eq("teamA"))
            .fetch()

        assertThat(result)
            .extracting("username")
            .containsExactly("member1", "member2")
    }

    /**
     * 세타조인
     * 회원의 이름이 팀이름과 같은 회원 조회
     */

    @Test
    fun thetaJoin() {
        val dummyTeam = Team("dummyTeam")
        em.persist(dummyTeam)

        em.persist(Member("teamA", 100, dummyTeam))
        em.persist(Member("teamB", 100, dummyTeam))
        em.persist(Member("teamC", 100, dummyTeam))

        em.flush()
        em.clear()

        val result = queryFactory
            .select(member)
            .from(member, team)
            .where(member.username.eq(team.name))
            .fetch()

        assertThat(result)
            .extracting("username")
            .containsExactly("teamA", "teamB")
    }


}