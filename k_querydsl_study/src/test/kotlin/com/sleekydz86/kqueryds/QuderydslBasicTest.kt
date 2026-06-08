package com.sleekydz86.kqueryds

import com.sleekydz86.kqueryds.entity.Member
import com.sleekydz86.kqueryds.entity.Team
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import com.querydsl.jpa.impl.JPAQueryFactory
import com.sleekydz86.kqueryds.entity.QMember
import com.sleekydz86.kqueryds.entity.QMember.*

@SpringBootTest
@Transactional
class QuerydslBasicTest {

    @Autowired
    private lateinit var em: EntityManager

    @BeforeEach
    fun before() {
        val teamA = Team("teamA")
        val teamB = Team("teamB")
        em.persist(teamA)
        em.persist(teamB)

        val member1=Member("member1",10,teamA)
        val member2=Member("member1",20,teamA)

        val member3=Member("member2",30,teamA)
        val member4=Member("member2",40,teamA)

        em.persist(member1)
        em.persist(member2)
        em.persist(member3)
        em.persist(member4)


        //초기화
        em.flush()
        em.clear()

        //확인
        val members = em.createQuery(
            "select m from Member m",
            Member::class.java
        ).resultList

        members.forEach { m ->
            println("member id=${m.id}, username=${m.username}, age=${m.age}, team=${m.team?.name}")
            println("member.team=${m.team?.name}")
        }
    }

    @Test
    fun startJPQL() {
        // member1을 찾아라.
        val findMember = em.createQuery(
            "select m from Member m where m.username = :username",
            Member::class.java
        )
            .setParameter("username", "member1")
            .singleResult

        assertThat(findMember.username).isEqualTo("member1")
    }

    @Test
    fun startQuerydsl() {
        val queryFactory = JPAQueryFactory(em)

        val m = QMember("m") //같은테이블이 아닌경우 이방식사용

        val findMember = queryFactory
            .select(m)
            .from(m)
            .where(m.username.eq("member1")) //바인딩처리 파라미터
            .fetchOne()

        assertThat(findMember?.username).isEqualTo("member1")
    }

    @Test
    fun startQuerydsl2() {
        val queryFactory = JPAQueryFactory(em)

        val findMember = queryFactory
            .select(member) //일방적으로 이렇게하기
            .from(member)
            .where(member.username.eq("member1")) //바인딩처리 파라미터
            .fetchOne()

        assertThat(findMember?.username).isEqualTo("member1")
    }
}