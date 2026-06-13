package com.sleekydz86.kqueryds.repository

import com.sleekydz86.kqueryds.dto.MemberSearchCondition
import com.sleekydz86.kqueryds.entity.Member
import com.sleekydz86.kqueryds.entity.Team
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    private lateinit var em: EntityManager

    @Autowired
    private lateinit var memberJpaRepository: MemberJpaRepository

    @Test
    fun basicTest() {
        val team = Team("teamA")
        em.persist(team)

        val member = Member("member1", 10, team)

        memberJpaRepository.save(member)

        val findMember = memberJpaRepository.findById(member.id!!).get()

        assertThat(findMember).isEqualTo(member)

        val result1 = memberJpaRepository.findAll()
        assertThat(result1).containsExactly(member)

        val result2 = memberJpaRepository.findByUsername("member1")
        assertThat(result2).containsExactly(member)
    }

    //Querydsl 테스트 추가
    @Test
    fun basicQuerydslTest() {
        val team = Team("teamA")
        em.persist(team)

        val member = Member("member1", 10, team)

        memberJpaRepository.save(member)

        val findMember = memberJpaRepository.findById(member.id!!).get()

        assertThat(findMember).isEqualTo(member)

        val result1 = memberJpaRepository.findAllQuerydsl()
        assertThat(result1).containsExactly(member)

        val result2 = memberJpaRepository.findByUsernameQuerydsl("member1")
        assertThat(result2).containsExactly(member)
    }

    //조회예제테스트
    @Test
    fun searchTest() {
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

        val condition = MemberSearchCondition().apply {
            ageGoe = 35
            ageLoe = 40
            teamName = "teamB"
        }

        val result = memberJpaRepository.searchByBuilder(condition)

        assertThat(result)
            .extracting("username")
            .containsExactly("member4")
    }
}