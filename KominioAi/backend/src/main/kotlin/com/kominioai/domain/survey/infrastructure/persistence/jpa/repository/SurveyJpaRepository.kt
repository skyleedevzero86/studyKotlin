package com.kominioai.domain.survey.infrastructure.persistence.jpa.repository

import com.kominioai.domain.survey.domain.model.Survey
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.domain.valueobject.UserId
import org.springframework.data.jpa.repository.JpaRepository

interface SurveyJpaRepository : JpaRepository<Survey, String> {
    fun findByCreatedBy(userId: UserId): List<Survey>
    fun findByStatus(status: SurveyStatus): List<Survey>
}