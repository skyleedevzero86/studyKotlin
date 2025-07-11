package com.kominioai.domain.survey.application.port.output

import com.kominioai.domain.survey.infrastructure.persistence.jpa.entity.SurveyResponse
import com.kominioai.domain.survey.domain.valueobject.ResponseId
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface SurveyResponseRepository {
    fun save(response: SurveyResponse): Mono<SurveyResponse>
    fun findById(id: UUID): Mono<SurveyResponse>
    fun findBySurveyId(surveyId: UUID): Flux<SurveyResponse>
    fun countBySurveyId(surveyId: UUID): Mono<Long>
}