package com.kominioai.domain.survey.application.port.out

import com.kominioai.domain.survey.domain.model.SurveyParticipation
import reactor.core.publisher.Mono

interface ParticipationPersistencePort {
    fun saveParticipation(participation: SurveyParticipation): Mono<Void>
}