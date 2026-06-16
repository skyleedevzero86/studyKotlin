package com.sleekydz86.kqueryds.repository

import com.sleekydz86.kqueryds.dto.MemberSearch3Condition
import com.sleekydz86.kqueryds.entity.Member
import com.sleekydz86.kqueryds.entity.Team
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class MemberPagingExampleRepositoryTest {

    @Autowired
    private lateinit var em: EntityManager

    @Autowired
    private lateinit var memberPagingExampleRepository: MemberPagingExampleRepository

    @BeforeEach
    fun before() {
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

    @Test
    fun countByMemberId() {
        val totalCount = memberPagingExampleRepository.countByMemberId()

        println("totalCount = $totalCount")

        assertThat(totalCount).isEqualTo(4L)
    }

    @Test
    fun countByWildcard() {
        val totalCount = memberPagingExampleRepository.countByWildcard()

        println("totalCount = $totalCount")

        assertThat(totalCount).isEqualTo(4L)
    }

    @Test
    fun searchPageWithoutFetchCount() {
        val condition = MemberSearch3Condition().apply {
            ageGoe = 10
            ageLoe = 40
        }

        val pageable = PageRequest.of(0, 2)

        val page = memberPagingExampleRepository.searchPageWithoutFetchCount(
            condition,
            pageable
        )

        page.content.forEach {
            println("member=${it.username}, age=${it.age}, team=${it.teamName}")
        }

        assertThat(page.content).hasSize(2)
        assertThat(page.totalElements).isEqualTo(4L)
        assertThat(page.totalPages).isEqualTo(2)
    }
}