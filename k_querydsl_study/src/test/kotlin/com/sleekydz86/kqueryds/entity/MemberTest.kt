package com.sleekydz86.kqueryds.entity

import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Commit
import org.springframework.test.annotation.Rollback
import org.springframework.transaction.annotation.Transactional
import kotlin.test.Test

@SpringBootTest
@Transactional
@Commit
class MemberTest {

    @Autowired
    private lateinit var em: EntityManager

    @Test
    @Rollback(false)
    fun testEntity(){

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
}