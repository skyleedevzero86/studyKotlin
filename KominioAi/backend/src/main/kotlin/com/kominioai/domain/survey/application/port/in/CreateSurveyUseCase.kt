package com.kominioai.domain.survey.application.port.`in`

import com.kominioai.domain.survey.application.dto.CreateSurveyCommand
import com.kominioai.domain.survey.domain.model.SurveyId
import reactor.core.publisher.Mono

interface CreateSurveyUseCase {
    fun createSurvey(command: CreateSurveyCommand): Mono<SurveyId>
}