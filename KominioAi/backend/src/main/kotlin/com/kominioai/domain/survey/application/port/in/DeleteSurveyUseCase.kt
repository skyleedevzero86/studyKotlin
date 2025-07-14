package com.kominioai.domain.survey.application.port.`in`

import com.kominioai.domain.survey.domain.model.SurveyId
import reactor.core.publisher.Mono

interface DeleteSurveyUseCase {
    fun deleteSurvey(surveyId: SurveyId): Mono<Void>
}