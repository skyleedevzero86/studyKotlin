import com.querydsl.jpa.impl.JPAQueryFactory
import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.PathBuilder
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQuery
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils

import com.kposti.domain.post.entity.QPost.post
import com.kposti.domain.member.entity.Member
import com.kposti.domain.post.enums.PostSearchKeywordTypeV1
import com.kposti.domain.post.repository.PostRepositoryCustom
import com.kposti.standard.search.PostSearchKeywordTypeV1

class PostRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : PostRepositoryCustom {

    override fun findByKw(
        kwType: PostSearchKeywordTypeV1?,
        kw: String?,
        author: Member?,
        published: Boolean?,
        listed: Boolean?,
        pageable: Pageable
    ): Page<com.kposti.domain.post.entity.Post> {
        val builder = BooleanBuilder().apply {
            author?.let { and(post.author.eq(it)) }
            published?.let { and(post.published.eq(it)) }
            listed?.let { and(post.listed.eq(it)) }
            if (!kw.isNullOrBlank()) {
                applyKeywordFilter(kwType, kw, this)
            }
        }

        val postsQuery = createPostsQuery(builder).apply {
            applySorting(pageable, this)
            offset(pageable.offset).limit(pageable.pageSize.toLong())
        }

        val totalQuery = createTotalQuery(builder)

        return PageableExecutionUtils.getPage(postsQuery.fetch(), pageable) { totalQuery.fetchOne() ?: 0L }
    }

    private fun applyKeywordFilter(
        kwType: PostSearchKeywordTypeV1?,
        kw: String,
        builder: BooleanBuilder
    ) {
        when (kwType) {
            PostSearchKeywordTypeV1.TITLE -> builder.and(post.title.containsIgnoreCase(kw))
            PostSearchKeywordTypeV1.CONTENT -> builder.and(post.content.containsIgnoreCase(kw))
            PostSearchKeywordTypeV1.AUTHOR -> builder.and(post.author.nickname.containsIgnoreCase(kw))
            else -> builder.and(
                post.title.containsIgnoreCase(kw)
                    .or(post.content.containsIgnoreCase(kw))
                    .or(post.author.nickname.containsIgnoreCase(kw))
            )
        }
    }

    private fun createPostsQuery(builder: BooleanBuilder): JPAQuery<com.kposti.domain.post.entity.Post> =
        jpaQueryFactory.selectFrom(post).where(builder)

    private fun applySorting(pageable: Pageable, query: JPAQuery<com.kposti.domain.post.entity.Post>) {
        pageable.sort.forEach { order ->
            val pathBuilder = PathBuilder(post.type, post.metadata)
            query.orderBy(
                OrderSpecifier(
                    if (order.isAscending) com.querydsl.core.types.Order.ASC else com.querydsl.core.types.Order.DESC,
                    pathBuilder.get(order.property)
                )
            )
        }
    }

    private fun createTotalQuery(builder: BooleanBuilder): JPAQuery<Long> =
        jpaQueryFactory.select(post.count()).from(post).where(builder)
}