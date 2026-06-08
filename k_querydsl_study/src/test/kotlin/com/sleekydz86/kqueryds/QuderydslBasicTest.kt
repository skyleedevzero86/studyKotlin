package com.sleekydz86.kqueryds

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import com.sleekydz86.kqueryds.entity.Member
import com.sleekydz86.kqueryds.entity.QMember
import com.sleekydz86.kqueryds.entity.Team
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class QuerydslBasicTest {

    @Autowired
    private lateinit var em: EntityManager

    private lateinit var queryFactory: JPAQueryFactory

    @BeforeEach
    fun before() {
        queryFactory = JPAQueryFactory(em)

        val teamA = Team("teamA")
        val teamB = Team("teamB")
        em.persist(teamA)
        em.persist(teamB)

        val member1 = Member("member1", 10, teamA)
        val member2 = Member("member2", 20, teamA)
        val member3 = Member("member3", 30, teamB)
        val member4 = Member("member4", 40, teamB)

        em.persist(member1)
        em.persist(member2)
        em.persist(member3)
        em.persist(member4)

        em.flush()
        em.clear()

        val members = em.createQuery(
            "select m from Member m",
            Member::class.java
        ).resultList

        members.forEach { m ->
            println("member id=${m.id}, username=${m.username}, age=${m.age}, team=${m.team?.name}")
        }
    }

    @Test
    fun startJPQL() {
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
        val m = QMember("m")

        val findMember = queryFactory
            .select(m)
            .from(m)
            .where(m.username.eq("member1"))
            .fetchOne()

        assertThat(findMember?.username).isEqualTo("member1")
    }

    @Test
    fun startQuerydsl2() {
        val member = QMember.member

        val findMember = queryFactory
            .selectFrom(member)
            .where(member.username.eq("member1"))
            .fetchOne()

        assertThat(findMember?.username).isEqualTo("member1")
    }

    @Test
    fun search() {
        val member = QMember.member

        val findMember = queryFactory
            .selectFrom(member)
            .where(
                member.username.eq("member1"),
                member.age.eq(10)
            )
            .fetchOne()
            ?: throw AssertionError("member1을 찾지 못했습니다.")

        assertThat(findMember.username).isEqualTo("member1")
        assertThat(findMember.age).isEqualTo(10)
    }

    @Test
    fun searchParam() {
        val result = searchMember("member1", 10)

        assertThat(result?.username).isEqualTo("member1")
        assertThat(result?.age).isEqualTo(10)
    }

    private fun searchMember(username: String?, age: Int?): Member? {
        val member = QMember.member

        return queryFactory
            .selectFrom(member)
            .where(
                usernameEq(username),
                ageEq(age)
            )
            .fetchOne()
    }

    @Test
    fun search2() {
        val member = QMember.member

        val findMember = queryFactory
            .selectFrom(member)
            .where(
                usernameEq("member1"),
                ageEq(10)
            )
            .fetchOne()
            ?: throw AssertionError("회원 조회 실패")

        assertThat(findMember.username).isEqualTo("member1")
        assertThat(findMember.username).isNotEqualTo("member2")
        assertThat(findMember.age).isEqualTo(10)
    }

    private fun usernameEq(username: String?): BooleanExpression? {
        return username?.takeIf { it.isNotBlank() }
            ?.let { QMember.member.username.eq(it) }
    }

    private fun ageEq(age: Int?): BooleanExpression? {
        return age?.let { QMember.member.age.eq(it) }
    }
    //리스트조회하는 함수
    @Test
    fun resultFetch() {
        val member = QMember.member

        val result = queryFactory
            .selectFrom(member)
            .fetch()

        assertThat(result).hasSize(4)

        result.forEach {
            println("member=${it.username}, age=${it.age}")
        }
    }

    //단건조회
    @Test
    fun resultFetchOne() {
        val member = QMember.member

        val findMember = queryFactory
            .selectFrom(member)
            .where(
                member.username.eq("member1"),
                member.age.eq(10)
            )
            .fetchOne()

        assertThat(findMember?.username).isEqualTo("member1")
        assertThat(findMember?.age).isEqualTo(10)
    }


    //첫번째만 무조건가져오기..
    @Test
    fun resultFetchFirst() {
        val member = QMember.member

        val findMember = queryFactory
            .selectFrom(member)
            .where(member.username.eq("member1"))
            .orderBy(member.age.asc())
            .fetchFirst()

        assertThat(findMember?.username).isEqualTo("member1")
        assertThat(findMember?.age).isEqualTo(10)
    }

    //전체카운트가져오기
    @Suppress("DEPRECATION")
    @Test
    fun resultFetchResults() {
        val member = QMember.member

        val results = queryFactory
            .selectFrom(member)
            .orderBy(member.age.asc())
            .offset(1)
            .limit(2)
            .fetchResults()

        val content = results.results
        val total = results.total
        val offset = results.offset
        val limit = results.limit

        assertThat(content).hasSize(2)
        assertThat(total).isEqualTo(4L)
        assertThat(offset).isEqualTo(1L)
        assertThat(limit).isEqualTo(2L)

        content.forEach {
            println("member=${it.username}, age=${it.age}")
        }
    }

    //조건에맞는갯수가져오기
    @Suppress("DEPRECATION")
    @Test
    fun resultFetchCount() {
        val member = QMember.member

        val count = queryFactory
            .selectFrom(member)
            .where(member.age.gt(10))
            .fetchCount()

        assertThat(count).isEqualTo(3L)
    }

    //분리해서 작성하기

    @Test
    fun pagingQuery() {
        val member = QMember.member

        //페이징
        val content = queryFactory
            .selectFrom(member)
            .orderBy(member.age.asc())
            .offset(0)
            .limit(2)
            .fetch()

        val total = queryFactory
            .select(member.count())
            .from(member)
            .fetchOne() ?: 0L

        assertThat(content).hasSize(2)
        assertThat(total).isEqualTo(4L)
    }

    // 최신방식으로 많이사용 대신 count쿼리 직접작성
    @Test
    fun countQuery() {
        val member = QMember.member

        val count = queryFactory
            .select(member.count())
            .from(member)
            .where(member.age.gt(10))
            .fetchOne() ?: 0L

        assertThat(count).isEqualTo(3L)
    }

}