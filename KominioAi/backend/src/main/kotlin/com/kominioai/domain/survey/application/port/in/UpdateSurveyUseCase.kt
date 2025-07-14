package com.kominioai.domain.survey.application.port.`in`

import com.kominioai.domain.survey.application.dto.UpdateSurveyCommand
import com.kominioai.domain.survey.domain.model.SurveyId
import reactor.core.publisher.Mono

interface UpdateSurveyUseCase {
    fun updateSurvey(command: UpdateSurveyCommand): Mono<SurveyId>
}