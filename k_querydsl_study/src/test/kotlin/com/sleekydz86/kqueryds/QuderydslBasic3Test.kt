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
import com.querydsl.jpa.JPAExpressions
import com.querydsl.core.types.dsl.CaseBuilder
import com.querydsl.core.types.dsl.Expressions

@SpringBootTest
@Transactional
class QuerydslBasic3Test {

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
    /**
     * subquery추가
     * 나이가 가장 많은 회원 조회
     */
    @Test
    fun subQuery() {
        val member = QMember.member
        val memberSub = QMember("memberSub")

        val result = queryFactory
            .selectFrom(member)
            .where(
                member.age.eq(
                    JPAExpressions
                        .select(memberSub.age.max())
                        .from(memberSub)
                        .where(memberSub.age.gt(10))
                )
            )
            .fetch()

        assertThat(result)
            .extracting("age")
            .containsExactly(40)
    }

    /**

     * case문 추가

     */

    //단순조건
    @Test
    fun basicCase() {
        val result: List<String> = queryFactory
            .select(
                member.age
                    .`when`(10).then("열살")
                    .`when`(20).then("스무살")
                    .otherwise("기타")
            )
            .from(member)
            .orderBy(member.age.asc())
            .fetch()

        result.forEach {
            println(it)
        }

        assertThat(result)
            .containsExactly("열살", "스무살", "기타", "기타")
    }

    //복잡한조건
    @Test
    fun complexCase() {
        val result: List<String> = queryFactory
            .select(
                CaseBuilder()
                    .`when`(member.age.between(0, 20)).then("0~20살")
                    .`when`(member.age.between(21, 30)).then("21~30살")
                    .otherwise("기타")
            )
            .from(member)
            .orderBy(member.age.asc())
            .fetch()

        result.forEach {
            println(it)
        }

        assertThat(result)
            .containsExactly("0~20살", "0~20살", "21~30살", "기타")
    }


    /**

     *상수,문자 더하기

     */

    //상수가 필요하면 Expressions.constant(xxx) 사용
    @Test
    fun constant() {
        val constantA = Expressions.constant("A")

        val result = queryFactory
            .select(
                member.username,
                constantA
            )
            .from(member)
            .orderBy(member.username.asc())
            .fetchFirst()
            ?: throw AssertionError("조회 결과가 없습니다.")

        assertThat(result.get(member.username)).isEqualTo("member1")
        assertThat(result.get(constantA)).isEqualTo("A")
        println("상수가 필요하면 Expressions.constant(xxx) 사용 result= $result")
    }

    //문자더하기
    @Test
    fun concat() {
        val result: String? = queryFactory
            .select(
                member.username
                    .concat("_")
                    .concat(member.age.stringValue())
            )
            .from(member)
            .where(member.username.eq("member1"))
            .fetchOne()

        assertThat(result).isEqualTo("member1_10")
        println("문자더하기 result= $result")
    }

}