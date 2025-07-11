package com.kominioai.domain.survey.application.port.output

import com.kominioai.domain.survey.domain.model.domain.Survey
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.domain.valueobject.UserId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface SurveyRepository {
    fun save(survey: Survey): Mono<Survey>
    fun findById(id: SurveyId): Mono<Survey>
    fun findAll(): Flux<Survey>
    fun findByStatus(status: SurveyStatus): Flux<Survey>
    fun findByCreatedBy(userId: UserId): Flux<Survey>
    fun findPublishedSurveys(): Flux<Survey>
    fun delete(id: SurveyId): Mono<Void>
    fun findByIdWithQuestions(id: SurveyId): Mono<Survey>
    fun findAllWithPaging(pageable: Pageable): Mono<Page<Survey>>
}