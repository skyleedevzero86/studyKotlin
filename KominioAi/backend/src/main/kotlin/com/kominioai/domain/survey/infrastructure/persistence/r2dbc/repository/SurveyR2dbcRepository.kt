package com.kominioai.domain.survey.infrastructure.persistence.r2dbc.repository

import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.entity.Survey
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface SurveyR2dbcRepository : ReactiveCrudRepository<Survey, String> {
    fun findByCreatedBy(userId: String): Flux<Survey>
    fun findByStatus(status: SurveyStatus): Flux<Survey>

    @Query("SELECT * FROM surveys WHERE status = 'PUBLISHED'")
    fun findPublishedSurveys(): Flux<Survey>
}