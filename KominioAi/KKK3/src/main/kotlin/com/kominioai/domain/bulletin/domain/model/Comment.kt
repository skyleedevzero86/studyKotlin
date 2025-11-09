package com.kominioai.domain.bulletin.domain.model

import java.time.LocalDateTime

data class Comment(
    val id: CommentId,
    val postId: PostId,
    val parentId: CommentId?,
    val content: String,
    val authorId: String,
    val authorName: String,
    val likeCount: Int,
    val replyCount: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun create(
            postId: PostId,
            parentId: CommentId?,
            content: String,
            authorId: String,
            authorName: String
        ): Comment {
            val now = LocalDateTime.now()
            return Comment(
                id = CommentId.generate(),
                postId = postId,
                parentId = parentId,
                content = content,
                authorId = authorId,
                authorName = authorName,
                likeCount = 0,
                replyCount = 0,
                createdAt = now,
                updatedAt = now
            )
        }
    }
    
    fun update(content: String): Comment {
        return copy(
            content = content,
            updatedAt = LocalDateTime.now()
        )
    }
    
    fun incrementLikeCount(): Comment {
        return copy(likeCount = likeCount + 1)
    }
    
    fun incrementReplyCount(): Comment {
        return copy(replyCount = replyCount + 1)
    }
    
    fun decrementReplyCount(): Comment {
        return copy(replyCount = maxOf(0, replyCount - 1))
    }
    
    fun isReply(): Boolean {
        return parentId != null
    }
    
    fun getDepth(): Int {
        // 실제 구현에서는 데이터베이스에서 계층 구조를 계산해야 함
        return if (parentId != null) 1 else 0
    }
}
