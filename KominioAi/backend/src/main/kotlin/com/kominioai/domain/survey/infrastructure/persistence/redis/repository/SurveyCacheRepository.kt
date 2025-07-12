package com.kominioai.domain.survey.infrastructure.persistence.redis.repository

import com.kominioai.domain.survey.infrastructure.persistence.redis.entity.SurveyCache
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SurveyCacheRepository : CrudRepository<SurveyCache, String> {
    fun findByStatus(status: String): List<SurveyCache>
    fun findByCreatedBy(createdBy: String): List<SurveyCache>
    fun findByTitleContaining(title: String): List<SurveyCache>
} 