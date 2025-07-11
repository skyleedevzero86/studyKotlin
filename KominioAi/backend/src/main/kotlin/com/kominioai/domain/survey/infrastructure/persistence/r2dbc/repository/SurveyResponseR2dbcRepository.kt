package com.kominioai.domain.survey.infrastructure.persistence.r2dbc.repository

import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.entity.SurveyResponse
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface SurveyResponseR2dbcRepository : ReactiveCrudRepository<SurveyResponse, String> {
    fun findBySurveyId(surveyId: String): Flux<SurveyResponse>
    
    @Query("SELECT COUNT(*) FROM survey_responses WHERE survey_id = :surveyId")
    fun countBySurveyId(surveyId: String): Mono<Long>
}