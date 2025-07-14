package com.kominioai.domain.survey.application.port.`in`

import com.kominioai.domain.survey.application.dto.result.SurveyResultDto
import com.kominioai.domain.survey.application.query.SurveyResultQuery
import reactor.core.publisher.Mono

interface GetSurveyResultUseCase {
    fun getSurveyResult(query: SurveyResultQuery): Mono<SurveyResultDto>
}