package com.kominioai.domain.bulletin.domain.service

import com.kominioai.domain.bulletin.domain.model.Comment
import org.springframework.stereotype.Service

@Service
class CommentService {
    
    fun validateCommentCreation(
        content: String,
        authorId: String
    ): Boolean {
        return content.isNotBlank() && 
               content.length <= 1000 &&
               authorId.isNotBlank()
    }
    
    fun validateCommentUpdate(content: String): Boolean {
        return content.isNotBlank() && content.length <= 1000
    }
    
    fun canUserEditComment(comment: Comment, userId: String): Boolean {
        return comment.authorId == userId
    }
    
    fun canUserDeleteComment(comment: Comment, userId: String, userRole: String): Boolean {
        return comment.authorId == userId || userRole == "ADMIN"
    }
    
    fun isReplyComment(comment: Comment): Boolean {
        return comment.parentId != null
    }
    
    fun calculateCommentDepth(comment: Comment, allComments: List<Comment>): Int {
        if (comment.parentId == null) return 0
        
        val parent = allComments.find { it.id == comment.parentId }
        return if (parent != null) {
            1 + calculateCommentDepth(parent, allComments)
        } else {
            0
        }
    }
    
    fun buildCommentTree(comments: List<Comment>): List<Comment> {
        val commentMap = comments.associateBy { it.id }
        val rootComments = mutableListOf<Comment>()
        
        comments.forEach { comment ->
            if (comment.parentId == null) {
                rootComments.add(comment)
            }
        }
        
        return rootComments.sortedBy { it.createdAt }
    }
    
    fun getCommentReplies(commentId: String, allComments: List<Comment>): List<Comment> {
        return allComments.filter { it.parentId?.value == commentId }
            .sortedBy { it.createdAt }
    }
}
