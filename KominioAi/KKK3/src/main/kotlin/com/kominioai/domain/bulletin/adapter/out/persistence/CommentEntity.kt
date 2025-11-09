package com.kominioai.domain.bulletin.adapter.out.persistence

import com.kominioai.domain.bulletin.domain.model.Comment
import com.kominioai.domain.bulletin.domain.model.CommentId
import com.kominioai.domain.bulletin.domain.model.PostId
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("comments")
data class CommentEntity(
    @Id
    val id: String,
    val postId: String,
    val parentId: String?,
    val content: String,
    val authorId: String,
    val authorName: String,
    val likeCount: Int,
    val replyCount: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    fun toDomain(): Comment {
        return Comment(
            id = CommentId(id),
            postId = PostId(postId),
            parentId = parentId?.let { CommentId(it) },
            content = content,
            authorId = authorId,
            authorName = authorName,
            likeCount = likeCount,
            replyCount = replyCount,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    companion object {
        fun fromDomain(comment: Comment): CommentEntity {
            return CommentEntity(
                id = comment.id.value,
                postId = comment.postId.value,
                parentId = comment.parentId?.value,
                content = comment.content,
                authorId = comment.authorId,
                authorName = comment.authorName,
                likeCount = comment.likeCount,
                replyCount = comment.replyCount,
                createdAt = comment.createdAt,
                updatedAt = comment.updatedAt
            )
        }
    }
}
