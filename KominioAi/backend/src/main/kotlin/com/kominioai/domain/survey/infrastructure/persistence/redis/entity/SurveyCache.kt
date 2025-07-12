package com.kominioai.domain.survey.infrastructure.persistence.redis.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.index.Indexed
import java.time.LocalDateTime

@RedisHash("survey")
data class SurveyCache(
    @Id
    val id: String,
    
    @Indexed
    val title: String,
    
    val description: String?,
    
    @Indexed
    val status: String,
    
    @Indexed
    val createdBy: String,
    
    val createdAt: LocalDateTime,
    
    val updatedAt: LocalDateTime,
    
    val questionCount: Int,
    
    val responseCount: Int
) 