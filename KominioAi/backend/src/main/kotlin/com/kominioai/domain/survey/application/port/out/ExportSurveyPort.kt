package com.kominioai.domain.survey.application.port.out

import com.kominioai.domain.survey.domain.model.SurveyId
import reactor.core.publisher.Mono

interface ExportSurveyPort {
    fun exportSurveyResults(surveyId: SurveyId): Mono<ByteArray>
}