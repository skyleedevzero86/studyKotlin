package com.kominioai.domain.bulletin.domain.service

import com.kominioai.domain.bulletin.domain.model.Post
import com.kominioai.domain.bulletin.domain.model.PostCategory
import org.springframework.stereotype.Service

@Service
class PostService {
    
    fun validatePostCreation(
        title: String,
        content: String,
        category: PostCategory,
        authorId: String
    ): Boolean {
        return title.isNotBlank() && 
               content.isNotBlank() && 
               title.length <= 200 && 
               content.length <= 5000 &&
               authorId.isNotBlank()
    }
    
    fun validatePostUpdate(
        title: String?,
        content: String?
    ): Boolean {
        return (title == null || title.isNotBlank()) &&
               (content == null || content.isNotBlank()) &&
               (title == null || title.length <= 200) &&
               (content == null || content.length <= 5000)
    }
    
    fun canUserEditPost(post: Post, userId: String): Boolean {
        return post.authorId == userId
    }
    
    fun canUserDeletePost(post: Post, userId: String, userRole: String): Boolean {
        return post.authorId == userId || userRole == "ADMIN"
    }
    
    fun shouldPinPost(category: PostCategory, pinned: Boolean): Boolean {
        return category == PostCategory.ANNOUNCEMENT && pinned
    }
    
    fun calculatePostScore(post: Post): Double {
        val viewWeight = 0.1
        val likeWeight = 1.0
        val commentWeight = 2.0
        val timeWeight = 0.01
        
        val timeScore = (System.currentTimeMillis() - post.createdAt.toEpochSecond(java.time.ZoneOffset.UTC) * 1000) / (1000 * 60 * 60 * 24) // days
        val pinnedBonus = if (post.pinned) 100.0 else 0.0
        
        return (post.viewCount * viewWeight + 
                post.likeCount * likeWeight + 
                post.commentCount * commentWeight + 
                timeScore * timeWeight + 
                pinnedBonus)
    }
}
