package com.sleekydz86.kqueryds.repository.support


import com.querydsl.core.types.EntityPath
import com.querydsl.core.types.Expression
import com.querydsl.core.types.dsl.PathBuilder
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport
import org.springframework.data.jpa.repository.support.Querydsl
import org.springframework.data.querydsl.SimpleEntityPathResolver
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository
import org.springframework.util.Assert

/**
 * Querydsl 4.x 버전에 맞춘 Querydsl 지원 라이브러리
 *
 * @author Younghan Kim
 * @see org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
 */
@Repository
abstract class Querydsl4RepositorySupport(
    private val domainClass: Class<*>
) {

    private lateinit var querydsl: Querydsl
    private lateinit var entityManager: EntityManager
    private lateinit var queryFactory: JPAQueryFactory

    @Autowired
    fun setEntityManager(entityManager: EntityManager) {
        Assert.notNull(entityManager, "EntityManager must not be null!")

        val entityInformation =
            JpaEntityInformationSupport.getEntityInformation(domainClass, entityManager)

        val resolver = SimpleEntityPathResolver.INSTANCE
        val path: EntityPath<*> = resolver.createPath(entityInformation.javaType)

        this.entityManager = entityManager
        this.querydsl = Querydsl(
            entityManager,
            PathBuilder(path.type, path.metadata)
        )
        this.queryFactory = JPAQueryFactory(entityManager)
    }

    @PostConstruct
    fun validate() {
        Assert.notNull(entityManager, "EntityManager must not be null!")
        Assert.notNull(querydsl, "Querydsl must not be null!")
        Assert.notNull(queryFactory, "QueryFactory must not be null!")
    }

    protected fun getQueryFactory(): JPAQueryFactory {
        return queryFactory
    }

    protected fun getQuerydsl(): Querydsl {
        return querydsl
    }

    protected fun getEntityManager(): EntityManager {
        return entityManager
    }

    protected fun <T> select(expr: Expression<T>): JPAQuery<T> {
        return getQueryFactory().select(expr)
    }

    protected fun <T> selectFrom(from: EntityPath<T>): JPAQuery<T> {
        return getQueryFactory().selectFrom(from)
    }

    @Suppress("DEPRECATION")
    protected fun <T> applyPagination(
        pageable: Pageable,
        contentQuery: (JPAQueryFactory) -> JPAQuery<T>
    ): Page<T> {
        val jpaQuery = contentQuery(getQueryFactory())

        val content = getQuerydsl()
            .applyPagination(pageable, jpaQuery)
            .fetch()

        return PageableExecutionUtils.getPage(content, pageable) {
            jpaQuery.fetchCount()
        }
    }

    @Suppress("DEPRECATION")
    protected fun <T> applyPagination(
        pageable: Pageable,
        contentQuery: (JPAQueryFactory) -> JPAQuery<T>,
        countQuery: (JPAQueryFactory) -> JPAQuery<*>
    ): Page<T> {
        val jpaContentQuery = contentQuery(getQueryFactory())

        val content = getQuerydsl()
            .applyPagination(pageable, jpaContentQuery)
            .fetch()

        val countResult = countQuery(getQueryFactory())

        return PageableExecutionUtils.getPage(content, pageable) {
            countResult.fetchCount()
        }
    }
}