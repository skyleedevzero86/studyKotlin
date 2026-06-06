package com.sleekydz86.kqueryds

import com.querydsl.jpa.impl.JPAQueryFactory
import com.sleekydz86.kqueryds.entity.Hello
import com.sleekydz86.kqueryds.entity.QHello
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Commit
import org.springframework.test.annotation.Rollback
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
@Commit
class KQuerydslStudyApplicationTests {

    @Autowired
    private lateinit var em: EntityManager

    @Test
    @Rollback(false)
    fun contextLoads() {
        val hello = Hello()
        em.persist(hello)

        val query = JPAQueryFactory(em)

        val qHello = QHello("h")

        val result = query
            .selectFrom(qHello)
            .fetchOne()


        println("결과: $result")
        println("결과 id: ${result?.id}")

        Assertions.assertThat(result).isEqualTo(hello)
        Assertions.assertThat(result?.id).isEqualTo(hello.id)

    }

}
