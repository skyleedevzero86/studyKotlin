package com.kominioai.domain.bulletin.domain.model

import java.time.LocalDateTime

data class Post(
    val id: PostId,
    val title: String,
    val content: String,
    val category: PostCategory,
    val authorId: String,
    val authorName: String,
    val pinned: Boolean,
    val viewCount: Int,
    val likeCount: Int,
    val commentCount: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun create(
            title: String,
            content: String,
            category: PostCategory,
            authorId: String,
            authorName: String,
            pinned: Boolean = false
        ): Post {
            val now = LocalDateTime.now()
            return Post(
                id = PostId.generate(),
                title = title,
                content = content,
                category = category,
                authorId = authorId,
                authorName = authorName,
                pinned = pinned,
                viewCount = 0,
                likeCount = 0,
                commentCount = 0,
                createdAt = now,
                updatedAt = now
            )
        }
    }
    
    fun update(
        title: String? = null,
        content: String? = null,
        category: PostCategory? = null,
        pinned: Boolean? = null
    ): Post {
        return copy(
            title = title ?: this.title,
            content = content ?: this.content,
            category = category ?: this.category,
            pinned = pinned ?: this.pinned,
            updatedAt = LocalDateTime.now()
        )
    }
    
    fun incrementViewCount(): Post {
        return copy(viewCount = viewCount + 1)
    }
    
    fun incrementLikeCount(): Post {
        return copy(likeCount = likeCount + 1)
    }
    
    fun incrementCommentCount(): Post {
        return copy(commentCount = commentCount + 1)
    }
    
    fun decrementCommentCount(): Post {
        return copy(commentCount = maxOf(0, commentCount - 1))
    }
}
