package com.kominioai.domain.survey.application.port.out

import com.kominioai.domain.survey.domain.model.ParticipationId
import com.kominioai.domain.survey.domain.model.QuizParticipation
import com.kominioai.domain.survey.domain.model.SurveyId
import reactor.core.publisher.Mono

interface QuizParticipationPersistencePort {
    fun save(participation: QuizParticipation): Mono<QuizParticipation>
    fun findById(id: ParticipationId): Mono<QuizParticipation>
    fun findBySurveyId(surveyId: SurveyId): Mono<List<QuizParticipation>>
    fun findBySurveyIdAndParticipantPhone(surveyId: SurveyId, phone: String): Mono<QuizParticipation?>
    fun deleteById(id: ParticipationId): Mono<Boolean>
}