package com.kominioai.domain.bulletin.adapter.out.persistence

import com.kominioai.domain.bulletin.domain.model.Post
import com.kominioai.domain.bulletin.domain.model.PostId
import com.kominioai.domain.bulletin.domain.model.PostCategory
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("posts")
data class PostEntity(
    @Id
    val id: String,
    val title: String,
    val content: String,
    val category: String,
    val authorId: String,
    val authorName: String,
    val pinned: Boolean,
    val viewCount: Int,
    val likeCount: Int,
    val commentCount: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    fun toDomain(): Post {
        return Post(
            id = PostId(id),
            title = title,
            content = content,
            category = PostCategory.valueOf(category),
            authorId = authorId,
            authorName = authorName,
            pinned = pinned,
            viewCount = viewCount,
            likeCount = likeCount,
            commentCount = commentCount,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    companion object {
        fun fromDomain(post: Post): PostEntity {
            return PostEntity(
                id = post.id.value,
                title = post.title,
                content = post.content,
                category = post.category.name,
                authorId = post.authorId,
                authorName = post.authorName,
                pinned = post.pinned,
                viewCount = post.viewCount,
                likeCount = post.likeCount,
                commentCount = post.commentCount,
                createdAt = post.createdAt,
                updatedAt = post.updatedAt
            )
        }
    }
}
