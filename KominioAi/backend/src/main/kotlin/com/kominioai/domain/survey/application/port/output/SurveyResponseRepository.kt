package com.kominioai.domain.survey.application.port.output

import com.kominioai.domain.survey.domain.model.domain.SurveyResponse
import com.kominioai.domain.survey.domain.valueobject.ResponseId
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface SurveyResponseRepository {
    fun save(response: SurveyResponse): Mono<SurveyResponse>
    fun findById(id: ResponseId): Mono<SurveyResponse>
    fun findBySurveyId(surveyId: SurveyId): Flux<SurveyResponse>
    fun countBySurveyId(surveyId: SurveyId): Mono<Long>
    fun findAll(): Flux<SurveyResponse>
    fun delete(id: ResponseId): Mono<Void>
}