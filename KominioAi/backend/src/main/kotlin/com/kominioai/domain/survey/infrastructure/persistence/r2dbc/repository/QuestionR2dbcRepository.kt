package com.kominioai.domain.survey.infrastructure.persistence.r2dbc.repository

import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.entity.Question
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface QuestionR2dbcRepository : ReactiveCrudRepository<Question, String> {
    @Query("SELECT * FROM questions WHERE survey_id = :surveyId ORDER BY order_index")
    fun findQuestionsBySurveyId(surveyId: String): Flux<Question>
} 