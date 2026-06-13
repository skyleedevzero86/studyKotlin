package com.sleekydz86.kqueryds.config

import com.sleekydz86.kqueryds.entity.Member
import com.sleekydz86.kqueryds.entity.Team
import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Profile("local")
@Component
class InitMember(
    private val initMemberService: InitMemberService
) {

    @PostConstruct
    fun init() {
        initMemberService.init()
    }

    @Component
    class InitMemberService(
        private val em: EntityManager
    ) {

        @Transactional
        fun init() {
            val teamA = Team("teamA")
            val teamB = Team("teamB")

            em.persist(teamA)
            em.persist(teamB)

            for (i in 0 until 100) {
                val selectedTeam = if (i % 2 == 0) teamA else teamB
                em.persist(Member("member$i", i, selectedTeam))
            }
        }
    }
}