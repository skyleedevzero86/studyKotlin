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
class QuerydslBasic4Test {

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

    //프로젝션 대상이 하나
    @Test
    fun simpleProjection() {
        val result: List<String?> = queryFactory
            .select(member.username)
            .from(member)
            .fetch()

        result.forEach {
            println("username=$it")
        }
    }

    //튜플조회-프로젝션대상이 둘이상일때 사용
    @Test
    fun tupleProjection() {
        val result: List<Tuple> = queryFactory
            .select(member.username, member.age)
            .from(member)
            .fetch()

        result.forEach { tuple ->
            val username = tuple.get(member.username)
            val age = tuple.get(member.age)

            println("username=$username")
            println("age=$age")
        }
    }

    //순수 JPA에서 DTO 조회 코드
    @Test
    fun findDtoByJPQL() {
        val result: List<MemberDto> = em.createQuery(
            """
        select new com.sleekydz86.kqueryds.dto.MemberDto(m.username, m.age)
        from Member m
        """.trimIndent(),
            MemberDto::class.java
        ).resultList

        result.forEach {
            println(it)
        }
    }

    //Querydsl 빈 생성(Bean population)
//프로퍼티 접근 - Setter
    @Test
    fun findDtoBySetter() {
        val result: List<MemberDto> = queryFactory
            .select(
                Projections.bean(
                    MemberDto::class.java,
                    member.username,
                    member.age
                )
            )
            .from(member)
            .fetch()

        result.forEach {
            println(it)
        }
    }

    //필드 직접 접근
    @Test
    fun findDtoByField() {
        val result: List<MemberDto> = queryFactory
            .select(
                Projections.fields(
                    MemberDto::class.java,
                    member.username,
                    member.age
                )
            )
            .from(member)
            .fetch()

        result.forEach {
            println(it)
        }
    }

    //별칭이 다를 때
    @Test
    fun findUserDto() {
        val memberSub = QMember("memberSub")

        val result: List<UserDto> = queryFactory
            .select(
                Projections.fields(
                    UserDto::class.java,
                    member.username.`as`("name"),
                    ExpressionUtils.`as`(
                        JPAExpressions
                            .select(memberSub.age.max())
                            .from(memberSub),
                        "age"
                    )
                )
            )
            .from(member)
            .fetch()

        result.forEach {
            println(it)
        }
    }


    //생성자사용
    @Test
    fun findDtoByConstructor() {
        val result: List<MemberDto> = queryFactory
            .select(
                Projections.constructor(
                    MemberDto::class.java,
                    member.username,
                    member.age
                )
            )
            .from(member)
            .fetch()

        result.forEach {
            println(it)
        }
    }

    //@QueryProjection 활용
    @Test
    fun queryProjection() {
        val result = queryFactory
            .select(QMemberDto2(member.username, member.age))
            .from(member)
            .fetch()

        result.forEach {
            println(it)
        }

        assertThat(result).hasSize(4)
    }

    //distinct
    @Test
    fun distinct() {
        val result: List<String?> = queryFactory
            .select(member.username)
            .distinct()
            .from(member)
            .fetch()

        result.forEach {
            println("username=$it")
        }
    }

    //동적 쿼리 - BooleanBuilder 사용
    @Test
    fun 동적쿼리_BooleanBuilder() {
        val usernameParam = "member1"
        val ageParam = 10

        val result = searchMember1(usernameParam, ageParam)

        assertThat(result.size).isEqualTo(1)
    }

    private fun searchMember1(usernameCond: String?, ageCond: Int?): List<Member> {
        val builder = BooleanBuilder()

        if (usernameCond != null) {
            builder.and(member.username.eq(usernameCond))
        }

        if (ageCond != null) {
            builder.and(member.age.eq(ageCond))
        }

        return queryFactory
            .selectFrom(member)
            .where(builder)
            .fetch()
    }

    private fun searchMember1_2(usernameCond: String?, ageCond: Int?): List<Member> {
        val builder = BooleanBuilder()

        usernameCond?.let {
            builder.and(member.username.eq(it))
        }

        ageCond?.let {
            builder.and(member.age.eq(it))
        }

        return queryFactory
            .selectFrom(member)
            .where(builder)
            .fetch()
    }

    //동적 쿼리 - Where 다중 파라미터 사용
    @Test
    fun 동적쿼리_WhereParam() {
        val usernameParam = "member1"
        val ageParam = 10

        val result = searchMember2(usernameParam, ageParam)

        assertThat(result.size).isEqualTo(1)
    }

    private fun searchMember2(usernameCond: String?, ageCond: Int?): List<Member> {
        return queryFactory
            .selectFrom(member)
            .where(
                usernameEq(usernameCond),
                ageEq(ageCond)
            )
            .fetch()
    }

    private fun usernameEq(usernameCond: String?): BooleanExpression? {
        return usernameCond?.let {
            member.username.eq(it)
        }
    }

    private fun ageEq(ageCond: Int?): BooleanExpression? {
        return ageCond?.let {
            member.age.eq(it)
        }
    }

    //수정, 삭제 벌크 연산
//쿼리한번으로 대량 데이터수정
    @Test
    fun bulkUpdate() {
        val count = queryFactory
            .update(member)
            .set(member.username, "비회원")
            .where(member.age.lt(28))
            .execute()

        em.flush()
        em.clear()

        assertThat(count).isEqualTo(2L)
    }

}