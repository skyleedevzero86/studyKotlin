package com.kominioai.domain.survey.application.port.output

import com.kominioai.domain.survey.infrastructure.persistence.jpa.entity.Survey
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.domain.valueobject.UserId
import org.springframework.data.r2dbc.repository.Query
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface SurveyRepository {
    fun save(survey: Survey): Mono<Survey>
    fun findById(id: UUID): Mono<Survey>
    fun findAll(): Flux<Survey>
    @Query("SELECT s FROM Survey s WHERE s.status = :status")
    fun findByStatus(status: SurveyStatus): Flux<Survey>
    fun findByCreatedBy(userId: UserId): Flux<Survey>
    fun findPublishedSurveys(): Flux<Survey>
    fun delete(id: UUID): Mono<Void>
    @Query("SELECT s FROM Survey s LEFT JOIN FETCH s.questions q LEFT JOIN FETCH q.options WHERE s.id = :id")
    fun findByIdWithQuestions(id: UUID): Mono<Survey>
}