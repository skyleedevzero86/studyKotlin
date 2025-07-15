package com.kominioai.domain.survey.application.port.out

import com.kominioai.domain.survey.domain.model.SurveyParticipation
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux

interface ParticipationPersistencePort {
    fun saveParticipation(participation: SurveyParticipation): Mono<Void>
    fun findBySurveyId(surveyId: String): Flux<SurveyParticipation>
    fun countBySurveyId(surveyId: String): Mono<Long>
}