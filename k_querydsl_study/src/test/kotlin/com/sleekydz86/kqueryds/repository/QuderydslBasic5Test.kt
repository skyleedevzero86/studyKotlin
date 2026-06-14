package com.sleekydz86.kqueryds.repository

import com.sleekydz86.kqueryds.entity.Member
import com.sleekydz86.kqueryds.entity.Team
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import com.sleekydz86.kqueryds.dto.MemberSearch3Condition
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


@SpringBootTest
@Transactional
class QuerydslBasic5Test {
    @Autowired
    private lateinit var em: EntityManager

    @Autowired
    private lateinit var memberRepository: Member3Repository

    @Test
    fun basicTest() {
        val team = Team("teamA")
        em.persist(team)

        val member = Member("member1", 10, team)

        memberRepository.save(member)

        val findMember = memberRepository.findById(member.id!!).get()

        assertThat(findMember).isEqualTo(member)

        val result1 = memberRepository.findAll()
        assertThat(result1).containsExactly(member)

        val result2 = memberRepository.findByUsername("member1")
        assertThat(result2).containsExactly(member)
    }

    //커스텀 리포지토리 동작 테스트 추가
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

        val condition = MemberSearch3Condition().apply {
            ageGoe = 35
            ageLoe = 40
            teamName = "teamB"
        }

        val result = memberRepository.search(condition)

        assertThat(result)
            .extracting("username")
            .containsExactly("member4")
    }

}