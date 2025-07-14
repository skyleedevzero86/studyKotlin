package com.kominioai.domain.survey.application.port.out

import com.kominioai.domain.survey.domain.model.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface SurveyPersistencePort {
    fun findById(id: SurveyId): Mono<Survey>
    fun findAll(criteria: SurveySearchCriteria): Flux<Survey>
    fun save(survey: Survey): Mono<SurveyId>
    fun update(survey: Survey): Mono<SurveyId>
    fun deleteById(id: SurveyId): Mono<Void>
    fun deleteByIds(ids: List<SurveyId>): Mono<Void>
    fun count(criteria: SurveySearchCriteria): Mono<Long>
    fun existsById(id: SurveyId): Mono<Boolean>
}
