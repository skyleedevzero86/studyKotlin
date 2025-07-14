package com.kominioai.domain.survey.application.port.`in`

import com.kominioai.domain.survey.domain.model.SurveyId
import reactor.core.publisher.Mono

interface PublishSurveyUseCase {
    fun publishSurvey(surveyId: SurveyId): Mono<SurveyId>
}