package com.komroonga.global.config

import com.komroonga.global.utils.QueryFactoryProvider
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JpaConfig(private val entityManager: EntityManager) {

    private val queryFactoryProvider: QueryFactoryProvider = QueryFactoryProvider { em ->
        JPAQueryFactory(em)
    }

    @Bean
    fun jpaQueryFactory(): JPAQueryFactory = queryFactoryProvider.provide(entityManager)
}