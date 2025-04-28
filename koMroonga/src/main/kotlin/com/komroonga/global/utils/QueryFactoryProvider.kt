package com.komroonga.global.utils

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager


fun interface QueryFactoryProvider {
    fun provide(entityManager: EntityManager): JPAQueryFactory
}

