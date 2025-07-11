package com.kominioai.domain.survey.infrastructure.persistence.jpa.repository

import com.kominioai.domain.survey.infrastructure.persistence.jpa.entity.Survey
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.domain.valueobject.UserId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface SurveyJpaRepository : JpaRepository<Survey, String> {
    fun findByCreatedBy(userId: UserId): List<Survey>
    fun findByStatus(status: SurveyStatus): List<Survey>

    @Query("SELECT s FROM Survey s LEFT JOIN FETCH s.questions q LEFT JOIN FETCH q.options WHERE s.id = :id")
    fun findByIdWithQuestions(id: String): Survey?
}